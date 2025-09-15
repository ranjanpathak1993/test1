FROM tomcat:9.0-jdk11
RUN rm -rf /usr/local/tomcat/webapps/*
COPY target/sample-webapp-1.0.0.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
