# Seed

## How to run

### Try with DOCKER 

docker build -t registry.digitalocean.com/euu/seed .

docker run --env "SPRING_DATASOURCE_URL=jdbc:postgresql://192.168.69.135:5432/seed"  registry.digitalocean.com/euu/seed


### Try with Springboot

mvn spring-boot:run -Dspring-boot.run.arguments=--spring.datasource.url=jdbc:postgresql://127.0.0.1:5432/seed


### Keep license headers up to date before commit

mvn license:check

mvn license:format


