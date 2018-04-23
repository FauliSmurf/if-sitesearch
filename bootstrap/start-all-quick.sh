#!/usr/bin/env sh

echo "== startup-script =="
# toolchain
docker start teamcity-server
sudo rm /home/alexander_orlov/buildAgent/logs/buildAgent.properties.lock
/home/alexander_orlov/buildAgent/bin/agent.sh start
docker start teamcity-agent-venus
docker start teamcity-agent-merkur

docker start sitesearch-elasticsearch
docker start sitesearch-elasticsearch-1
docker start sitesearch-search-service
docker start sitesearch-search-service-1

docker start if-sitesearch
docker start if-sitesearch-green
docker start if-sitesearch-green-1
docker start if-sitesearch-blue
docker start if-sitesearch-blue-1

docker start consul
docker start router

docker start if-tagging-service # might required to call `bootstrap/update-if-service.sh` if this does not work # does not work, try with "docker exec -it router nginx -s reload"
#sh bootstrap/update-if-service.sh if-tagging-service # have not been tested yet
docker start if-app-webcrawler
docker exec router nginx -s reload

sudo sysctl -w vm.max_map_count=262144 # required for ELK's Elasticsearch
docker-compose --file opt/docker-compose-elk.yaml -p sitesearch up -d
docker-compose --file opt/docker-compose-bg.yaml -p tmp up -d
echo "/== startup-script =="


