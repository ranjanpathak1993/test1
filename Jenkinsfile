pipeline {
    agent any
    environment {
        IMAGE_NAME = "sample-webapp"
        IMAGE_TAG  = "${env.BUILD_NUMBER ?: 'local'}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test (Maven)') {
            steps {
                // Windows compatible bat command
                bat 'mvn -B clean package'
            }
        }

        stage('Archive WAR') {
            steps {
                archiveArtifacts artifacts: 'target/*.war', fingerprint: true
            }
        }

        stage('Build Docker Image') {
            steps {
                powershell """
                # Assign variables for Docker build
                \$imageName = "${env.IMAGE_NAME}"
                \$imageTag = "${env.IMAGE_TAG}"

                # Build Docker image
                docker build -t \$imageName:\$imageTag .
                """
            }
        }

        stage('Run Container (Smoke Test)') {
            steps {
                powershell """
                \$imageName = "${env.IMAGE_NAME}"
                \$imageTag = "${env.IMAGE_TAG}"

                # Remove existing test container if exists
                docker rm -f \$imageName\_test -ErrorAction SilentlyContinue

                # Run container in detached mode
                docker run -d --name \$imageName\_test -p 8080:8080 \$imageName:\$imageTag

                # Wait 5 seconds for container to start
                Start-Sleep -Seconds 5

                try {
                    # Check if application is responding
                    \$response = Invoke-WebRequest -UseBasicParsing -Uri http://localhost:8080/ -ErrorAction Stop
                    Write-Output "Smoke test passed: \$($response.StatusCode)"
                } catch {
                    # Print logs if test fails
                    docker logs \$imageName\_test
                    exit 1
                } finally {
                    # Clean up test container
                    docker rm -f \$imageName\_test -ErrorAction SilentlyContinue
                }
                """
            }
        }
    }

    post {
        always {
            // Record JUnit test results
            junit 'target/surefire-reports/*.xml'
        }
    }
}
