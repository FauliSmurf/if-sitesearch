#!/usr/bin/env sh

docker_image_name=if-sitesearch
docker_tag=latest
docker_network=sitesearch

./gradlew clean build --info -x test
cd service
docker build --tag intrafind/${docker_image_name}:${docker_tag} .
cd ..
docker ps

isBlueUp() {
    if [ -f "./blue-green-deployment.lock" ]; then
        rm ./blue-green-deployment.lock
        return 0
    else
        touch ./blue-green-deployment.lock
        return 1
    fi
}

runService() {
    docker run -d --name $1 \
        --log-driver=gelf \
        --log-opt gelf-address=udp://localhost:12201 \
        --env SECURITY_USER_PASSWORD=$SECURITY_USER_PASSWORD \
        --env BUILD_NUMBER=$BUILD_NUMBER \
        --env SCM_HASH=$SCM_HASH \
        --env SECURITY_OAUTH2_CLIENT_CLIENT_SECRET=$SECURITY_OAUTH2_CLIENT_CLIENT_SECRET \
        --network $docker_network \
        intrafind/${docker_image_name}:${docker_tag}
}

if isBlueUp; then
    echo "blue is active"
    green="${docker_image_name}-green"

    docker rm -f $green
    runService $green
    sleep 21
    docker exec router switch.sh green

else
    echo "blue is inactive"
    blue="${docker_image_name}-blue"

    docker rm -f $blue
    runService $blue
    sleep 21
    docker exec router switch.sh blue
fi
