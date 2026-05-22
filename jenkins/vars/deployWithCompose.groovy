// Jenkins Shared Library function for deploying with Docker Compose
// Usage: deployWithCompose(environment: 'dev', composeFiles: ['compose.yaml'])

def call(Map config) {
    def environment = config.environment ?: 'dev'
    def composeFiles = config.composeFiles ?: ['compose.yaml']
    def healthcheckUrl = config.healthcheckUrl ?: 'http://localhost:8080/swagger-ui.html'
    def maxWaitTime = config.maxWaitTime ?: 120

    echo "🚀 Deploying to ${environment} environment..."

    try {
        // Construct docker-compose command
        def composeCmd = composeFiles.collect { "-f ${it}" }.join(' ')

        // Pull latest images
        sh "docker compose ${composeCmd} pull || true"

        // Stop and remove existing containers
        sh "docker compose ${composeCmd} down || true"

        // Start services
        sh "docker compose ${composeCmd} up -d"

        // Wait for services to be ready
        sh "sleep 10"

        // Display service status
        sh "docker compose ${composeCmd} ps"

        // Health check
        def healthcheckPassed = false
        for (int i = 0; i < maxWaitTime; i += 5) {
            try {
                sh "curl -f ${healthcheckUrl} > /dev/null 2>&1"
                healthcheckPassed = true
                echo "✅ Application is healthy!"
                break
            } catch (Exception e) {
                echo "Waiting for application to be ready... (${i}s/${maxWaitTime}s)"
                sleep(5)
            }
        }

        if (!healthcheckPassed) {
            echo "⚠️  Healthcheck did not pass within ${maxWaitTime}s"
        }

        echo "✅ Deployment to ${environment} completed"
    } catch (Exception e) {
        echo "❌ Deployment failed: ${e.message}"
        throw e
    }
}

