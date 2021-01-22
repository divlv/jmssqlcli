FROM maven:3.6.3-jdk-8

# Add build number
RUN COMMIT_ID=$(git rev-parse --short HEAD) && sed -i "s/GIT_VERSION/1.0 build ${COMMIT_ID}/g" src/main/java/lv/div/jmssqlcli/Main.java

COPY ./ ./

RUN mvn clean package

ENTRYPOINT ["java", "-jar", "target/jmssqlcli.jar"]
