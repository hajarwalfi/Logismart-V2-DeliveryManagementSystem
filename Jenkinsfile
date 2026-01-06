pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }

    environment {
        // SonarQube configuration
        SONAR_HOST_URL = 'http://localhost:9000'
        SONAR_LOGIN = credentials('sonarqube-token')

        // Docker configuration
        DOCKER_IMAGE = 'logismart/delivery-system'
        DOCKER_TAG = "${env.BUILD_NUMBER}"

        // Database test configuration
        DB_URL = 'jdbc:postgresql://localhost:5432/logismart_test'
        DB_USER = 'logismart_test'
        DB_PASSWORD = credentials('db-test-password')

        // JWT for tests
        JWT_SECRET = credentials('jwt-secret-test')
    }

    stages {
        stage('Checkout') {
            steps {
                echo '🔄 Checking out source code...'
                checkout scm
                sh 'git log -1 --pretty=format:"%h - %an, %ar : %s"'
            }
        }

        stage('Build') {
            steps {
                echo '🔨 Building the application...'
                sh '''
                    mvn clean compile -B -DskipTests
                '''
            }
        }

        stage('Unit Tests') {
            steps {
                echo '🧪 Running unit tests...'
                sh '''
                    mvn test -B
                '''
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Integration Tests') {
            steps {
                echo '🔗 Running integration tests...'
                sh '''
                    mvn verify -B -DskipUnitTests
                '''
            }
            post {
                always {
                    junit '**/target/failsafe-reports/*.xml'
                }
            }
        }

        stage('Code Coverage') {
            steps {
                echo '📊 Generating code coverage reports...'
                sh '''
                    mvn jacoco:report
                '''
            }
            post {
                always {
                    jacoco(
                        execPattern: '**/target/jacoco.exec',
                        classPattern: '**/target/classes',
                        sourcePattern: '**/src/main/java',
                        exclusionPattern: '**/dto/**,**/entity/**,**/config/**,**/exception/**,**/*Application.java,**/mapper/*Impl.java'
                    )
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                echo '🔍 Running SonarQube analysis...'
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        mvn sonar:sonar \
                            -Dsonar.projectKey=logismart-delivery-system \
                            -Dsonar.host.url=${SONAR_HOST_URL} \
                            -Dsonar.login=${SONAR_LOGIN}
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                echo '⏳ Waiting for Quality Gate...'
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Package') {
            steps {
                echo '📦 Packaging application...'
                sh '''
                    mvn package -B -DskipTests
                '''
            }
            post {
                success {
                    archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                echo '🐳 Building Docker image...'
                script {
                    sh """
                        docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
                        docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest
                    """
                }
            }
        }

        stage('Security Scan') {
            steps {
                echo '🔒 Scanning Docker image for vulnerabilities...'
                script {
                    sh """
                        docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
                            aquasec/trivy image ${DOCKER_IMAGE}:${DOCKER_TAG} || true
                    """
                }
            }
        }

        stage('Deploy to Staging') {
            when {
                branch 'main'
            }
            steps {
                echo '🚀 Deploying to staging environment...'
                script {
                    sh '''
                        docker-compose -f docker-compose.ci.yml down || true
                        docker-compose -f docker-compose.ci.yml up -d
                    '''
                }
            }
        }
    }

    post {
        always {
            echo '🧹 Cleaning up...'
            cleanWs()
        }
        success {
            echo '✅ Pipeline completed successfully!'
            emailext(
                subject: "✅ Build Success: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
                    Build succeeded!

                    Job: ${env.JOB_NAME}
                    Build Number: ${env.BUILD_NUMBER}
                    Build URL: ${env.BUILD_URL}

                    Check console output for details.
                """,
                to: '${DEFAULT_RECIPIENTS}'
            )
        }
        failure {
            echo '❌ Pipeline failed!'
            emailext(
                subject: "❌ Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
                    Build failed!

                    Job: ${env.JOB_NAME}
                    Build Number: ${env.BUILD_NUMBER}
                    Build URL: ${env.BUILD_URL}

                    Check console output for details.
                """,
                to: '${DEFAULT_RECIPIENTS}'
            )
        }
    }
}