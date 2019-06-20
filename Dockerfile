FROM java:8
VOLUME /tmp
EXPOSE 8095
ADD /build/libs/DeltaIoTLoopa2MAPEK.jar DeltaIoTLoopa2MAPEK.jar
ENTRYPOINT ["java","-jar","DeltaIoTLoopa2MAPEK.jar"]
