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
                \$imageName = '${env.IMAGE_NAME}'
                \$imageTag = '${env.IMAGE_TAG}'

                docker build -t \$imageName:\$imageTag .
                """
            }
        }

        stage('Run Container (Smoke Test)') {
            steps {
                powershell """
                \$imageName = '${env.IMAGE_NAME}'
                \$imageTag = '${env.IMAGE_TAG}'

                # Remove existing test container if exists
                docker rm -f "\${imageName}_test" -ErrorAction SilentlyContinue

                # Run container in detached mode
                docker run -d --name "\${imageName}_test" -p 8080:8080 \$imageName:\$imageTag

                # Wait for container to start
                Start-Sleep -Seconds 5

                try {
                    # Smoke test
                    \$response = Invoke-WebRequest -UseBasicParsing -Uri http://localhost:8080/ -ErrorAction Stop
                    Write-Output "Smoke test passed: \$($response.StatusCode)"
                } catch {
                    docker logs "\${imageName}_test"
                    exit 1
                } finally {
                    # Clean up
                    docker rm -f "\${imageName}_test" -ErrorAction SilentlyContinue
                }
                """
            }
        }
    }

    post {
        always {
            junit 'target/surefire-reports/*.xml'
        }
    }
}
