# Campus Equipment Booking

A Spring Boot 3.3.5 application built with Java 17. This lab change validates booking capacity boundaries and borrower identifiers, then packages and deploys the application with Docker and Kubernetes.

## Assigned change request

- **Capacity validation:** Correct the boundary condition so a booking at the allowed capacity is handled correctly and a booking beyond the available capacity is rejected.
- **Borrower identifier validation:** Reject borrower IDs that are missing, empty, or contain only whitespace.
- **Tests:** Cover the capacity boundary and invalid borrower-ID cases, including valid input.

## Prerequisites

JDK 17, Maven, Docker Desktop, Minikube, `kubectl`, and Git. Run commands from the project root containing `pom.xml`. Examples use Windows CMD.

## Build and test

```bat
mvn -version
java -version
mvn clean test
mvn clean verify
```

The packaged JAR is expected at `target\campus-equipment-booking-1.0.0.jar`. `mvn clean verify` is the CI pipeline command.

## Docker build and run

```bat
mvn clean package
docker build -t campus-equipment-booking:set-d .
docker images campus-equipment-booking
docker run --name campus-equipment-booking-local -d -p 8081:8080 campus-equipment-booking:set-d
docker ps --filter "name=campus-equipment-booking-local"
curl http://127.0.0.1:8081/actuator/health
```

Host port `8081` is used because Jenkins may already occupy host port `8080`. Expected health response: `{"status":"UP"}`.

If the local container name is already taken, remove it first with `docker rm -f campus-equipment-booking-local`.

## Kubernetes deployment (Minikube)

```bat
minikube start
minikube image load campus-equipment-booking:set-d
kubectl apply -f k8s\deployment.yaml
kubectl apply -f k8s\service.yaml
kubectl scale deployment/campus-equipment-booking --replicas=2
kubectl rollout status deployment/campus-equipment-booking --timeout=180s
kubectl get deployments
kubectl get pods -o wide
kubectl get services
kubectl get endpoints campus-equipment-booking
```

The Deployment should show **2/2 available replicas**. The Service is `ClusterIP`; access it from the host with port-forwarding.

Terminal 1:
```bat
kubectl port-forward service/campus-equipment-booking 18082:8080
```

Terminal 2:
```bat
curl http://127.0.0.1:18082/actuator/health
```

Expected response: `{"status":"UP"}`. Stop port-forwarding with `Ctrl+C`.

## Evidence for submission

### Git history and status
```bat
git branch --show-current
git log -5 --oneline --decorate
git status --short
git diff --stat
```

The feature branch for this change is `feature/borrower-validation`. Ensure the changes are committed and the working tree is clean before submission.

### Maven test results
```bat
mvn clean verify
```
Capture the final summary showing the test count and `BUILD SUCCESS`.

### Docker verification
```bat
docker images campus-equipment-booking
docker ps -a --filter "name=campus-equipment-booking"
docker inspect campus-equipment-booking-local --format "{{.State.Status}}"
curl http://127.0.0.1:8081/actuator/health
```

If you did not run a local Docker container, include the image listing and Kubernetes evidence instead.

### Kubernetes and health check
```bat
kubectl get deployments
kubectl get pods -o wide
kubectl get services
kubectl get endpoints campus-equipment-booking
kubectl rollout status deployment/campus-equipment-booking
```
Also capture the actual response from `curl http://127.0.0.1:18082/actuator/health` while port-forwarding is running.

## Results previously recorded during lab work

- Maven packaging/test run completed successfully: **13 tests, 0 failures, 0 errors, 0 skipped**.
- Kubernetes Deployment was observed at **2/2 available replicas**.
- Both application Pods were `Running` and `1/1 Ready`, with **0 restarts**.
- Service endpoints were `10.244.0.8:8080` and `10.244.0.9:8080`.
- Application logs showed Spring Boot started and Tomcat listening on port `8080`.
- Jenkins' health-check stage previously failed while port-forwarding. Re-run the corrected health check and record its successful response before marking the pipeline health check as passed.

## Health endpoint configuration

`src\main\resources\application.properties` should include:

```properties
server.port=8080
management.endpoints.web.exposure.include=health
management.endpoint.health.probes.enabled=true
```

# Campus Equipment Booking — Jenkins CI/CD Pipeline

A Spring Boot 3.3.5 application built with Java 17. The lab change validates booking capacity boundaries and borrower identifiers, then builds, tests, packages, containerizes, and deploys the application to Kubernetes through Jenkins.

## 1. Assigned change request

- **Capacity validation:** Handle the allowed capacity boundary consistently and reject bookings beyond available capacity.
- **Borrower identifier validation:** Reject borrower IDs that are missing, empty, or whitespace-only.
- **Regression tests:** Cover the capacity boundary, over-capacity case, invalid borrower IDs, and valid input.

## 2. Pipeline overview

