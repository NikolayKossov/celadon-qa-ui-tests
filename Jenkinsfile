pipeline {
    agent { label 'docker' }
    options {
        timestamps()
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
        timeout(time: 20, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    stages {
        stage('Checkout') {
            steps {
                deleteDir()
                checkout scm
                script {
                    env.COMPOSE_PROJECT_NAME = "celadon-${env.BUILD_TAG}".toLowerCase().replaceAll('[^a-z0-9-]', '-')
                    env.LOCAL_UID = sh(script: 'id -u', returnStdout: true).trim()
                    env.LOCAL_GID = sh(script: 'id -g', returnStdout: true).trim()
                }
            }
        }
        stage('Docker UI tests') {
            steps {
                sh 'mkdir -p build && docker compose up --build --abort-on-container-exit --exit-code-from tests'
            }
        }
    }
    post {
        always {
            sh 'docker compose down --remove-orphans || true'
            junit allowEmptyResults: true, testResults: 'build/test-results/test/*.xml'
            archiveArtifacts allowEmptyArchive: true, artifacts: 'build/reports/**,build/allure-results/**'
            script {
                if (fileExists('build/allure-results')) {
                    allure results: [[path: 'build/allure-results']]
                }
            }
        }
    }
}
