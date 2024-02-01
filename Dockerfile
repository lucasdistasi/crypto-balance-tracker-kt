FROM eclipse-temurin:21.0.2_13-jdk
RUN mkdir "/home/crypto-balance-tracker"
WORKDIR .
COPY /build/libs/crypto-balance-tracker.jar .
EXPOSE 8080
CMD ["java", "-jar", "crypto-balance-tracker.jar"]
