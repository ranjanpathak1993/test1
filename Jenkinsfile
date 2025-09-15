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

        stage('Build & Test') {
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
                docker build -t ${env.IMAGE_NAME}:${env.IMAGE_TAG} .
                """
            }
        }

        stage('Run Container (Smoke Test)') {
            steps {
                powershell """
                docker rm -f ${env.IMAGE_NAME}_test -ErrorAction SilentlyContinue
                docker run -d --name ${env.IMAGE_NAME}_test -p 8080:8080 ${env.IMAGE_NAME}:${env.IMAGE_TAG}

                Start-Sleep -Seconds 5

                try {
                    \$response = Invoke-WebRequest -UseBasicParsing -Uri http://localhost:8080/ -ErrorAction Stop
                    Write-Output "Smoke test passed: \$($response.StatusCode)"
                } catch {
                    docker logs ${env.IMAGE_NAME}_test
                    exit 1
                } finally {
                    docker rm -f ${env.IMAGE_NAME}_test -ErrorAction SilentlyContinue
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
