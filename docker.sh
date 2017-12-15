#!/usr/bin/env bash
cd TestWarez/

./fabricMessage.sh


docker-compose up
BUILD_STATUS=$(docker-compose run -e SIGN_PROPERTIES=${SIGN_PROPERTIES} testwarez sh -c './gradlew clean build -x test crashlyticsUploadDistributionSignedReleaseProd --stacktrace')
echo -e "$BUILD_STATUS";
    if echo "$BUILD_STATUS" | grep -q "BUILD SUCCESSFUL"; then
        exit 0;
    else
        exit 1;
    fi
