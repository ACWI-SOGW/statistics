pipeline {
    agent any

    stages {
        stage('Package') {
            steps {
                withMaven(maven:'maven_3_5_4') {
                    sh 'mvn clean package'
                }
            }
        }
        stage('Test') {
            steps {
                withMaven(maven:'maven_3_5_4') {
                    sh 'mvn test'
                }
            }
        }
        stage('Maven Install') {
            steps {
                withMaven(maven:'maven_3_5_4') {
                    sh 'mvn install'
                }
            }
        }
    }
}

