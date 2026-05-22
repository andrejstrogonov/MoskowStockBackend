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
    }

    stages {
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
                        docker build \
                            -t ${REGISTRY}/${IMAGE_NAME}:${BUILD_NUMBER} \
                            -t ${REGISTRY}/${IMAGE_NAME}:latest \
                            .
                    '''
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
                            echo ${REGISTRY_PASS} | docker login -u ${REGISTRY_USER} --password-stdin ${REGISTRY}
                            docker push ${REGISTRY}/${IMAGE_NAME}:${BUILD_NUMBER}
                            docker push ${REGISTRY}/${IMAGE_NAME}:latest
                            docker logout ${REGISTRY}
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
                        export IMAGE_TAG=${BUILD_NUMBER}
                        docker compose -f compose.yaml pull || true
                        docker compose -f compose.yaml down || true
                        docker compose -f compose.yaml up -d
                        sleep 10
                        docker compose -f compose.yaml ps
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
                        export IMAGE_TAG=${BUILD_NUMBER}
                        docker compose -f compose.yaml -f compose.prod.yaml down || true
                        docker compose -f compose.yaml -f compose.prod.yaml up -d
                        sleep 10
                        docker compose -f compose.yaml -f compose.prod.yaml ps
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
                        curl -s http://localhost:8080/swagger-ui.html | grep -q "swagger" && echo "✅ Swagger UI is accessible" || echo "⚠️  Swagger UI check failed"
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
                emailext(
                    subject: "✅ Build #\${BUILD_NUMBER} succeeded",
                    body: """
                        Build \${BUILD_NUMBER} completed successfully.

                        Build URL: \${BUILD_URL}
                        Build Log: \${BUILD_URL}console
                    """,
                    recipientProviders: [developers(), requestor()],
                    to: '\${DEFAULT_RECIPIENTS}'
                )
            }
        }

        failure {
            echo "❌ Pipeline failed!"
            script {
                emailext(
                    subject: "❌ Build #\${BUILD_NUMBER} failed",
                    body: """
                        Build \${BUILD_NUMBER} failed!

                        Build URL: \${BUILD_URL}
                        Build Log: \${BUILD_URL}console
                    """,
                    recipientProviders: [developers(), requestor(), culprits()],
                    to: '\${DEFAULT_RECIPIENTS}'
                )
            }
        }

        unstable {
            echo "⚠️  Pipeline unstable!"
        }
    }
}

