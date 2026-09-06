FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
ENV GRADLE_USER_HOME=/tmp/gradle-cache
COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle
RUN sed -i 's/\r$//' gradlew && sh gradlew --version --no-daemon && chmod -R a+rwX /tmp/gradle-cache
COPY src ./src
COPY docker/run-tests.sh /app/run-tests.sh
RUN sed -i 's/\r$//' /app/run-tests.sh && chmod -R a+rX /app
ENTRYPOINT ["sh", "/app/run-tests.sh"]
