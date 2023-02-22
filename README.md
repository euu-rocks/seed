# Seed

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



