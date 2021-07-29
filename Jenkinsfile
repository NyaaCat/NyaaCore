pipeline {
    agent any
    stages {
        stage('Build') {
            tools {
                jdk "openjdk11"
            }
            steps {
                sh './gradlew shadowJar publish'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
            cleanWs()
        }
    }
}
