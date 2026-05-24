package main

import (
	"flag"
	"fmt"
	"os"
	"os/exec"
	"strings"
	"time"
)

// PushDockerImageOptions contains options for pushing Docker image
type PushDockerImageOptions struct {
	Registry      string
	ImageName     string
	Tag           string
	Username      string
	Password      string
	SkipLogout    bool
	Verbose       bool
}

// LoginToRegistry logs in to Docker registry
func LoginToRegistry(registry, username, password string, verbose bool) error {
	fmt.Printf("🔐 Logging in to Docker registry: %s\n", registry)

	cmd := exec.Command("docker", "login", "-u", username, "--password-stdin", registry)
	cmd.Stdin = strings.NewReader(password)
	cmd.Stderr = os.Stderr

	if verbose {
		fmt.Printf("Running command: docker login -u %s --password-stdin %s\n", username, registry)
	}

	if err := cmd.Run(); err != nil {
		fmt.Printf("❌ Failed to login to registry: %v\n", err)
		return err
	}

	fmt.Printf("✅ Successfully logged in to registry\n")
	return nil
}

// PushDockerImage pushes Docker image to registry
func PushDockerImage(opts PushDockerImageOptions) error {
	fmt.Printf("📤 Pushing Docker image to registry...\n")

	// Login to registry
	if err := LoginToRegistry(opts.Registry, opts.Username, opts.Password, opts.Verbose); err != nil {
		return err
	}

	// Build full image name
	fullImageName := fmt.Sprintf("%s/%s:%s", opts.Registry, opts.ImageName, opts.Tag)

	fmt.Printf("🚀 Pushing image: %s\n", fullImageName)

	// Push image
	cmd := exec.Command("docker", "push", fullImageName)
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr

	if opts.Verbose {
		fmt.Printf("Running command: docker push %s\n", fullImageName)
	}

	if err := cmd.Run(); err != nil {
		fmt.Printf("❌ Failed to push Docker image: %v\n", err)
		return err
	}

	fmt.Printf("✅ Docker image pushed successfully\n")

	// Logout from registry if not skipped
	if !opts.SkipLogout {
		fmt.Printf("🔓 Logging out from registry\n")
		cmd := exec.Command("docker", "logout", opts.Registry)
		cmd.Stderr = os.Stderr

		if err := cmd.Run(); err != nil {
			fmt.Printf("⚠️  Warning: Failed to logout from registry: %v (continuing anyway)\n", err)
		} else {
			fmt.Printf("✅ Successfully logged out from registry\n")
		}
	}

	return nil
}

func main() {
	flag.Usage = func() {
		fmt.Fprintf(os.Stderr, `Usage: %s [options]

Push a Docker image to a registry.

Options:
`, os.Args[0])
		flag.PrintDefaults()
	}

	registry := flag.String("registry", "docker.io", "Docker registry")
	imageName := flag.String("image", "", "Docker image name (required)")
	tag := flag.String("tag", "latest", "Docker image tag")
	username := flag.String("username", "", "Registry username (required)")
	password := flag.String("password", "", "Registry password (required)")
	skipLogout := flag.Bool("skip-logout", false, "Skip logout after push")
	verbose := flag.Bool("verbose", false, "Verbose output")

	flag.Parse()

	// Validate required arguments
	if *imageName == "" {
		fmt.Fprintf(os.Stderr, "Error: --image is required\n")
		flag.Usage()
		os.Exit(1)
	}
	if *username == "" {
		fmt.Fprintf(os.Stderr, "Error: --username is required\n")
		flag.Usage()
		os.Exit(1)
	}
	if *password == "" {
		fmt.Fprintf(os.Stderr, "Error: --password is required or DOCKER_PASSWORD env var should be set\n")
		flag.Usage()
		os.Exit(1)
	}

	opts := PushDockerImageOptions{
		Registry:   *registry,
		ImageName:  *imageName,
		Tag:        *tag,
		Username:   *username,
		Password:   *password,
		SkipLogout: *skipLogout,
		Verbose:    *verbose,
	}

	// Start timer
	start := time.Now()

	// Push image
	if err := PushDockerImage(opts); err != nil {
		os.Exit(1)
	}

	// Print execution time
	elapsed := time.Since(start)
	fmt.Printf("⏱️  Push completed in %v\n", elapsed)
}

