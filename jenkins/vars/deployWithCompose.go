package main

import (
	"flag"
	"fmt"
	"net/http"
	"os"
	"os/exec"
	"strings"
	"time"
)

// DeployOptions contains options for deploying with Docker Compose
type DeployOptions struct {
	Environment     string
	ComposeFiles    []string
	HealthcheckURL  string
	MaxWaitTime     int
	Verbose         bool
}

// ExecuteComposeCommand executes a docker-compose command
func ExecuteComposeCommand(composeFiles []string, args []string, verbose bool) error {
	cmd := exec.Command("docker", "compose")

	// Add compose file flags
	for _, file := range composeFiles {
		cmd.Args = append(cmd.Args, "-f", file)
	}

	// Add additional arguments
	cmd.Args = append(cmd.Args, args...)

	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr

	if verbose {
		fmt.Printf("Running command: docker %v\n", cmd.Args)
	}

	if err := cmd.Run(); err != nil {
		return err
	}

	return nil
}

// CheckHealth checks if the application is healthy
func CheckHealth(url string, maxWaitTime int, verbose bool) error {
	fmt.Printf("❤️  Performing health checks at %s\n", url)

	timeout := time.Duration(maxWaitTime) * time.Second
	start := time.Now()

	client := &http.Client{
		Timeout: 5 * time.Second,
	}

	for {
		resp, err := client.Get(url)
		if err == nil && resp.StatusCode < 400 {
			fmt.Printf("✅ Application is healthy!\n")
			resp.Body.Close()
			return nil
		}

		if resp != nil {
			resp.Body.Close()
		}

		if time.Since(start) > timeout {
			fmt.Printf("❌ Health check failed after %ds\n", maxWaitTime)
			return fmt.Errorf("health check timeout")
		}

		elapsed := time.Since(start).Seconds()
		fmt.Printf("⏳ Waiting for application to start... (%.0fs elapsed)\n", elapsed)

		if verbose && err != nil {
			fmt.Printf("  Error: %v\n", err)
		}

		time.Sleep(5 * time.Second)
	}
}

// DeployWithCompose deploys application using Docker Compose
func DeployWithCompose(opts DeployOptions) error {
	fmt.Printf("🚀 Deploying to %s environment...\n", opts.Environment)

	// Pull latest images
	fmt.Println("📦 Pulling latest images...")
	if err := ExecuteComposeCommand(opts.ComposeFiles, []string{"pull"}, opts.Verbose); err != nil {
		fmt.Printf("⚠️  Warning: Failed to pull images (continuing): %v\n", err)
	}

	// Stop and remove existing containers
	fmt.Println("🛑 Stopping existing containers...")
	if err := ExecuteComposeCommand(opts.ComposeFiles, []string{"down"}, opts.Verbose); err != nil {
		fmt.Printf("⚠️  Warning: Failed to stop containers (continuing): %v\n", err)
	}

	// Start services
	fmt.Println("▶️  Starting services...")
	if err := ExecuteComposeCommand(opts.ComposeFiles, []string{"up", "-d"}, opts.Verbose); err != nil {
		fmt.Printf("❌ Failed to start services: %v\n", err)
		return err
	}

	// Wait for services to be ready
	fmt.Println("⏳ Waiting for services to be ready...")
	time.Sleep(10 * time.Second)

	// Display service status
	fmt.Println("📊 Service status:")
	if err := ExecuteComposeCommand(opts.ComposeFiles, []string{"ps"}, opts.Verbose); err != nil {
		fmt.Printf("⚠️  Warning: Failed to get service status: %v\n", err)
	}

	// Health check
	if opts.HealthcheckURL != "" {
		if err := CheckHealth(opts.HealthcheckURL, opts.MaxWaitTime, opts.Verbose); err != nil {
			fmt.Printf("⚠️  Health check did not pass within %ds\n", opts.MaxWaitTime)
		}
	}

	fmt.Printf("✅ Deployment to %s completed\n", opts.Environment)
	return nil
}

func main() {
	flag.Usage = func() {
		fmt.Fprintf(os.Stderr, `Usage: %s [options]

Deploy application using Docker Compose.

Options:
`, os.Args[0])
		flag.PrintDefaults()
	}

	environment := flag.String("environment", "dev", "Environment name (dev, staging, prod)")
	composeFilesStr := flag.String("compose-files", "compose.yaml", "Comma-separated list of compose files")
	healthcheckURL := flag.String("healthcheck-url", "http://localhost:8080/swagger-ui.html", "Application health check URL")
	maxWaitTime := flag.Int("max-wait", 120, "Maximum time to wait for health check (seconds)")
	verbose := flag.Bool("verbose", false, "Verbose output")

	flag.Parse()

	// Parse compose files
	composeFiles := strings.Split(*composeFilesStr, ",")
	for i, f := range composeFiles {
		composeFiles[i] = strings.TrimSpace(f)
	}

	opts := DeployOptions{
		Environment:    *environment,
		ComposeFiles:   composeFiles,
		HealthcheckURL: *healthcheckURL,
		MaxWaitTime:    *maxWaitTime,
		Verbose:        *verbose,
	}

	// Start timer
	start := time.Now()

	// Deploy
	if err := DeployWithCompose(opts); err != nil {
		os.Exit(1)
	}

	// Print execution time
	elapsed := time.Since(start)
	fmt.Printf("⏱️  Deployment completed in %v\n", elapsed)
}

