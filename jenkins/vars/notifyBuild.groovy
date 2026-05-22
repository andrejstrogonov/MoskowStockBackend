// Jenkins Shared Library function for sending notifications
// Usage: notifyBuild(status: 'SUCCESS', slackChannel: '#deployments')

def call(Map config) {
    def status = config.status ?: 'UNKNOWN'
    def slackChannel = config.slackChannel ?: '#ci-cd'
    def slackWebhookId = config.slackWebhookId ?: 'slack-webhook'
    def notifyEmail = config.notifyEmail ?: true
    def recipients = config.recipients ?: '${DEFAULT_RECIPIENTS}'

    def statusEmoji = status == 'SUCCESS' ? '✅' : '❌'
    def statusColor = status == 'SUCCESS' ? 'good' : 'danger'

    // Slack notification
    if (env.SLACK_WEBHOOK_URL) {
        try {
            def message = """
            ${statusEmoji} Build *${env.BUILD_NUMBER}* - ${status}
            Job: ${env.JOB_NAME}
            Build URL: ${env.BUILD_URL}
            Branch: ${env.BRANCH_NAME ?: 'N/A'}
            """

            sh '''
                curl -X POST -H 'Content-type: application/json' \
                    --data '{"text":"''' + message + '''"}' \
                    ${SLACK_WEBHOOK_URL}
            '''
        } catch (Exception e) {
            echo "⚠️  Failed to send Slack notification: ${e.message}"
        }
    }

    // Email notification
    if (notifyEmail) {
        try {
            emailext(
                subject: "${statusEmoji} Build #${env.BUILD_NUMBER} - ${status}",
                body: """
                    Build ${env.BUILD_NUMBER} ${status}
                    
                    Job: ${env.JOB_NAME}
                    Build URL: ${env.BUILD_URL}
                    Console Output: ${env.BUILD_URL}console
                    
                    Commit: ${env.GIT_COMMIT ?: 'N/A'}
                    Branch: ${env.BRANCH_NAME ?: 'N/A'}
                """,
                recipientProviders: [developers(), requestor(), culprits()],
                to: recipients,
                mimeType: 'text/plain'
            )
        } catch (Exception e) {
            echo "⚠️  Failed to send email notification: ${e.message}"
        }
    }
}

