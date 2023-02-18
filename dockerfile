FROM openjdk:11
COPY target/setup-0.0.1-SNAPSHOT.jar setup.jar
ENTRYPOINT ["java","-jar","setup.jar"]