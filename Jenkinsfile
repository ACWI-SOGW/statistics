pipeline {
    agent any

    stages {
        stage('Git Clone') {
            steps {
                sh 'git clone git@github.com:ACWI-SOGW/statistics.git'
            }
        }

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

