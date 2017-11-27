#!/usr/bin/env pwsh

$docker_image_name = "if-sitesearch"
$docker_tag = "latest"
$docker_network = "sitesearch"

./gradlew clean build --info -x test
cd service
docker build --tag intrafind/${docker_image_name}:${docker_tag} .
cd ..
docker ps
function isBlueUp() {
    $isBlueGreenLockSet = Test-Path ./blue-green-deployment.lock
    if($isBlueGreenLockSet){
        rm ./blue-green-deployment.lock
    } else {
        touch ./blue-green-deployment.lock
    }
    return $isBlueGreenLockSet
}

function runService([Int] $service_port = 3442, [String] $container_name) {
    docker run -d --name ${container_name} `
        -p ${service_port}:8001 `
        --log-driver=gelf `
        --log-opt gelf-address=udp://main.sitesearch.cloud:12201 `
        --env SECURITY_USER_PASSWORD=$env:SECURITY_USER_PASSWORD `
        --env BUILD_NUMBER=$env:BUILD_NUMBER `
        --env SCM_HASH=$env:SCM_HASH `
        --env SECURITY_OAUTH2_CLIENT_CLIENT_SECRET=$env:SECURITY_OAUTH2_CLIENT_CLIENT_SECRET `
        --network $docker_network `
        intrafind/${docker_image_name}:${docker_tag}
}

#$data = "${docker_image_name}-data"
#mkdir ~/srv/$data
#sudo chown -R 1000:1000 ~/srv/$data # make it a svc_usr' directory

# TODO change mapped volumes to avoid collision with other running containers
if(isBlueUp){
    write-host blue is active
    $green = "${docker_image_name}-green"

#    mkdir ~/srv/$green
#    sudo chown -R 1000:1000 ~/srv/$green # make it a svc_usr' directory
#        -v ~/srv/${green}:/home/svc_usr/data `

    docker rm -f $green
    runService -container_name $green
    echo "TEST BEGIN GREEN"
    sleep 150
    echo "TEST END GREEN"
#    docker run -d --name $green `
#        -p 3442:8001 `
#        --log-driver=gelf `
#        --log-opt gelf-address=udp://main.sitesearch.cloud:12201 `
#        --env SECURITY_USER_PASSWORD=$env:SECURITY_USER_PASSWORD `
#        --env BUILD_NUMBER=$env:BUILD_NUMBER `
#        --env SCM_HASH=$env:SCM_HASH `
#        --env SECURITY_OAUTH2_CLIENT_CLIENT_SECRET=$env:SECURITY_OAUTH2_CLIENT_CLIENT_SECRET `
#        --network $docker_network `
#        intrafind/${docker_image_name}:${docker_tag}

#echo ~/srv/$green
#echo ~/srv/$data ~/srv/$green
#echo "~/srv/$data ~/srv/$green"
#        sudo rm -rf ~/srv/$blue
#        sudo ln -s ~/srv/$data ~/srv/$green
} else {
    write-host blue is inactive
    $blue = "${docker_image_name}-blue"

#    mkdir ~/srv/$blue
#    sudo chown -R 1000:1000 ~/srv/$blue # make it a svc_usr' directory
#        -v ~/srv/${blue}:/home/svc_usr/data `
#        --log-driver=gelf `
#        --log-opt gelf-address=udp://main.sitesearch.cloud:12201 `
    docker rm -f $blue
    runService -service_port 4442 -container_name $blue
    echo "TEST BEGIN BLUE"
    sleep 150
    echo "TEST END BLUE"

#    docker run -d --name $blue `
#        -p 4442:8001 `
#        --log-driver=gelf `
#        --log-opt gelf-address=udp://main.sitesearch.cloud:12201 `
#        --env SECURITY_USER_PASSWORD=$env:SECURITY_USER_PASSWORD `
#        --env BUILD_NUMBER=$env:BUILD_NUMBER `
#        --env SCM_HASH=$env:SCM_HASH `
#        --env SECURITY_OAUTH2_CLIENT_CLIENT_SECRET=$env:SECURITY_OAUTH2_CLIENT_CLIENT_SECRET `
#        --network $docker_network `
#        intrafind/${docker_image_name}:${docker_tag}

#echo ~/srv/$blue
#echo ~/srv/$data ~/srv/$blue
#echo "~/srv/$data ~/srv/$blue"
#        sudo rm -rf ~/srv/$green
#        sudo ln -s ~/srv/$data ~/srv/$blue
}
