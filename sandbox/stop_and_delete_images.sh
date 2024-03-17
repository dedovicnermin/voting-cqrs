#!/usr/bin/env bash

docker compose down
docker volume rm $(docker volume ls -q)
docker image rm dedovicnermin/election-integrity:latest
docker image rm dedovicnermin/vote-integrity:latest
docker image rm dedovicnermin/election-ttl:latest
docker image rm dedovicnermin/query-service:latest
docker image rm dedovicnermin/event-sink:latest