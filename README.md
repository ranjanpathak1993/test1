# sample-webapp

Minimal Spring Boot webapp packaged as WAR, with Jenkinsfile and Dockerfile.

## Quick local build

```bash
mvn -B clean package
docker build -t sample-webapp:local .
docker run -p 8080:8080 sample-webapp:local
```
