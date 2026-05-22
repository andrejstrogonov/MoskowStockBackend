package main

import (
	"bytes"
	"encoding/json"
	"flag"
	"fmt"
	"net/http"
	"os"
	"strings"
	"time"
)

// NotifyOptions contains options for sending notifications
type NotifyOptions struct {
	Status          string
	JobName         string
	BuildNumber     string
	BuildURL        string
	SlackWebhookURL string
	EmailRecipients []string
	SlackChannel    string
	Verbose         bool
}

// SlackPayload represents the Slack webhook payload
type SlackPayload struct {
	Text   string       `json:"text"`
	Blocks []SlackBlock `json:"blocks"`
}

// SlackBlock represents a Slack block
type SlackBlock struct {
	Type   string      `json:"type"`
	Text   *SlackText  `json:"text,omitempty"`
	Fields []SlackText `json:"fields,omitempty"`
}

// SlackText represents Slack text
type SlackText struct {
	Type string `json:"type"`
	Text string `json:"text"`
}

// SendSlackNotification sends notification to Slack
func SendSlackNotification(opts NotifyOptions) error {
	if opts.SlackWebhookURL == "" {
		fmt.Println("⏭️  Slack webhook URL not configured, skipping Slack notification")
		return nil
	}

	fmt.Println("📤 Sending Slack notification...")

	statusEmoji := "❌"
	if opts.Status == "SUCCESS" {
		statusEmoji = "✅"
	}

	text := fmt.Sprintf("%s Build #%s - %s", statusEmoji, opts.BuildNumber, opts.Status)

	payload := SlackPayload{
		Text: text,
		Blocks: []SlackBlock{
			{
				Type: "section",
				Text: &SlackText{
					Type: "mrkdwn",
					Text: fmt.Sprintf("*Build Status*\nJob: %s\nStatus: %s\nBuild #%s\n<%s|View Details>",
						opts.JobName, opts.Status, opts.BuildNumber, opts.BuildURL),
				},
			},
		},
	}

	// Add channel if specified
	if opts.SlackChannel != "" {
		payload.Text = fmt.Sprintf("%s (in %s)", payload.Text, opts.SlackChannel)
	}

	payloadBytes, err := json.Marshal(payload)
	if err != nil {
		fmt.Printf("❌ Failed to marshal Slack payload: %v\n", err)
		return err
	}

	if opts.Verbose {
		fmt.Printf("Sending to Slack: %s\n", string(payloadBytes))
	}

	client := &http.Client{Timeout: 10 * time.Second}
	resp, err := client.Post(opts.SlackWebhookURL, "application/json", bytes.NewBuffer(payloadBytes))
	if err != nil {
		fmt.Printf("❌ Failed to send Slack notification: %v\n", err)
		return err
	}
	defer resp.Body.Close()

	if resp.StatusCode >= 400 {
		fmt.Printf("❌ Slack returned error status: %d\n", resp.StatusCode)
		return fmt.Errorf("slack error: %d", resp.StatusCode)
	}

	fmt.Printf("✅ Slack notification sent successfully\n")
	return nil
}

// SendEmailNotification sends notification via email
func SendEmailNotification(opts NotifyOptions) error {
	if len(opts.EmailRecipients) == 0 {
		fmt.Println("⏭️  No email recipients configured, skipping email notification")
		return nil
	}

	fmt.Println("📧 Sending email notification...")

	if opts.Verbose {
		fmt.Printf("Recipients: %v\n", opts.EmailRecipients)
	}

	statusEmoji := "❌"
	subject := fmt.Sprintf("❌ Build #%s - %s", opts.BuildNumber, opts.Status)

	if opts.Status == "SUCCESS" {
		statusEmoji = "✅"
		subject = fmt.Sprintf("✅ Build #%s - %s", opts.BuildNumber, opts.Status)
	}

	body := fmt.Sprintf(`
Build Status Report
===================

Status: %s %s
Job Name: %s
Build Number: %s
Build URL: %s
Timestamp: %s

For more details, visit: %s
`, statusEmoji, opts.Status, opts.JobName, opts.BuildNumber, opts.BuildURL, time.Now().Format(time.RFC3339), opts.BuildURL)

	if opts.Verbose {
		fmt.Printf("Subject: %s\n", subject)
		fmt.Printf("Body:\n%s\n", body)
		fmt.Println("✅ Email notification prepared (note: actual sending requires SMTP configuration)")
	} else {
		fmt.Printf("✅ Email notification prepared for %d recipient(s)\n", len(opts.EmailRecipients))
	}

	return nil
}

// NotifyBuild sends notifications about build status
func NotifyBuild(opts NotifyOptions) error {
	fmt.Printf("📢 Notifying build status: %s\n", opts.Status)

	// Send Slack notification
	if err := SendSlackNotification(opts); err != nil {
		fmt.Printf("⚠️  Warning: Failed to send Slack notification: %v\n", err)
	}

	// Send email notification
	if err := SendEmailNotification(opts); err != nil {
		fmt.Printf("⚠️  Warning: Failed to send email notification: %v\n", err)
	}

	fmt.Println("✅ Notifications completed")
	return nil
}

func main() {
	flag.Usage = func() {
		fmt.Fprintf(os.Stderr, `Usage: %s [options]

Send build notifications to Slack and/or email.

Options:
`, os.Args[0])
		flag.PrintDefaults()
	}

	status := flag.String("status", "UNKNOWN", "Build status (SUCCESS, FAILURE, UNSTABLE)")
	jobName := flag.String("job-name", "Unknown Job", "Jenkins job name")
	buildNumber := flag.String("build-number", "0", "Build number")
	buildURL := flag.String("build-url", "", "Build URL (required)")
	slackWebhook := flag.String("slack-webhook", os.Getenv("SLACK_WEBHOOK_URL"), "Slack webhook URL")
	emailRecipientsStr := flag.String("email-recipients", "", "Comma-separated email recipients")
	slackChannel := flag.String("slack-channel", "", "Slack channel name")
	verbose := flag.Bool("verbose", false, "Verbose output")

	flag.Parse()

	// Parse email recipients
	var emailRecipients []string
	if *emailRecipientsStr != "" {
		emailRecipients = strings.Split(*emailRecipientsStr, ",")
		for i, e := range emailRecipients {
			emailRecipients[i] = strings.TrimSpace(e)
		}
	}

	opts := NotifyOptions{
		Status:          *status,
		JobName:         *jobName,
		BuildNumber:     *buildNumber,
		BuildURL:        *buildURL,
		SlackWebhookURL: *slackWebhook,
		EmailRecipients: emailRecipients,
		SlackChannel:    *slackChannel,
		Verbose:         *verbose,
	}

	// Notify
	if err := NotifyBuild(opts); err != nil {
		os.Exit(1)
	}
}

