
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
                    powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $p=$null; try { $p=Start-Process -FilePath 'kubectl' -ArgumentList @('port-forward','service/campus-equipment-booking','18082:8080') -PassThru -NoNewWindow; Start-Sleep -Seconds 3; $ok=$false; for($i=0; $i -lt 30; $i++){ if($p.HasExited){ throw 'kubectl port-forward exited unexpectedly' }; try { $r=Invoke-RestMethod -Uri 'http://127.0.0.1:18082/actuator/health' -TimeoutSec 3; if($r.status -eq 'UP'){ $ok=$true; break } } catch {}; Start-Sleep -Seconds 2 }; if(-not $ok){ throw 'Health check failed: application did not return UP' }; Write-Output 'Health check passed: UP' } finally { if($p -and -not $p.HasExited){ Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue } }"
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