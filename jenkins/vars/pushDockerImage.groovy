// Jenkins Shared Library function for pushing Docker image to registry
// Usage: pushDockerImage(registry: 'docker.io', imageName: 'myapp', credentialsId: 'docker-creds')

def call(Map config) {
    def registry = config.registry ?: 'docker.io'
    def imageName = config.imageName ?: 'moskowstock/backend'
    def tag = config.tag ?: 'latest'
    def credentialsId = config.credentialsId ?: 'docker-registry-credentials'
    def skipLogout = config.skipLogout ?: false

    echo "📤 Pushing Docker image to registry..."

    try {
        withCredentials([usernamePassword(
            credentialsId: credentialsId,
            usernameVariable: 'REGISTRY_USER',
            passwordVariable: 'REGISTRY_PASS'
        )]) {
            sh '''
                echo "${REGISTRY_PASS}" | docker login -u "${REGISTRY_USER}" --password-stdin ${registry}
                docker push ${registry}/${imageName}:${tag}
            '''

            if (!skipLogout) {
                sh "docker logout ${registry} || true"
            }
        }

        echo "✅ Docker image pushed successfully"
    } catch (Exception e) {
        echo "❌ Failed to push Docker image: ${e.message}"
        try {
            sh "docker logout ${registry} || true"
        } catch (Exception logoutError) {
            // Ignore logout errors
        }
        throw e
    }
}