The `Jenkinsfile` should automate the following stages:

1. **Checkout** — retrieve the source from Git.
2. **Build and test** — run `mvn -B clean verify`; fail the pipeline if tests fail.
3. **Build Docker image** — build the image and tag it with the Jenkins build number. Also create the lab-required `campus-equipment-booking:set-d` tag.
4. **Load image into Minikube** — make the locally built image available to the cluster.
5. **Deploy to Kubernetes** — apply the Deployment and Service manifests.
6. **Rollout verification** — set the Deployment image to the current build and wait for the rollout.
7. **Health check** — port-forward the Kubernetes Service and retry `http://127.0.0.1:18082/actuator/health` until it returns `{"status":"UP"}` or the check times out.

A successful manual health check confirms the Kubernetes Service and application endpoint work from the interactive terminal. The Jenkins health-check stage must still pass in a fresh pipeline run before the complete CI/CD pipeline can be reported as successful.

## 3. Prerequisites and Jenkins configuration

Install/configure these on the Windows machine running Jenkins:

- JDK 17 and Maven
- Git
- Docker Desktop, with the Docker engine running
- Minikube and `kubectl`
- A running Minikube profile using the Docker driver (or adapt the pipeline to your cluster)
- Jenkins job configured to use this repository and its `Jenkinsfile`

The Jenkins service account must be able to run `git`, `mvn`, `docker`, `minikube`, and `kubectl`, and must use the intended Minikube/Kubernetes context. Verify this under **Windows Services → Jenkins → Properties → Log On** and by checking the tools/context from the Jenkins job environment. Do not assume that a command working in your personal terminal will also work under the Jenkins service account.

The sample pipeline below assumes the repository is checked out by Jenkins and contains:
- `pom.xml`
- `Dockerfile`
- `k8s\deployment.yaml`
- `k8s\service.yaml`

If the repository is private, configure Jenkins Git credentials in the job or Jenkins Credentials. Do not put access tokens or passwords in the `Jenkinsfile`.

## 4. Example Jenkinsfile (Windows agent)

Save the following as `Jenkinsfile` in the repository root. Adjust the image name, manifest paths, and tool configuration if your project differs.

```groovy
pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        APP_IMAGE = 'campus-equipment-booking'
        K8S_DEPLOYMENT = 'campus-equipment-booking'
        K8S_SERVICE = 'campus-equipment-booking'
        HEALTH_PORT = '18082'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                bat 'git status --short'
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
                    docker build -t %APP_IMAGE%:%BUILD_NUMBER% .
                    docker tag %APP_IMAGE%:%BUILD_NUMBER% %APP_IMAGE%:set-d
                    docker images %APP_IMAGE%
                '''
            }
        }

        stage('Load Image into Minikube') {
            steps {
                bat '''
                    minikube status
                    minikube image load %APP_IMAGE%:%BUILD_NUMBER%
                '''
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                bat '''
                    kubectl apply -f k8s\\deployment.yaml
                    kubectl apply -f k8s\\service.yaml
                    kubectl set image deployment/%K8S_DEPLOYMENT% %K8S_DEPLOYMENT%=%APP_IMAGE%:%BUILD_NUMBER%
                    kubectl scale deployment/%K8S_DEPLOYMENT% --replicas=2
                    kubectl rollout status deployment/%K8S_DEPLOYMENT% --timeout=180s
                    kubectl get deployments
                    kubectl get pods -o wide
                    kubectl get services
                '''
            }
        }

        stage('Health Check') {
            steps {
                bat '''
                    powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $p=$null; try { $p=Start-Process -FilePath 'kubectl' -ArgumentList @('port-forward','service/%K8S_SERVICE%','%HEALTH_PORT%:8080') -PassThru -NoNewWindow; Start-Sleep -Seconds 3; $ok=$false; for($i=0; $i -lt 30; $i++){ if($p.HasExited){ throw 'kubectl port-forward exited unexpectedly' }; try { $r=Invoke-RestMethod -Uri 'http://127.0.0.1:%HEALTH_PORT%/actuator/health' -TimeoutSec 3; if($r.status -eq 'UP'){ $ok=$true; break } } catch {}; Start-Sleep -Seconds 2 }; if(-not $ok){ throw 'Health check failed: application did not return UP' }; Write-Output 'Health check passed: UP' } finally { if($p -and -not $p.HasExited){ Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue } }"
                '''
            }
        }
    }

    post {
        always {
            bat 'kubectl get pods -o wide'
        }
        success {
            echo 'CI/CD pipeline completed successfully.'
        }
        failure {
            echo 'CI/CD pipeline failed. Review the first failed stage and its console output.'
        }
    }
}
```

### Important Jenkinsfile notes

