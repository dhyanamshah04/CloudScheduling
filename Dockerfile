FROM eclipse-temurin:8-jdk

WORKDIR /app

COPY src /app/src

RUN javac -d /app/classes /app/src/*.java

CMD ["java", "-cp", "/app/classes", "Main"]