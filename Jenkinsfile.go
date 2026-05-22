// Updated Jenkinsfile using Go utilities instead of Groovy Shared Library
// This version calls external Go tools for Docker operations, deployments, and notifications

pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 1, unit: 'HOURS')
        timestamps()
    }

    environment {
        REGISTRY = 'docker.io'
        REGISTRY_CREDENTIALS = 'docker-registry-credentials'
        IMAGE_NAME = 'moskowstock/backend'
        JAVA_VERSION = '21'
        BUILD_DIR = "${WORKSPACE}/target"
        JENKINS_HELPERS = "${WORKSPACE}/jenkins/go/bin"
    }

    stages {
        stage('Prepare') {
            steps {
                script {
                    echo "⚙️  Preparing environment..."

                    // Build Go utilities if they don't exist
                    sh '''
                        if [ ! -d "${WORKSPACE}/jenkins/go/bin" ]; then
                            echo "📦 Building Go utilities..."
                            cd "${WORKSPACE}/jenkins/go"
                            make clean build
                            cd "${WORKSPACE}"
                        fi
                    '''
                }
            }
        }

        stage('Checkout') {
            steps {
                script {
                    echo "🔄 Checking out source code..."
                    checkout scm
                }
            }
        }

        stage('Setup') {
            steps {
                script {
                    echo "⚙️  Setting up environment..."
                    sh 'chmod +x ./mvnw'
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    echo "🔨 Building project with Maven..."
                    sh './mvnw clean package -DskipTests -X'
                }
            }
        }

        stage('Unit Tests') {
            steps {
                script {
                    echo "🧪 Running unit tests..."
                    sh './mvnw test'
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                    jacoco(
                        execFilePattern: '**/target/jacoco.exec',
                        classPattern: '**/target/classes',
                        sourcePattern: '**/src/main/java',
                        exclusionPattern: '**/src/test/**'
                    )
                }
            }
        }

        stage('Code Quality Analysis') {
            steps {
                script {
                    echo "📊 Running SonarQube analysis..."
                    sh '''
                        ./mvnw sonar:sonar \
                            -Dsonar.projectKey=MoskowStockBackend \
                            -Dsonar.host.url=http://sonarqube:9000 \
                            -Dsonar.login=${SONARQUBE_TOKEN} \
                            || echo "SonarQube analysis completed with warnings"
                    '''
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    echo "🐳 Building Docker image..."
                    sh '''
                        ${JENKINS_HELPERS}/build-docker \
                            --image ${REGISTRY}/${IMAGE_NAME} \
                            --tag ${BUILD_NUMBER} \
                            --dockerfile Dockerfile \
                            --verbose
                    '''

                    // Also tag as latest
                    sh "docker tag ${REGISTRY}/${IMAGE_NAME}:${BUILD_NUMBER} ${REGISTRY}/${IMAGE_NAME}:latest"
                }
            }
        }

        stage('Push to Registry') {
            when {
                branch 'main'
            }
            steps {
                script {
                    echo "📤 Pushing Docker image to registry..."
                    withCredentials([usernamePassword(
                        credentialsId: "${REGISTRY_CREDENTIALS}",
                        usernameVariable: 'REGISTRY_USER',
                        passwordVariable: 'REGISTRY_PASS'
                    )]) {
                        sh '''
                            ${JENKINS_HELPERS}/push-docker \
                                --registry ${REGISTRY} \
                                --image ${IMAGE_NAME} \
                                --tag ${BUILD_NUMBER} \
                                --username ${REGISTRY_USER} \
                                --password ${REGISTRY_PASS} \
                                --verbose

                            # Also push latest tag
                            ${JENKINS_HELPERS}/push-docker \
                                --registry ${REGISTRY} \
                                --image ${IMAGE_NAME} \
                                --tag latest \
                                --username ${REGISTRY_USER} \
                                --password ${REGISTRY_PASS} \
                                --skip-logout \
                                --verbose
                        '''
                    }
                }
            }
        }

        stage('Deploy to Dev') {
            when {
                branch 'develop'
            }
            steps {
                script {
                    echo "🚀 Deploying to development environment..."
                    sh '''
                        ${JENKINS_HELPERS}/deploy-compose \
                            --environment dev \
                            --compose-files "compose.yaml" \
                            --healthcheck-url "http://localhost:8080/swagger-ui.html" \
                            --max-wait 120 \
                            --verbose
                    '''
                }
            }
        }

        stage('Deploy to Production') {
            when {
                tag pattern: "v\\d+\\.\\d+\\.\\d+$", comparator: "REGEXP"
            }
            input {
                message "Do you want to deploy to production?"
                ok "Deploy"
                submitter "admin,deployers"
            }
            steps {
                script {
                    echo "🚀 Deploying to production environment..."
                    sh '''
                        ${JENKINS_HELPERS}/deploy-compose \
                            --environment prod \
                            --compose-files "compose.yaml,compose.prod.yaml" \
                            --healthcheck-url "http://localhost:8080/swagger-ui.html" \
                            --max-wait 180 \
                            --verbose
                    '''
                }
            }
        }

        stage('Health Check') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'main'
                }
            }
            steps {
                script {
                    echo "❤️  Performing health checks..."
                    sh '''
                        max_attempts=10
                        attempt=0
                        while [ $attempt -lt $max_attempts ]; do
                            if curl -f http://localhost:8080/swagger-ui.html > /dev/null 2>&1; then
                                echo "✅ Application is healthy!"
                                exit 0
                            fi
                            attempt=$((attempt + 1))
                            echo "Waiting for application to start... (attempt $attempt/$max_attempts)"
                            sleep 5
                        done
                        echo "❌ Health check failed!"
                        exit 1
                    '''
                }
            }
        }

        stage('Smoke Tests') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'main'
                }
            }
            steps {
                script {
                    echo "🔥 Running smoke tests..."
                    sh '''
                        curl -s http://localhost:8080/swagger-ui.html | grep -q "swagger" && \
                            echo "✅ Swagger UI is accessible" || \
                            echo "⚠️  Swagger UI check failed"
                    '''
                }
            }
        }
    }

    post {
        always {
            echo "🧹 Cleaning up..."
            cleanWs()
        }

        success {
            echo "✅ Pipeline succeeded!"
            script {
                sh '''
                    ${JENKINS_HELPERS}/notify-build \
                        --status SUCCESS \
                        --job-name "${JOB_NAME}" \
                        --build-number "${BUILD_NUMBER}" \
                        --build-url "${BUILD_URL}" \
                        --slack-webhook "${SLACK_WEBHOOK_URL}" \
                        --verbose || echo "Notification completed"
                '''
            }
        }

        failure {
            echo "❌ Pipeline failed!"
            script {
                sh '''
                    ${JENKINS_HELPERS}/notify-build \
                        --status FAILURE \
                        --job-name "${JOB_NAME}" \
                        --build-number "${BUILD_NUMBER}" \
                        --build-url "${BUILD_URL}" \
                        --slack-webhook "${SLACK_WEBHOOK_URL}" \
                        --verbose || echo "Notification completed"
                '''
            }
        }

        unstable {
            echo "⚠️  Pipeline unstable!"
        }
    }
}

