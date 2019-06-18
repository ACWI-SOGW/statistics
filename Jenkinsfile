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
                
                // remove untracked files
                sh 'git clean -f'
                // remove local unstaged changes
                sh 'git checkout .'
                // remove staged changes
                sh 'git reset --hard HEAD'
                // make sure we are on a branch
                sh "git checkout -f master"
                // sh "git checkout -f ${scm.branches.[0].name}"
                // make sure local is up dated
                sh "git pull origin master"
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
        stage('Release Prepare') {
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
                            sh "mvn clean release:clean"
                            // tests are run in prior stage and batch-mode skips prompts
                            sh "mvn --batch-mode ${dryRun} -Dtag=${pomArtifactId}-${releaseVersion} release:prepare"
                        } catch (ex) {
                            // remove the tag if something went wrong
                            // sh "git tag -d ${pomArtifactId}-${releaseVersion}"
                            sh "mvn release:rollback"
                            throw ex
                        }
                    }
                }
            }
        }
        stage('Publish') {
            // only publish a SNAPSHOT when NOT a dry run 
            when { not { expression { params.DRY_RUN } } }
            steps {
                // tests are run in prior stage
                sh 'mvn deploy -Dmaven.test.skip=true ${repoId}'
            }
        }
        stage('Release Perform') {
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
    }
}
