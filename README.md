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
