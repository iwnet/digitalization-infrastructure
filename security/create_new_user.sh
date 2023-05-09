#!/bin/bash


### Change to keycloak listening ip
IP_ADDRESS=""

### Change to keycloak listening port (8080/8443 for ssl)
KEYCLOAK_LISTEN_PORT="8080"

### If ssl connection change to "s" else leave empty 
SSL=""

### Change accordingly to how the configuration in the docker/.env file is setup. MASTER_ADMIN_USER, MASTER_ADMIN_PASSWORD 
### refer to the master realm admin. 
MASTER_ADMIN_USER="admin"
MASTER_ADMIN_PASSWORD="admin"


############################################################################################
############################# DO NOT CHANGE ANYTHING FROM HERE #############################
############################################################################################

REALM="test_realm"

KEYCLOAK_BASE_URL="http${SSL}://${IP_ADDRESS}:${KEYCLOAK_LISTEN_PORT}/auth"

echo $KEYCLOAK_BASE_URL

USERNAME=$1
PASSWORD=$2
TYPE=$3

# Fetch Keycloak admin token from master realm
results=$(curl -d "client_id=admin-cli" -d "username=${MASTER_ADMIN_USER}" -d "password=${MASTER_ADMIN_PASSWORD}"  -d 'grant_type=password' -s "${KEYCLOAK_BASE_URL}/realms/master/protocol/openid-connect/token" && echo)

token=$(./get_access_token.py "$results")

echo $token

# Fecth desired role id
rid=$(./get_role_id.py "$(curl -X GET -H "Accept: application/json" -H "Content-Type: application/json" -H "Authorization: bearer $token" -s "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/roles" && echo)" "$TYPE")
echo "rid : $rid"


# Create new user
curl -ik -X POST -H "Accept: application/json" -H "Content-Type: application/json" -H "Authorization: bearer $token" -d "{\"username\":\"$USERNAME\",\"enabled\": true,\"emailVerified\": true,\"credentials\" :[{\"type\":\"password\",\"value\":\"$PASSWORD\",\"temporary\":false}]}" -s "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/users" && echo

curl -X GET -H "Accept: application/json" -H "Content-Type: application/json" -H "Authorization: bearer $token" -s "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/users?username=$USERNAME" && echo

# Fetch id of created admin user
uid=$(./get_user_id.py "$(curl -X GET -H "Accept: application/json" -H "Content-Type: application/json" -H "Authorization: bearer $token" -s "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/users?username=$USERNAME" && echo)")
echo $uid

# Map admin user to admin role 
curl -ik -X POST -H "Accept: application/json" -H "Content-Type: application/json" -H "Authorization: bearer $token" -d "[{\"id\": \"${rid}\", \"name\": \"$TYPE\"}]" -s "${KEYCLOAK_BASE_URL}/admin/realms/${REALM}/users/${uid}/role-mappings/realm" && echo

