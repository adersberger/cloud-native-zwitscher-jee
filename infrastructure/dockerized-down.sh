#!/bin/bash

COMPOSE_FILE="./zwitscher-cluster/docker-compose.yml"
PROJECT_NAME="zwitscher"
echo "Stopping ${PROJECT_NAME} on docker compose..."
docker-compose -f ${COMPOSE_FILE} down