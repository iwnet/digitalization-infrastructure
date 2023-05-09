#!/bin/bash

### Change to keycloak listening ip
IP_ADDRESS=""

### Change to keycloak listening port (8080/8443 for ssl)
KEYCLOAK_LISTEN_PORT="8080"

### If ssl connection change to "s" else leave empty 
SSL=""

#######################################################################################################
################################### DO NOT CHANGE ANYTHING FROM HERE ##################################
#######################################################################################################

ADMIN_USERNAME=admin
ADMIN_PASSWORD=iwnet_admin
CLIENT_ID=bda_client
CLIENT_SECRET=bda_secret
REALM="test_realm"

KEYCLOAK_PREFIX="http${SSL}://${IP_ADDRESS}:${KEYCLOAK_LISTEN_PORT}/auth"
BDA_SERVER_PREFIX="http${SSL}://${IP_ADDRESS}:9999/api"

results=$(curl -d "client_id=${CLIENT_ID}" -d "client_secret=${CLIENT_SECRET}" -d "username=${ADMIN_USERNAME}" -d "password=${ADMIN_PASSWORD}"  -d 'grant_type=password' -s "${KEYCLOAK_PREFIX}/realms/test_realm/protocol/openid-connect/token" && echo)
token=$(python3 -c "import sys, json; print(json.loads(json.dumps($results))['access_token'])")

curl -ik -X GET -H "Content-type:application/json" -H "Accept:application/json"  -H "Authorization: bearer $token" "$BDA_SERVER_PREFIX/sharedrecipes" > /dev/null 2>&1

curl -ik -X POST -H "Content-type:application/json" -H "Accept:application/json" -H "Authorization: bearer $token" $BDA_SERVER_PREFIX/datastore/init && echo

curl -ik -X POST -H "Authorization: bearer $token" -H "Content-type:application/json" -H "Accept:application/json" --data @iwnetrepo.json "$BDA_SERVER_PREFIX/datastore" && echo


