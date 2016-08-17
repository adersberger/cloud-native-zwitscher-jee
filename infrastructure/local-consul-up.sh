#!/bin/bash

echo "Starting local Consul in development mode... (press Ctrl+C to shutdown)"
./zwitscher-service-discovery/bin/consul agent -dev -ui -bind=127.0.0.1