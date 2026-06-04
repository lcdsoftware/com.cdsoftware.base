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
                    <b>Plugin:</b> ${env.PLUGIN_NAME}<br/>
                    <b>iDempiere:</b> ${env.IDEMPIERE_VERSION}<br/>
                    <b>Branch:</b> ${env.BRANCH_NAME ?: 'N/A'}<br/>
                    <b>Job:</b> ${env.JOB_NAME}<br/>
        """

                    def mainDescription = """
                    <b>Plugin:</b> ${env.PLUGIN_NAME}<br/>
                    <b>iDempiere:</b> ${env.IDEMPIERE_VERSION}<br/>
                    <b>Repositorio:</b> Bitbucket<br/>
                    <b>Última rama ejecutada:</b> ${env.BRANCH_NAME}<br/>
                    <b>Último build:</b> #${env.BUILD_NUMBER}<br/>
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