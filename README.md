# Seed

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)
![Build and Deploy](https://github.com/euu-rocks/seed/actions/workflows/build-and-push-main.yml/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=euu-rocks_seed&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=euu-rocks_seed)
[![Javadoc](https://img.shields.io/badge/JavaDoc-Online-green)](https://euu-rocks.github.io/seed/javadoc/)

## How to run

### Try with DOCKER 
```
docker build -t euurocks/seed .

docker run --env "SPRING_DATASOURCE_URL=jdbc:postgresql://192.168.69.135:5432/seed" euurocks/seed
```
> Note:
>
> 192.168.69.135 is your current IP of your running database service


### Try with Springboot
```
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.datasource.url=jdbc:postgresql://127.0.0.1:5432/seed
```
> Note: 
> 
> 127.0.0.1 is your current IP of your running database service

### Try with Docker Compose
```
docker-compose up
```


### Keep license headers up to date before commit

mvn license:check

mvn license:format