- The pipeline is for a **Windows Jenkins agent** because it uses `bat` and PowerShell.
- The Kubernetes Deployment's container name is assumed to be `campus-equipment-booking`. If the `containers[].name` in `k8s/deployment.yaml` differs, update the container name in `kubectl set image`.
- `imagePullPolicy: IfNotPresent` is appropriate for a locally loaded Minikube image. Ensure the manifest does not force a registry pull for this image.
- The health check uses port `18082` on the Jenkins host to avoid clashing with Jenkins on host port `8080`. Ensure port `18082` is not already occupied.
- If `kubectl` or Minikube works in your interactive session but not in Jenkins, configure the Jenkins service account's PATH, Docker access, and Kubernetes context. Do not mark the pipeline successful until Jenkins itself passes the health check.
- This example deploys a local image to Minikube; pushing to a remote registry requires registry credentials and a corresponding push/pull workflow.

## 5. Local build and test commands

Run these from the project root containing `pom.xml`:

```bat
java -version
mvn -version
mvn -B clean verify
```

The expected packaged JAR is `target\\campus-equipment-booking-1.0.0.jar`. The Jenkins build stage uses `mvn -B clean verify`, which compiles, tests, and runs Maven verification goals.

## 6. Docker commands for local verification

These are optional manual checks; Jenkins performs the image build automatically.

```bat
docker build -t campus-equipment-booking:set-d .
docker images campus-equipment-booking
docker run --name campus-equipment-booking-local -d -p 8081:8080 campus-equipment-booking:set-d
curl http://127.0.0.1:8081/actuator/health
```

Expected health response:

```json
{"status":"UP"}
```

Host port `8081` is used to avoid a possible conflict with Jenkins on host port `8080`. If the container name already exists, remove it before recreating it:

```bat
docker rm -f campus-equipment-booking-local
```

## 7. Kubernetes manual verification

The Jenkins pipeline applies the manifests, loads the image into Minikube, scales the Deployment to two replicas, and waits for rollout. For manual diagnosis:

```bat
minikube status
kubectl get deployments
kubectl get pods -o wide
kubectl get services
kubectl get endpoints campus-equipment-booking
kubectl rollout status deployment/campus-equipment-booking --timeout=180s
```

The expected Deployment status is **2/2 available replicas**.

For a manual health check, use two terminals.

Terminal 1:

```bat
kubectl port-forward service/campus-equipment-booking 18082:8080
```

Keep this terminal open. Terminal 2:

```bat
curl http://127.0.0.1:18082/actuator/health
```

Expected response:

```json
{"status":"UP"}
```

Stop the port-forward with `Ctrl+C`.

## 8. Health endpoint configuration

Confirm `src\\main\\resources\\application.properties` contains:

```properties
server.port=8080
management.endpoints.web.exposure.include=health
management.endpoint.health.probes.enabled=true
```

## 9. Evidence to capture for submission

### Git

```bat
git branch --show-current
git log -5 --oneline --decorate
git status --short
git diff --stat
```

### Maven

Run `mvn -B clean verify` and capture the final test summary and `BUILD SUCCESS`.

### Docker

```bat
docker images campus-equipment-booking
docker ps -a --filter "name=campus-equipment-booking"
```

### Kubernetes

```bat
kubectl get deployments
kubectl get pods -o wide
kubectl get services
kubectl get endpoints campus-equipment-booking
kubectl rollout status deployment/campus-equipment-booking --timeout=180s
```

### Health check and Jenkins

Capture:
- The manual health response from `http://127.0.0.1:18082/actuator/health`.
- Jenkins Console Output showing each stage and the final build result.
- The Health Check stage output containing `Health check passed: UP`.
- The test report generated by Maven/Jenkins, if test-result publishing is configured.

**Current recorded manual result:** the user confirmed that the Kubernetes health endpoint returned `status UP`. The Jenkins health-check stage had previously failed during port-forwarding. Update the Jenkins result only after rerunning the pipeline and observing its actual outcome.

## 10. Previously observed results (historical; verify with a fresh run)

- Maven verification previously completed with **13 tests, 0 failures, 0 errors, 0 skipped**.
- A Kubernetes snapshot showed **2/2 available replicas** and two Pods `Running`, `1/1 Ready`, with zero restarts.
- The observed Service endpoints were `10.244.0.8:8080` and `10.244.0.9:8080`.
- Spring Boot logs showed Tomcat listening on port `8080`.
- A later manual health check returned `status UP`.
- Jenkins' Health Check stage previously failed. Do not report the full Jenkins pipeline as successful until a new run finishes successfully.

## 11. Commit the documentation and pipeline

After adding or updating `README.md` and `Jenkinsfile` in your repository:

```bat
git add README.md Jenkinsfile
git commit -m "ci: document Jenkins CI/CD pipeline"
git status --short
```

If `Jenkinsfile` is already present, review it and merge the applicable changes rather than overwriting working project-specific logic.
