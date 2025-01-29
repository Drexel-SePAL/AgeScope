# docker build -t agescope .
# docker run -it --tmpfs /mnt/ramdisk:rw,size=4g -v $PWD/sample/:/sample agescope

# Use an official Gradle image as the base image
FROM gradle:jdk21-jammy AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the project files
COPY . .

# Build the project and create a .jar file
RUN gradle build

# Use a lightweight base image for the final stage
FROM eclipse-temurin:21-jre

# Install dependencies
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    git \
    && rm -rf /var/lib/apt/lists/*

# Install Android SDK Command-Line Tools
ENV ANDROID_SDK_ROOT /opt/android-sdk
ENV PATH ${PATH}:${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools

RUN mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools \
    && cd ${ANDROID_SDK_ROOT}/cmdline-tools \
    && wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O commandlinetools.zip \
    && unzip commandlinetools.zip -d ${ANDROID_SDK_ROOT}/cmdline-tools \
    && mv ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest \
    && rm commandlinetools.zip

RUN yes | sdkmanager --licenses \
    && sdkmanager "platform-tools" "platforms;android-33" "build-tools;33.0.2"

# Set the working directory for the final image
WORKDIR /app

# Copy the jar file and execution script from the build stage
COPY --from=build /app/build/libs/*.jar app.jar
COPY --from=build /app/docker_run.sh docker_run.sh

# Execute
RUN chmod +x /app/docker_run.sh
CMD ["/app/docker_run.sh"]

