FROM maven:3.6.3-jdk-8

ARG COMMIT_ID

COPY ./ ./

# Add build number
RUN sed -i "s/GIT_VERSION/1.0 build $COMMIT_ID/g" src/main/java/lv/div/jmssqlcli/Main.java

RUN mvn clean package

RUN mkdir /input

ENTRYPOINT ["java", "-jar", "target/jmssqlcli.jar"]
