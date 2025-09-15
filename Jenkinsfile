pipeline {
  agent any
  environment {
    IMAGE_NAME = "sample-webapp"
    IMAGE_TAG = "${env.BUILD_NUMBER ?: 'local'}"
  }
  stages {
    stage('Checkout') {
      steps { checkout scm }
    }
    stage('Build & Test') {
      steps { sh 'mvn -B clean package' }
    }
    stage('Archive WAR') {
      steps { archiveArtifacts artifacts: 'target/*.war', fingerprint: true }
    }
    stage('Build Docker Image') {
      steps { sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ." }
    }
    stage('Run Container (smoke test)') {
      steps {
        sh ''' 
          docker rm -f ${IMAGE_NAME}_test || true
          docker run -d --name ${IMAGE_NAME}_test -p 8080:8080 ${IMAGE_NAME}:${IMAGE_TAG}
          sleep 5
          curl -f http://localhost:8080/ || (docker logs ${IMAGE_NAME}_test && exit 1)
          docker rm -f ${IMAGE_NAME}_test
        '''
      }
    }
  }
}