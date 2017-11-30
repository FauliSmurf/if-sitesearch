#!/usr/bin/env sh

#Set-StrictMode -Version Latest
#$ErrorActionPreference = "Stop"
#$PSDefaultParameterValues["*:ErrorAction"] = "Stop"

#function setupRedirectRouterContainer {
    docker_network="sitesearch"
    docker_redirect_image="router"
    docker_tag="latest"
    cd docker-${docker_redirect_image}
    docker build --tag intrafind/${docker_redirect_image}:${docker_tag} .
    docker rm -f $docker_redirect_image
#    mkdir ~/srv/${docker_redirect_image}
#        -e BLUE_NAME=blue `
#        -e GREEN_NAME=green `
#        -e LIVE=blue `
#        -e CONSUL_URL=consul:8500 `
#        -e constraint:node=master `
    docker run -d --name $docker_redirect_image \
        -p 80:80 \
        -p 443:443 \
        -v /etc/letsencrypt:/etc/letsencrypt \
        --network $docker_network \
        intrafind/${docker_redirect_image}:${docker_tag}

    cd ..
#}
#setupRedirectRouterContainer