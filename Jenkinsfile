pipeline {
    agent any

    environment {
        IMAGE_NAME = "sample-webapp"
        IMAGE_TAG  = "${env.BUILD_NUMBER ?: 'local'}"
    }

    options {
        // Keep only the last 10 builds to save space
        buildDiscarder(logRotator(numToKeepStr: '10'))
        // Timeout entire pipeline after 30 minutes
        timeout(time: 30, unit: 'MINUTES')
        // ANSI color output for readability
        ansiColor('xterm')
    }

    stages {
        stage('Checkout') {
            steps {
                echo "Checking out code from SCM..."
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                echo "Building project with Maven..."
                bat 'mvn -B clean package'
            }
        }

        stage('Archive WAR') {
            steps {
                echo "Archiving WAR files..."
                archiveArtifacts artifacts: 'target/*.war', fingerprint: true
            }
        }

        stage('Build Docker Image') {
            steps {
                echo "Building Docker image ${env.IMAGE_NAME}:${env.IMAGE_TAG}..."
                powershell """
                docker build -t ${env.IMAGE_NAME}:${env.IMAGE_TAG} .
                """
            }
        }

        stage('Run Container (Smoke Test)') {
            steps {
                echo "Starting smoke test container..."
                powershell """
                # Remove previous test container if exists
                docker rm -f ${env.IMAGE_NAME}_test -ErrorAction SilentlyContinue

                # Run container in detached mode
                docker run -d --name ${env.IMAGE_NAME}_test -p 8080:8080 ${env.IMAGE_NAME}:${env.IMAGE_TAG}

                # Wait for container to start (max 30 seconds)
                \$maxRetries = 6
                \$retryCount = 0
                \$success = \$false

                while (-not \$success -and \$retryCount -lt \$maxRetries) {
                    Start-Sleep -Seconds 5
                    try {
                        \$response = Invoke-WebRequest -UseBasicParsing -Uri http://localhost:8080/ -ErrorAction Stop
                        Write-Output "Smoke test passed: \$($response.StatusCode)"
                        \$success = \$true
                    } catch {
                        Write-Output "Attempt \$([int](\$retryCount+1)) failed. Retrying..."
                        \$retryCount++
                    }
                }

                if (-not \$success) {
                    Write-Output "Smoke test failed. Printing container logs:"
                    docker logs ${env.IMAGE_NAME}_test
                    exit 1
                }

                # Cleanup test container
                docker rm -f ${env.IMAGE_NAME}_test -ErrorAction SilentlyContinue
                """
            }
        }
    }

    post {
        always {
            echo "Publishing JUnit test results..."
            junit 'target/surefire-reports/*.xml'

            echo "Cleaning up any leftover Docker containers..."
            powershell """
            docker rm -f ${env.IMAGE_NAME}_test -ErrorAction SilentlyContinue
            """
        }
        success {
            echo "Pipeline completed successfully! Docker image: ${env.IMAGE_NAME}:${env.IMAGE_TAG}"
        }
        failure {
            echo "Pipeline failed. Check console output and Docker logs."
        }
    }
}
