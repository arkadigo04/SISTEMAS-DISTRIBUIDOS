# Etapa 1: Compilación (Build)
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Compilamos el proyecto creando el .jar
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Run)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# Copiamos el .jar ya compilado de la etapa anterior
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# Arrancamos Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]