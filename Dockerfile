
# User should mount the directory as download destination onto 
# what configured as `chelidonium.baseDir` in the container when running this app.
# Example: (also check etc/runtime-config.yml)
# $ sudo docker run -v /file1:/download -p 9292:9292 -d chelidonium:2.0


FROM tomcat:8.5
LABEL maintainer="yangyd@live.com"

# Work dir for the embedded tomcat
VOLUME /tmp

EXPOSE 9292

ADD target/chelidonium-2.0.jar /app/chelidonium.jar
ADD etc/runtime-config.yml /app/config/application.yml

WORKDIR /app
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","chelidonium.jar"]
