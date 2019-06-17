pipeline {
    agent any

    tools {
        maven 'maven_3_5_4'
        jdk 'jdk8'
    }
    environment {
        // requires the Pipeline Utility Steps plugin
        pom = readMavenPom file: 'pom.xml'
        pomVersion = pom.getVersion()
        pomArtifactId = pom.getArtifactId()
        //pomReleases = pom.getProperties().get("artifactory.releases")
        //pomSnapshots = pom.getProperties().get("artifactory.snapshots")
    }

    stages {
        stage('Reset Repo') {
            // it is not clear that the repo is a clean checkout
            steps {
                // remove potentially old release files if exist
                sh 'rm pom.xml.releaseBackup release.properties 2>/dev/null || true'
                // rest the git state (again, from failed release state)
                sh 'git checkout -f master'
                sh 'git reset --hard'
                sh 'git pull origin master'
            }
        }
        stage('Build Test') {
            steps {
                sh 'mvn clean package test'
            }
            post {
                success {
                    junit 'target/surefire-reports/**/*.xml' 
                }
            }
        }
        stage('Release') {
            // only release build when triggered
            when {
                expression{ params.RELEASE_BUILD }
            }
            steps {
                // incorporate the maven dryRun flag
                script {
                    if (params.DRY_RUN) {
                        dryRun='-DdryRun=true'
                    } else {
                        dryRun=''
                    }
                    releaseVersion = pomVersion.replace("-SNAPSHOT","")
                }
                // I want to retain this commented credential for documentation of how-to use sshagent in case we need it
                // this credential name must be defined in jenkins credentials config
                //sshagent(credentials : ['jenkins_git']) {
                //sshagent(credentials : ['a19251a9-ab43-4dd0-bd76-5b6dba9cd793']) {
                    // tests are run in prior tests
                    sh "mvn --batch-mode ${dryRun} -Dtag=${pomArtifactId}-${releaseVersion} release:prepare"
                    sh "mvn --batch-mode ${dryRun} release:perform"
                //}
            }
        }
        stage('Publish') {
            // only publish when NOT a dry run
            when {
                expression { params.DRY_RUN == false }
            }
            steps {
                script {
                    if (params.RELEASE_BUILD) {
                        repoId='-DrepositoryId=releases'
                    } else {
                        repoId='-DrepositoryId=snapshots'
                    }
                }
                // test complete in test stage
                sh 'mvn deploy -Dmaven.test.skip=true ${repoId}'
            }
        }
    }
}
