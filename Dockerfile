FROM maven:3.9-eclipse-temurin-17

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

EXPOSE 4567

CMD ["java", "-cp", "target/classes:target/dependency/*", "ngordnet.main.Main"]