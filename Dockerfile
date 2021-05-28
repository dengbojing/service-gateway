# FROM openjdk:11 
# COPY . .
# RUN  ./gradlew build -x test
# EXPOSE 8090
# CMD ["java","-jar","build/libs/service-gateway-0.0.1-SNAPSHOT.jar"]


FROM openjdk:11 AS build  
LABEL maintainer="dengbojing@qq.com"  
COPY . .
RUN ./gradlew build -x test  

FROM openjdk:11 AS final
WORKDIR /app
COPY --from=build build/libs/service-gateway-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8090  
ENTRYPOINT ["java","-jar","app.jar"] 