#!/usr/bin/env pwsh

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
$PSDefaultParameterValues["*:ErrorAction"] = "Stop"

$docker_network = "sitesearch"

$Env:SPRING_CONFIG_NAME = "application, prod"
#./gradlew clean build --info -x test
./gradlew build --no-rebuild --build-cache --info -x test

#$DOCKER_IMAGE_NAME = (Get-ChildItem  service/build/libs/*.jar).BaseName
$DOCKER_IMAGE_NAME = "if-sitesearch"
$DOCKER_TAG = "latest"

mkdir ~/srv/${DOCKER_IMAGE_NAME}
sudo chown -R 1000:1000 ~/srv/${DOCKER_IMAGE_NAME} # make it a svc_usr' directory
sudo chmod -R 744 ~/srv/${DOCKER_IMAGE_NAME}

# TODO enable b/g deployment, or at least, reduce probability of failure 
cd service
docker build --tag intrafind/${DOCKER_IMAGE_NAME}:${DOCKER_TAG} .
docker rm -f ${DOCKER_IMAGE_NAME}
#    -v ~/srv/${DOCKER_IMAGE_NAME}:/home/svc_usr/data `
#    --log-opt syslog-address=tcp://main.sitesearch.cloud:9600 `
docker run -d --name ${DOCKER_IMAGE_NAME} `
    -p 2443:8001 `
    --log-driver=syslog `
    --log-opt syslog-address=tcp://main.sitesearch.cloud:5044 `
    --env SECURITY_USER_PASSWORD=$env:SECURITY_USER_PASSWORD `
    --env BUILD_NUMBER=$env:BUILD_NUMBER `
    --env SCM_HASH=$env:SCM_HASH `
    --env SECURITY_OAUTH2_CLIENT_CLIENT_SECRET=$env:SECURITY_OAUTH2_CLIENT_CLIENT_SECRET `
    -v ~/srv/${DOCKER_IMAGE_NAME}:/data `
    --network $docker_network `
    intrafind/${DOCKER_IMAGE_NAME}:${DOCKER_TAG}
cd ..

function cleanupDocker {
    $danglingImages = $(docker images -f "dangling=true" -q)
    if ([string]::IsNullOrEmpty($danglingImages)){
        "There are no dangling Docker images"
    } else {
        docker rmi -f $danglingImages # cleanup, GC for dangling images
    }
}
cleanupDocker
