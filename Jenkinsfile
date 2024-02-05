pipeline {
    agent any

    environment {
        GRADLE_USER_HOME = "$WORKSPACE/.gradle"
        ANDROID_HOME = "/usr/local/android-sdk"
        ANDROID_USER_HOME = "$WORKSPACE/.android"
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
                sh "test ! -f $ANDROID_USER_HOME/debug.keystore && keytool -genkey -v -keystore $ANDROID_USER_HOME/debug.keystore -alias androiddebugkey -storepass android -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname \"CN=Android Debug,O=Android,C=US\" || true"
                sh "sh ./gradlew build"
            }
        }
    }
}
