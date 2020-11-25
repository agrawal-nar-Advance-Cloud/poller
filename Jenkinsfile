pipeline {
    agent any

    stages {

       stage('Git Clone') {
          steps {
                checkout scm
            }
        }

       stage('Get last git commit') {
          steps {
                script {
                git_hash = sh(returnStdout: true, script: "git rev-parse HEAD").trim()
               }
            }
        }

        stage('Build JAR') {
            steps {
                sh "mvn clean install"
            }
        }

        stage('Build image') {
             steps {
                script {
                    dockerImage = docker.build ("${POLLER_IMAGE}")
                }

            }
        }

        stage('Registring image') {
            steps{
                script {
                    docker.withRegistry( '', 'dockerCred' ) {
                    dockerImage.push("${git_hash}")
                    dockerImage.push("latest")
                    }
                }
            }
        }

        stage('Write file myvalues.yaml') {
            steps{
                script {
                    writeFile file: 'helm/myvalues.yaml', text: "${POLLER_MYVALUES}"
                    sh "cat ./helm/myvalues.yaml"
                }
            }
        }

        stage('Get cluster info') {
            steps{
                script {
                    withKubeConfig([credentialsId: 'kubernetesCred',serverUrl: "${ServerUrl}"]) {
                        sh "kubectl  cluster-info"
                    }
                }
            }
        }

        stage('Helm upgrade') {
            steps{
                script {
                    withKubeConfig([credentialsId: 'kubernetesCred',serverUrl: "${ServerUrl}"]) {
                        sh "helm upgrade poller -n api ./helm/ -f ./helm/myvalues.yaml"
                    }
                }
            }
        }

    }
}