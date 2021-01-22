FROM maven:3.6.3-jdk-8

ARG COMMIT_ID

RUN pwd

RUN echo $COMMIT_ID
RUN ls -la
RUN cd .. && ls -la
RUN cd .. && ls -la
# Add build number
RUN sed -i "s/GIT_VERSION/1.0 build $COMMIT_ID/g" src/main/java/lv/div/jmssqlcli/Main.java

COPY ./ ./

RUN mvn clean package

ENTRYPOINT ["java", "-jar", "target/jmssqlcli.jar"]
