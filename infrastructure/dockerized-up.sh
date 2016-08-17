#!/bin/bash

COMPOSE_FILE="./zwitscher-cluster/docker-compose.yml"
PROJECT_NAME="zwitscher"
echo "Starting ${PROJECT_NAME} locally with Docker Compose..."
docker-compose -f ${COMPOSE_FILE} build --no-cache --pull --force-rm
docker-compose -f ${COMPOSE_FILE} up -d
docker-compose -f ${COMPOSE_FILE} ps