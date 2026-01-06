pipeline {
    agent any

    tools {
        // Ces noms doivent correspondre EXACTEMENT à votre "Global Tool Configuration"
        maven 'Maven-3.9.1'
        jdk 'JDK17'
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
                // Utilisation de double %% pour échapper le caractère sous Windows bat
                bat 'git log -1 --pretty=format:"%%h - %%an, %%ar : %%s"'
            }
        }

        stage('Build') {
            steps {
                echo '🔨 Building the application...'
                bat 'mvn clean compile -B -DskipTests'
            }
        }

        stage('Unit Tests') {
            steps {
                echo '🧪 Running unit tests...'
                bat 'mvn test -B'
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
                bat 'mvn verify -B -DskipUnitTests'
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
                bat 'mvn jacoco:report'
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
                // Assurez-vous que le nom 'SonarQube' est configuré dans Manage Jenkins > System
                withSonarQubeEnv('SonarQube') {
                    bat "mvn sonar:sonar -Dsonar.projectKey=logismart-delivery-system -Dsonar.host.url=%SONAR_HOST_URL% -Dsonar.login=%SONAR_LOGIN%"
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
                bat 'mvn package -B -DskipTests'
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
                    bat "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                    bat "docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest"
                }
            }
        }

        stage('Security Scan') {
            steps {
                echo '🔒 Scanning Docker image for vulnerabilities...'
                script {
                    // Utilisation de || exit 0 pour ne pas bloquer si Trivy trouve des failles (optionnel)
                    bat "docker run --rm -v //var/run/docker.sock:/var/run/docker.sock aquasec/trivy image ${DOCKER_IMAGE}:${DOCKER_TAG}"
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
                    bat 'docker-compose -f docker-compose.ci.yml down || ver > nul'
                    bat 'docker-compose -f docker-compose.ci.yml up -d'
                }
            }
        }
    }

    post {
        always {
            echo '🧹 Cleaning up...'
            // cleanWs() est maintenant à l'intérieur du bloc pipeline/post, il trouvera le contexte.
            cleanWs()
        }
        success {
            echo '✅ Pipeline completed successfully!'
            emailext(
                subject: "✅ Build Success: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: "Le build Logismart est terminé avec succès.\nURL: ${env.BUILD_URL}",
                to: 'votre-email@exemple.com'
            )
        }
        failure {
            echo '❌ Pipeline failed!'
            emailext(
                subject: "❌ Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: "Le build Logismart a échoué. Vérifiez les logs sur: ${env.BUILD_URL}",
                to: 'votre-email@exemple.com'
            )
        }
    }
}