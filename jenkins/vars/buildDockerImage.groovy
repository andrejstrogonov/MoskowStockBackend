// Jenkins Shared Library function for Docker image building
// Usage: buildDockerImage(imageName: 'myapp', tag: 'latest')

def call(Map config) {
    def imageName = config.imageName ?: 'moskowstock/backend'
    def tag = config.tag ?: 'latest'
    def dockerfile = config.dockerfile ?: 'Dockerfile'
    def buildArgs = config.buildArgs ?: ''

    echo "🐳 Building Docker image: ${imageName}:${tag}"

    try {
        sh '''
            docker build \
                -t ${imageName}:${tag} \
                -f ${dockerfile} \
                ${buildArgs} \
                .
        '''
        echo "✅ Docker image built successfully"
    } catch (Exception e) {
        echo "❌ Failed to build Docker image: ${e.message}"
        throw e
    }
}

