# Use a base image with JDK (e.g., OpenJDK 17)
FROM openjdk:21-jdk-slim

# Set the working directory
WORKDIR /src

# Copy the JAR file to the container
COPY build/libs/fat.jar app.jar

# Expose the port your application runs on
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "app.jar"]
