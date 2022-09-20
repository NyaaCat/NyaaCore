pipeline {
    agent any
    stages {
        stage('Build') {
            tools {
                jdk "jdk17"
            }
            steps {
                sh './gradlew reobfJar sourcesJar javadocJar'
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
