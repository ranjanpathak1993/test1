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

                # Correct variable interpolation using ${}
                docker build -t \$(${imageName}):\$(${imageTag}) .
                """
            }
        }

        stage('Run Container (Smoke Test)') {
            steps {
                powershell """
                \$imageName = '${env.IMAGE_NAME}'
                \$imageTag = '${env.IMAGE_TAG}'

                docker rm -f "\${imageName}_test" -ErrorAction SilentlyContinue
                docker run -d --name "\${imageName}_test" -p 8080:8080 \$(${imageName}):\$(${imageTag})

                Start-Sleep -Seconds 5

                try {
                    \$response = Invoke-WebRequest -UseBasicParsing -Uri http://localhost:8080/ -ErrorAction Stop
                    Write-Output "Smoke test passed: \$($response.StatusCode)"
                } catch {
                    docker logs "\${imageName}_test"
                    exit 1
                } finally {
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
