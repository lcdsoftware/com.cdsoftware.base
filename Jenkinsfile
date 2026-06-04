pipeline {
    agent none

    environment {
        PLUGIN_NAME = "com.cdsoftware.base"
        IDEMPIERE_VERSION = "12.0.0"
    }

    stages {
        stage('Init') {
            agent any
            steps {
                script {
                    def branchJob = currentBuild.rawBuild.getParent()
                    def multibranchJob = branchJob.getParent()

                    def branchDescription = """
        Plugin: ${env.PLUGIN_NAME}
        iDempiere: ${env.IDEMPIERE_VERSION}
        Branch: ${env.BRANCH_NAME}
        Job: ${env.JOB_NAME}
        """

                    def mainDescription = """
        Plugin: ${env.PLUGIN_NAME}
        iDempiere: ${env.IDEMPIERE_VERSION}
        Repositorio: Bitbucket
        Última rama ejecutada: ${env.BRANCH_NAME}
        Último build: #${env.BUILD_NUMBER}
        """

                    branchJob.setDescription(branchDescription)
                    multibranchJob.setDescription(mainDescription)

                    currentBuild.description = "${env.PLUGIN_NAME}-${env.IDEMPIERE_VERSION}.${env.BUILD_NUMBER}"

                    echo "Descripción del build, rama y multibranch actualizadas."
                }
            }
        }

        stage('Compile') {
            agent {
                docker {
                    image 'carl0jgr/idempiere-source-builder:12'
                    args '--entrypoint=\'\' -u root:root -v /var/jenkins_home/.m2:/root/.m2'
                }
            }

            steps {
                dir('target-platform') {
                    git branch: '12.0', url: 'https://github.com/ingeint/idempiere-target-platform-plugin.git'

                    sh './plugin-builder build ../${PLUGIN_NAME} ../${PLUGIN_NAME}.test'

                    archiveArtifacts artifacts: "target/${PLUGIN_NAME}-${IDEMPIERE_VERSION}.${BUILD_NUMBER}.jar", fingerprint: true

                    sh 'rm -rf target ../${PLUGIN_NAME}/target ../${PLUGIN_NAME}.test/target'
                }
            }
        }
    }
}