
pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        IMAGE_NAME = 'campus-equipment-booking'
        DEPLOYMENT_NAME = 'campus-equipment-booking'
        CONTAINER_NAME = 'campus-equipment-booking'
    }

    stages {
        
        stage('Check Jenkins Environment') {
            steps {
                bat '''
                    whoami
                    git --version
                    java -version
                    mvn -version
                    docker --version
                    kubectl version --client
                    minikube status
                '''
            }
        }
        
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                bat 'mvn -B clean verify'
            }
        }

        stage('Build Docker Image') {
            steps {
                bat '''
                    docker build ^
                      -t campus-equipment-booking:%BUILD_NUMBER% ^
                      -t campus-equipment-booking:set-d .
                '''
            }
        }

        stage('Load Image into Minikube') {
            steps {
                bat '''
                    minikube status
                    minikube image load campus-equipment-booking:%BUILD_NUMBER%
                '''
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                bat '''
                    kubectl apply -f k8s\\deployment.yaml
                    kubectl apply -f k8s\\service.yaml

                    kubectl scale deployment/campus-equipment-booking --replicas=2

                    kubectl set image deployment/campus-equipment-booking ^
                      campus-equipment-booking=campus-equipment-booking:%BUILD_NUMBER%

                    kubectl rollout status deployment/campus-equipment-booking --timeout=180s
                    kubectl get deployments
                    kubectl get pods
                    kubectl get services
                '''
            }
        }

        stage('Health Check') {
            steps {
                bat '''
                    powershell -NoProfile -ExecutionPolicy Bypass -Command "$p=Start-Process kubectl -ArgumentList 'port-forward','service/campus-equipment-booking','18082:8080' -PassThru -NoNewWindow; try { $ok=$false; for($i=0; $i -lt 30; $i++){ try { $r=Invoke-RestMethod 'http://127.0.0.1:18082/actuator/health' -TimeoutSec 2; if($r.status -eq 'UP'){ $ok=$true; break } } catch {}; Start-Sleep -Seconds 2 }; if(-not $ok){ Write-Error 'Health check failed'; exit 1 }; Write-Output 'Health check passed: UP' } finally { Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue }"
                '''
            }
        }
    }

    post {
        success {
            echo 'CI/CD pipeline succeeded.'
        }
        failure {
            echo 'Pipeline failed. Check the stage logs before deploying again.'
        }
    }
}