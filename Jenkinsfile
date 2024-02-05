pipeline {
    agent any

    environment {
        GRADLE_USER_HOME = ".gradle"
        ANDROID_HOME = "/usr/local/android-sdk"
        ANDROID_USER_HOME = ".android"
    }

    stages {
        stage('Build') {
            agent {
                dockerfile {
                    filename "Dockerfile"
                    reuseNode true
                }
            }
            steps {
                sh "sh ./gradlew"
            }
        }
    }
}
