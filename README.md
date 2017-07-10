if-sitesearch
    sfdsfa test
=

# About

* [JIRA]()
* [PROD Release]() / [DEV Release]()
* [PROD Release]()
    
# Pre-requisite / Configuration

Add a [configuration profile](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html#boot-features-external-config-profile-specific-properties) 
to the `config` folder inside this project.

# API Reference

    http://localhost:8200/swagger-ui.html

# Initialization

## Elasticsearch

    docker run --rm -d --name sitesearch -p 9200-9300:9200-9300 -v c:/Users/alex/my/project/intrafind/elasticsearch/data:/usr/share/elasticsearch/data elasticsearch:5.4.3-alpine

# Run 

    ./run.sh
    open http://localhost:8200
    
# Test

    ./test.sh

`SPRING_CONFIG_NAME=application,local` environment property is required to add a specific configuration’s properties.    

# Benchmark / Performance Test

    ./gradlew clean jmh

# Release

    ./release.sh