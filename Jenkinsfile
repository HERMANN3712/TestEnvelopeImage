pipeline {
  agent {
    node {
      label 'TestNoeud'
    }

  }
  stages {
    stage('') {
      steps {
        sh 'mkdir TestEnvelopeImage'
        sh 'cd TestEnvelopeImage'
        sh 'gh repo clone HERMANN3712/TestEnvelopeImage'
        sh 'mvn clean javafx:run'
      }
    }

  }
}