# Estágio 1: Build (Compilação)
FROM maven:3.9.6-amazoncorretto-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Compila e cria o pacote. O '-DskipTests' agiliza o processo ignorando testes unitários no deploy
RUN mvn package -DskipTests

# Estágio 2: Run (Execução)
FROM amazoncorretto:17-alpine-jdk
WORKDIR /app
# Copia o JAR gerado no estágio anterior para a imagem final
COPY --from=build /app/target/*.jar app.jar

# Define a porta (Koyeb injeta a porta real na variável PORT)
ENV PORT=8080
EXPOSE 8080

# Comando para rodar
CMD ["java", "-jar", "app.jar"]
