pipeline {
    agent any

    environment {
        IMAGE_NAME = "sample-webapp"
        CONTAINER_NAME = "sample-webapp_test"
        WAR_FILE = "target/sample-webapp-1.0.0.war"
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
                bat "mvn -B clean package"
            }
        }

        stage('Archive WAR') {
            steps {
                echo "Archiving WAR files..."
                archiveArtifacts artifacts: WAR_FILE, fingerprint: true
            }
        }

        stage('Build Docker Image') {
            steps {
                echo "Building Docker image ${IMAGE_NAME}:14..."
                powershell "docker build -t ${IMAGE_NAME}:14 ."
            }
        }

        stage('Run Container (Smoke Test)') {
            steps {
                echo "Starting smoke test container..."
                powershell """
                # Delete existing container if exists
                try {
                    docker rm -f ${CONTAINER_NAME}
                } catch {
                    Write-Output "Container does not exist, skipping removal."
                }

                # Run new container
                docker run -d --name ${CONTAINER_NAME} -p 8080:8080 ${IMAGE_NAME}:14
                docker ps
                """
            }
        }
    }

    post {
        always {
            echo "Cleaning up Docker containers..."
            powershell """
            try {
                docker rm -f ${CONTAINER_NAME}
            } catch {
                Write-Output "Container does not exist, skipping removal."
            }
            """

            echo "Publishing JUnit test results..."
            junit '**/target/surefire-reports/*.xml'
        }

        success {
            echo "Pipeline completed successfully!"
        }

        failure {
            echo "Pipeline failed. Check console output and Docker logs."
        }
    }
}
