#!/usr/bin/env powershell

docker start teamcity-server
~/buildAgent/bin/agent.sh start

docker start elasticsearch
docker start sitesearch-search-service
docker start if-sitesearch
docker start if-sitesearch-green
docker start if-sitesearch-blue
docker start http-to-https-redirect


