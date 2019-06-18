pipeline {
    agent any

    tools {
        maven 'maven_3_5_4'
        jdk 'jdk8'
    }
    environment {
        // requires the Pipeline Utility Steps plugin to read pom file
        pom = readMavenPom file: 'pom.xml'
        pomVersion = pom.getVersion()
        pomArtifactId = pom.getArtifactId()
        
        // this is how to read properties. while no longer needed - retained for documentation
        //pomReleases = pom.getProperties().get("artifactory.releases")
        //pomSnapshots = pom.getProperties().get("artifactory.snapshots")
        
        // incorporate the maven dryRun flag
        dryRun="${ (params.DRY_RUN) ? '-DdryRun=true' : ' '}"

        // ensure that release tag is clean
        releaseVersion = pomVersion.replace("-SNAPSHOT","")
        
        // use the release flag to specify the deploy repository
        repoId='-DrepositoryId=' + "${ (params.RELEASE_BUILD) ? 'releases' : 'snapshots'}"
    }

    stages {
        stage('Reset Repo') {
            // it is not clear that the repo is a clean checkout
            steps {
                // remove potentially old release files if exist
                sh 'rm pom.xml.releaseBackup release.properties 2>/dev/null || true'
                // rest the git state (again, from failed prior job run)
                // sh "git checkout -f ${scm.branches.[0].name}"
                sh 'git reset --hard'
                sh "git checkout -f master"
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
                // if the default .ssh/conf public key works, then this agent is unnecessary.
                // this sshagent credential must be defined in jenkins credentials config
                sshagent(credentials : ['a19251a9-ab43-4dd0-bd76-5b6dba9cd793']) {
                    script {
                        try {
                            // tests are run in prior stage and batch-mode skips prompts
                            sh "mvn --batch-mode ${dryRun} -Dtag=${pomArtifactId}-${releaseVersion} release:prepare"
                            // release perform might deploy artifacts
                            // only publish a RELEASE when NOT a dry run 
                            // if ( ! params.DRY_RUN  ) {
                            //    sh "mvn deploy -Dmaven.test.skip=true ${repoId}"
                            // }
                            sh "mvn --batch-mode ${dryRun} release:perform"
                        } catch (ex) {
                            // remove the tag if something went wrong
                            // sh "git tag -d ${pomArtifactId}-${releaseVersion}"
                            sh "mvn release:rollback"
                            throw ex
                        } finally {
                            sh "mvn release:clean"
                        }
                    }
                }
            }
        }
        stage('Publish') {
            // only publish when NOT a dry run
            when {
                expression { params.DRY_RUN == false }
            }
            steps {
                // test complete in test stage
                sh 'mvn deploy -Dmaven.test.skip=true ${repoId}'
            }
        }
    }
}
