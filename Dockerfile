FROM maven:3.6.3-jdk-8

COPY ./ ./

RUN mvn clean package

CMD ["java", "-jar", "target/jmssqlcli.jar"]
