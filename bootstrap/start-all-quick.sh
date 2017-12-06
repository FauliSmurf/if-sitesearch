#!/usr/bin/env sh

echo "== startup-script =="
sudo sysctl -w vm.max_map_count=262144 # required for ELK's Elasticsearch
# toolchain
docker start teamcity-server
sudo rm /home/alexander_orlov/buildAgent/logs/buildAgent.properties.lock
/home/alexander_orlov/buildAgent/bin/agent.sh start
docker start teamcity-agent-Venus
docker start teamcity-agent-Merkur

docker start sitesearch-elasticsearch
docker start sitesearch-search-service

docker start if-sitesearch
docker start if-sitesearch-green
docker start if-sitesearch-blue

docker start consul
docker start router
echo "/== startup-script =="


