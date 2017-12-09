#!/usr/bin/env sh

pwsh start-sitesearch-elasticsearch.ps1
pwsh start-sitesearch-search-service.ps1

ls ./opt/docker-compose-sitesearch.yaml
ls opt/docker-compose-sitesearch.yaml
ls ../opt/docker-compose-sitesearch.yaml

docker-compose --file ./opt/docker-compose-sitesearch.yaml down
docker-compose --file ./opt/docker-compose-sitesearch.yaml up -d