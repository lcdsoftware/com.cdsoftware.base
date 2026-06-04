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
                    currentBuild.description = "${env.PLUGIN_NAME} - Build #${env.BUILD_NUMBER}"

                    echo "Descripción del build y del proyecto actualizada correctamente."
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