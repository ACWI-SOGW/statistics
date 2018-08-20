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
		//pomReleases = pom.getProperties().get("artifactory.releases")
		//pomSnapshots = pom.getProperties().get("artifactory.snapshots")
	}

	stages {
		stage('Package') {
			steps {
				// differ testing to test stage
				sh 'mvn clean package -Dmaven.test.skip=true'
			}
		}
		stage('Test') {
			// unit test and publish results
			steps {
				sh 'mvn test -Dmaven.javadoc.skip=true'
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
				}
				// tests are run in prior tests
				sh 'mvn --batch-mode $dryRun -Dmaven.test.skip=true release:prepare release:perform'
			}
		}
		stage('Publish') {
			// only pulish when NOT a dry run
			when {
				expression { params.DRY_RUN == false }
			}
			steps {
				// test complete in test stage
				sh 'mvn deploy -Dmaven.test.skip=true'
			}
		}
	}
}
