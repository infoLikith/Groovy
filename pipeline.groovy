pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/javahometech/ai-leads'
            }
        }
        stage('Build') {
            steps {
                sh 'mvn clean install'
            }
        }
        stage('Shutdown Tomcat') {
            steps {
                script {
                    sshagent(['tomcat_ssh']) {
                        sh '''
                        ssh -o StrictHostKeyChecking=no your-tomcat-user@your-tomcat-server '
                        /path/to/tomcat/bin/shutdown.sh || true
                        '
                        '''
                    }
                }
            }
        }
        stage('Deploy') {
            steps {
                script {
                    sshagent(['tomcat_ssh']) {
                        sh '''
                        scp -o StrictHostKeyChecking=no target/*.war your-tomcat-user@your-tomcat-server:/path/to/tomcat/webapps/
                        '''
                    }
                }
            }
        }
        stage('Start Tomcat') {
            steps {
                script {
                    sshagent(['tomcat_ssh']) {
                        sh '''
                        ssh -o StrictHostKeyChecking=no your-tomcat-user@your-tomcat-server '
                        /path/to/tomcat/bin/startup.sh
                        '
                        '''
                    }
                }
            }
        }
    }
    post {
        always {
            cleanWs()
        }
    }
}
