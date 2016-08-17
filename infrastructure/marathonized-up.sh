#!/bin/bash

APP_GROUP="/zwitscher"
MARATHON_FILE="./zwitscher/marathon-appgroup.json"
echo "Starting ${APP_GROUP} on DC/OS (Marathon)..."

if [ -n 'dcos marathon group list | grep -n $APP_GROUP' ]
  then
    echo "Group $APP_GROUP already exists. Updating..."
    dcos marathon group update ${APP_GROUP} < ${MARATHON_FILE}
  else
    echo "Group $APP_GROUP does not exist. Adding..."
    dcos marathon group add ${MARATHON_FILE}
fi