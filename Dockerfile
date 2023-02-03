FROM azul/zulu-openjdk-alpine:11
WORKDIR /root
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/root/app.jar"]