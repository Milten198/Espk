FROM ubuntu:16.04

# based on work Jacek Marchwicki
MAINTAINER Bartłomiej Hołota "bartekholota@gmail.com"
RUN apt-get update && apt-get upgrade --assume-yes --with-new-pkgs
# Install java8
RUN apt-get install -y software-properties-common && add-apt-repository -y ppa:webupd8team/java && apt-key update && apt-get update
RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
RUN apt-get install -y oracle-java8-installer

# Install Deps
RUN dpkg --add-architecture i386  && apt-get update && apt-get install -y \
 curl \
 expect \
 git \
 lib32gcc1 \
 lib32ncurses5 \
 lib32stdc++6 \
 lib32z1 \
 libc6-i386 \
 python \
 wget \
 unzip

# Install Android SDK
RUN cd /opt \
    && wget --output-document=android-sdk.zip --quiet https://dl.google.com/android/repository/sdk-tools-linux-3859397.zip \
    && mkdir android-sdk \
    && unzip -q android-sdk.zip -d android-sdk \
    && rm android-sdk.zip \
    && chown -R root.root android-sdk

# Setup environment
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle
ENV ANDROID_HOME /opt/android-sdk
ENV PATH ${PATH}:${ANDROID_HOME}/tools:${ANDROID_HOME}/tools/bin:${ANDROID_HOME}/platform-tools

# Install sdk elements
RUN mkdir ~/.android
RUN echo '' > ~/.android/repositories.cfg
RUN echo 'y' | sdkmanager 'build-tools;26.0.0'
RUN echo 'y' | sdkmanager 'platforms;android-26'
RUN echo 'y' | sdkmanager 'platform-tools'
RUN echo 'y' | sdkmanager 'extras;android;m2repository'
RUN echo 'y' | sdkmanager 'extras;google;m2repository'
RUN echo 'y' | sdkmanager 'extras;google;google_play_services'
RUN (while true; do echo 'y'; sleep 2; done) | sdkmanager --licenses

# Cleaning
RUN apt-get clean
RUN groupadd docker
RUN usermod -aG docker root

# GO to workspace
RUN mkdir -p /opt/workspace
WORKDIR /opt/workspace
