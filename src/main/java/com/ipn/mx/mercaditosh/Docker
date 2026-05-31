# Etapa 1: Construcción (Build) usando Maven y JDK 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Compilamos el proyecto omitiendo los tests
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Run) usando JRE 21 para un contenedor más ligero
FROM eclipse-temurin:21-jre
WORKDIR /app
# Copiamos el .jar generado
COPY --from=build /app/target/*.jar app.jar
# Exponemos el puerto estándar
EXPOSE 8080
# Comando de inicio
ENTRYPOINT ["java", "-jar", "app.jar"]