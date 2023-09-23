# for intel:
# FROM openjdk:17-alpine

# for M1 mac:
FROM bellsoft/liberica-openjdk-alpine-musl:17

ARG JAR_FILE=/build/libs/task-service-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} application.jar
#UseEpsilonGC:
#ENTRYPOINT ["java",  "-XX:MaxHeapSize=536870912","-XX:+UnlockExperimentalVMOptions", "-XX:+UseEpsilonGC","-XX:NativeMemoryTracking=summary","-jar", "application.jar"]
#UseSerialGC:
#ENTRYPOINT ["java", "-XX:+UseSerialGC","-XX:NativeMemoryTracking=summary","-jar", "application.jar"]
#UseG1GC:
#ENTRYPOINT ["java", "-XX:+UseG1GC","-XX:NativeMemoryTracking=summary","-jar", "application.jar"]

#UseZGC:
ENTRYPOINT ["java", "-Xmx538M","-XX:+UseZGC","-XX:NativeMemoryTracking=summary","-jar", "application.jar"]



