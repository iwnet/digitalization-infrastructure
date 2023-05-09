Create Secure User on Keycloak - manual
==========================================

0. Before first use edit the script ```create_new_user.sh ``` and fix 
   keycloak related info.
1. Provide the script with the needed information on creating a new user:
   - username
   - password
   - usertype (choose between admin / iwnet_user)
   and run it 
```
   > ./create_new_user.sh <username> <password> "admin"/"iwnet_user"
```
