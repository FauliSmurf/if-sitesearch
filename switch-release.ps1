#!/usr/bin/env powershell

$docker_image_name = "if-sitesearch"
$docker_tag = "latest"
$docker_network = "sitesearch"

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

$data = "${docker_image_name}-data"
mkdir ~/srv/$data
sudo chown -R 1000:1000 ~/srv/$data # make it a svc_usr' directory

# TODO change mapped volumes to avoid collision with other running containers
if(isBlueUp){
    write-host blue is up and will be removed
    $green = "${docker_image_name}-green"

#    mkdir ~/srv/$green
#    sudo chown -R 1000:1000 ~/srv/$green # make it a svc_usr' directory

    docker rm -f $green
    docker run -d --name $green `
        -p 3442:8001 `
        --env SECURITY_USER_PASSWORD=$env:SECURITY_USER_PASSWORD `
        --env BUILD_NUMBER=$env:BUILD_NUMBER `
        --env SCM_HASH=$env:SCM_HASH `
        -v ~/srv/${data}:/home/svc_usr/data `
        --network $docker_network `
        intrafind/${docker_image_name}:${docker_tag}

    $Env:SITESEARCH_VARIANT_RUNNING = "green"

} else {
    write-host blue is down and will be removed
    $blue = "${docker_image_name}-blue"

#    mkdir ~/srv/$blue
#    sudo chown -R 1000:1000 ~/srv/$blue # make it a svc_usr' directory

    docker rm -f $blue
    docker run -d --name $blue `
        -p 4442:8001 `
        --env SECURITY_USER_PASSWORD=$env:SECURITY_USER_PASSWORD `
        --env BUILD_NUMBER=$env:BUILD_NUMBER `
        --env SCM_HASH=$env:SCM_HASH `
        -v ~/srv/${data}:/home/svc_usr/data `
        --network $docker_network `
        intrafind/${docker_image_name}:${docker_tag}

    $Env:SITESEARCH_VARIANT_RUNNING = "blue"
}
