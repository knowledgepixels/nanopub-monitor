FROM eclipse-temurin:23 AS build

WORKDIR /app
COPY . .
RUN ./mvnw package -Dmaven.test.skip=true && \
    mv target/nanopub-monitor.war /app/nanopub-monitor.war

FROM tomcat:11.0.9
COPY --from=build /app/nanopub-monitor.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080