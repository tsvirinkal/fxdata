#!/bin/bash

# Check if version-tag argument is provided
if [ -z "$1" ]; then
    echo "Usage: $0 <version-tag>"
    exit 1
fi

VERSION_TAG=$1
APP_NAME="fxdata-api"
AUTHOR="tsvirinkal"
SEPARATOR="##############################################################################"
cd ../frontend
echo $SEPARATOR
echo "Building frontend..."
ng build --configuration=production
cd ../backend
echo
echo $SEPARATOR
echo "Building backend..."
mvn clean package -Drevision=$VERSION_TAG

echo
echo $SEPARATOR
echo "Building docker image..."

docker build -t $APP_NAME . || {
   echo "Docker build failed. Exiting..."
   exit 1
}

# Get the newest image ID
IMAGE_ID=$(docker images -q --filter "dangling=false" --format "{{.ID}}" | head -n 1)

# Check if IMAGE_ID is set and not empty
if [ -z "$IMAGE_ID" ]; then
    echo "No image found!"
    exit 1
fi

echo
echo $SEPARATOR
echo "Publishing.."
docker tag $IMAGE_ID $AUTHOR/$APP_NAME:$VERSION_TAG
docker push $AUTHOR/$APP_NAME:$VERSION_TAG

echo "$AUTHOR/$APP_NAME:$VERSION_TAG is successfully published!"

