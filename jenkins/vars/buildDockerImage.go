package main

import (
	"flag"
	"fmt"
	"log"
	"os"
	"os/exec"
	"strings"
	"time"
)

// BuildDockerImageOptions contains options for building Docker image
type BuildDockerImageOptions struct {
	ImageName  string
	Tag        string
	Dockerfile string
	BuildArgs  string
	Verbose    bool
}

// BuildDockerImage builds a Docker image with specified options
func BuildDockerImage(opts BuildDockerImageOptions) error {
	fmt.Printf("🐳 Building Docker image: %s:%s\n", opts.ImageName, opts.Tag)

	cmd := exec.Command("docker", "build")

	// Add image name and tag
	cmd.Args = append(cmd.Args, "-t", fmt.Sprintf("%s:%s", opts.ImageName, opts.Tag))

	// Add dockerfile if specified
	if opts.Dockerfile != "" {
		cmd.Args = append(cmd.Args, "-f", opts.Dockerfile)
	}

	// Add build arguments if specified
	if opts.BuildArgs != "" {
		buildArgsList := strings.Split(opts.BuildArgs, ",")
		for _, arg := range buildArgsList {
			cmd.Args = append(cmd.Args, "--build-arg", strings.TrimSpace(arg))
		}
	}

	// Add working directory (current directory)
	cmd.Args = append(cmd.Args, ".")

	if opts.Verbose {
		fmt.Printf("Running command: docker %v\n", cmd.Args)
	}

	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr

	if err := cmd.Run(); err != nil {
		fmt.Printf("❌ Failed to build Docker image: %v\n", err)
		return err
	}

	fmt.Printf("✅ Docker image built successfully: %s:%s\n", opts.ImageName, opts.Tag)
	return nil
}

func main() {
	flag.Usage = func() {
		fmt.Fprintf(os.Stderr, `Usage: %s [options]

Build a Docker image with specified options.

Options:
`, os.Args[0])
		flag.PrintDefaults()
	}

	imageName := flag.String("image", "", "Docker image name (required)")
	tag := flag.String("tag", "latest", "Docker image tag")
	dockerfile := flag.String("dockerfile", "Dockerfile", "Path to Dockerfile")
	buildArgs := flag.String("build-args", "", "Build arguments (comma-separated)")
	verbose := flag.Bool("verbose", false, "Verbose output")

	flag.Parse()

	// Validate required arguments
	if *imageName == "" {
		fmt.Fprintf(os.Stderr, "Error: --image is required\n")
		flag.Usage()
		os.Exit(1)
	}

	opts := BuildDockerImageOptions{
		ImageName:  *imageName,
		Tag:        *tag,
		Dockerfile: *dockerfile,
		BuildArgs:  *buildArgs,
		Verbose:    *verbose,
	}

	// Start timer
	start := time.Now()

	// Build image
	if err := BuildDockerImage(opts); err != nil {
		os.Exit(1)
	}

	// Print execution time
	elapsed := time.Since(start)
	fmt.Printf("⏱️  Build completed in %v\n", elapsed)
}

