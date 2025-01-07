pipeline {
    agent any

     triggers {
          pollSCM('H/2 * * * *')
        }

    environment {
        GITHUB_TOKEN = credentials('github_manifest')
        DOCKERHUB_CREDENTIALS = credentials('enami_dockerhub')
        ARGO_CD_REPO = 'https://github.com/Khadija-Laamiri/e-banking-api-manifest.git'
        IMAGE_TAG = "v0.${BUILD_NUMBER}"
        APP_REGISTRY = "enamifatimazahrae2001/user"
        MANIFEST_FILE = "user-service/user-deployment.yml"
    }

    stages {
        stage('Build Artifact') {
            steps {
                script {
                    dir('user-service') {
                        bat "mvn clean install"
                    }
                }
            }
        }

        stage('Build App Image') {
            steps {
                script {
                    dir('user-service') {
                        dockerImage = docker.build("${APP_REGISTRY}:${IMAGE_TAG}")
                    }
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'enami_dockerhub',
                                                  usernameVariable: 'DOCKERHUB_CREDENTIALS_USR',
                                                  passwordVariable: 'DOCKERHUB_CREDENTIALS_PSW')]) {
                    echo "Docker Username: ${DOCKERHUB_CREDENTIALS_USR}"
                    writeFile file: 'docker_token.txt', text: "${DOCKERHUB_CREDENTIALS_PSW}"

                    bat """
                        docker login -u %DOCKERHUB_CREDENTIALS_USR% --password-stdin < docker_token.txt
                        docker push ${APP_REGISTRY}:${IMAGE_TAG}
                        docker rmi ${APP_REGISTRY}:${IMAGE_TAG} -f
                    """
                }
            }
        }

        stage('Update Argo CD Manifest') {
            steps {
                dir('..') {
                    script {
                        withCredentials([string(credentialsId: 'github_manifest', variable: 'GITHUB_TOKEN')]) {
                            bat """
                                git clone https://oauth2:${GITHUB_TOKEN}@https://github.com/Khadija-Laamiri/e-banking-api-manifest.git
                            """

                            dir('e-banking-api-manifest') {
                                bat """
                                    echo "Fichier manifest: ${MANIFEST_FILE}"
                                   powershell "(Get-Content -Path '${MANIFEST_FILE}') -replace 'enamifatimazahrae2001/user.*', 'enamifatimazahrae2001/discovery:${IMAGE_TAG}' | Set-Content -Path '${MANIFEST_FILE}'"
                                    git diff
                                    git add .
                                    git commit -m "Update user service image version to ${IMAGE_TAG}"
                                    git push origin master
                                """
                            }

                            bat "rmdir /S /Q e-banking-api-manifest"
                        }
                    }
                }
            }
        }
    }
}