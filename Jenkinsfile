pipeline {
    agent none
    environment {
        PLUGIN_NAME = "com.cdsoftware.base"
        IDEMPIERE_VERSION = "12.0.0"
    }
    stages {
        stage('Validate') {
            agent {
                docker {
                    image 'carl0jgr/idempiere-source-builder:12'
                    args '--entrypoint=\'\' -u root:root -v /var/jenkins_home/.m2:/m2 -e MAVEN_OPTS=-Dmaven.repo.local=/m2'
                }
            }
            steps {
                sh './ci/validate-branch.sh "${BRANCH_NAME}" "${CHANGE_BRANCH:-}" "${CHANGE_TARGET:-}"'
                sh './ci/validate-plugin.sh'
            }
        }
        stage('Compile') {
            agent {
                docker {
                    image 'carl0jgr/idempiere-source-builder:12'
                    args '--entrypoint=\'\' -u root:root -v /var/jenkins_home/.m2:/m2 -e MAVEN_OPTS=-Dmaven.repo.local=/m2'
                }
            }
            steps {
                dir('target-platform') {
                    git branch: '12.0', url: 'https://github.com/ingeint/idempiere-target-platform-plugin.git'
                    sh 'rm -rf target release'
                    sh './plugin-builder build ../${PLUGIN_NAME} ../${PLUGIN_NAME}.test'
                    sh '../ci/validate-artifacts.sh target "${BUILD_NUMBER}"'
                    sh '../ci/package-artifacts.sh target release "${BRANCH_NAME}" "${BUILD_NUMBER}"'
                    archiveArtifacts artifacts: 'release/*', fingerprint: true
                }
            }
            post {
                always {
                    script {
                        if (fileExists("${PLUGIN_NAME}.test/target/surefire-reports")) {
                            junit testResults: "${PLUGIN_NAME}.test/target/surefire-reports/*.xml", allowEmptyResults: false
                        } else {
                            echo 'Surefire reports were not produced because the build stopped before test execution.'
                        }
                    }
                }
                cleanup {
                    dir('target-platform') {
                        sh 'rm -rf target release ../${PLUGIN_NAME}/target ../${PLUGIN_NAME}.test/target'
                    }
                }
            }
        }
    }
}
