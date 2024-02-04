pipeline {
    agent any

    stages {
        stage('Build') {
            agent {
                dockerfile {
                    filename "Dockerfile"
                    //additionalBuildArgs "--pull --build-arg RUST_VERSION=${RUST_VERSION}"
                    reuseNode true
                }
            }
            steps {
                sh "./gradlew"
            }
        }
    }
}
