FROM maven:3.9.9-eclipse-temurin-23 as build

# Copy source for build:
COPY src /nanopub-monitor/src
COPY pom.xml /nanopub-monitor

# Build with maven:
RUN mvn -f /nanopub-monitor/pom.xml clean package

# Pull base image:
FROM tomcat:11.0

# Remove default webapps:
RUN rm -fr /usr/local/tomcat/webapps/*

# Copy target from build stage
COPY --from=build /nanopub-monitor/target/nanopub-monitor /usr/local/tomcat/nanopub-monitor/target/nanopub-monitor

COPY scripts /usr/local/tomcat/nanopub-monitor/scripts
RUN ln -s /usr/local/tomcat/nanopub-monitor/target/nanopub-monitor /usr/local/tomcat/webapps/ROOT

EXPOSE 8080

CMD ["catalina.sh", "run"]
