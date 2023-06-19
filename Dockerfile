FROM openjdk:17-jdk
RUN useradd -ms /bin/bash jdkUsr
WORKDIR /scaler
COPY ./build/libs/scaler.jar .
RUN chown jdkUsr:jdkUsr scaler.jar
RUN chmod +x scaler.jar
USER jdkUsr
#CMD ["java", "-jar", "scaler.jar"]