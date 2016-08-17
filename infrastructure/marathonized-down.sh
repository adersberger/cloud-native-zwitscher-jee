#!/bin/bash

APP_GROUP="/zwitscher"
echo "Stopping ${APP_GROUP} on DC/OS (Marathon)"
dcos marathon group remove --force ${APP_GROUP}