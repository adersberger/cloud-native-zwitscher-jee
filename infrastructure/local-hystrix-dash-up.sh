#!/bin/bash

HYSTRIX_DASH_PORT="9870"
echo "Starting local Hystrix dashboard on port ${HYSTRIX_DASH_PORT}..."
docker run -d -p ${HYSTRIX_DASH_PORT}:8080 --name hystrix-dashboard mlabouardy/hystrix-dashboard:latest