FROM openjdk:8
ENV ARTIFACTID xc-service-search
ENV ARTIFACTVERSION 1.0-SNAPSHOT
ENV HOME_PATH /home
WORKDIR $HOME_PATH
ADD /$ARTIFACTID-$ARTIFACTVERSION.jar $HOME_PATH/$ARTIFACTID.jar
ENTRYPOINT ["java", "-jar", "xc-service-search.jar"]