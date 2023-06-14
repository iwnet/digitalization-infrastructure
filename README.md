IW-NET Big Data Analytics Module 
================================


About
-----
This repository contains the source code of the IW-NET Big Data Analytics component.
The set of Dockerfiles provided in the docker folder are intended for setting up a
local development environment with all the required Big Data Analytics subcomponents.
The set of Helm scripts provided in the kubernetes folder are intended for setting up a
local Kubernetes cluster that hosts all the Big Data Analytics subcomponents.
All required databases and modules are containerized and reasonable defaults are 
provided. Encryption and high availability options are disabled by default, however 
user authentication/authorization is enabled. This setup is not for production usage.


Requirements
------------
Latest docker.io, docker-compose v.1.24.1, python3, curl, make.

You can install the requirements using the script install_prequisities.sh.


Getting Started
---------------
In order to setup the BDA with all the required components locally:
 
1. Edit the _docker/.env_ file and provide local values for the unset variables.
2. Run ```make``` from the _docker_ directory to build all the required images using
   the Dockerfiles.
3. Create a _conf/bda.properties_ file using the provided template and provide values 
   for the highlighted parameters (similar with the _.env_ file).
4. Run ```docker-compose up``` from the _docker_ directory to create a network, the 
   volumes and the containers of the BDA subcomponents.


Start the BDA server
--------------------
1. Compile and run the BDA server:
```
docker exec -it bda-controller /bin/bash
   > ./compile.sh 
   > nohup ./run.sh 2>&1 > out.txt &
```
2. Exit the container
3. Navigate to the folder big-data-platform
4. Edit the init.sh file and fillin the necessary information about the bda server / keycloak
5. Run the script
```
   > ./init.sh
```


Create Secure Users
-------------------
Visit security/README.md for more information


Interact with the BDA server
----------------------------
Visit examples/curl_examples.txt for REST-based examples.


Local deployment with Kubernetes
--------------------------------
- Install latest minikube, helm, kubectl packages.
- Execute steps 1 and 3 from the 'Getting Started' section.
- Edit the _kubernetes/helm/postgres/values.yaml_ and _kubernetes/helm/keycloak/values.yaml_ 
  files and provide local values for the unset variables (similar with the .env file).
- Start minikube with:
```
minikube start --mount --mount-string="PATH_TO_SOURCE_CODE:/home/ubuntu/shared-bigdata-infra" \
--memory 4096 --kubernetes-version=v1.23.12 
```
- Navigate to the folder big-data-platform and start the pods with the command: 
```./start_iwnet_with_helm_minikube.sh```
- From two separate terminals execute the following (blocking) commands:
```
BDA_CONTROLLER_POD=$(kubectl get all -n iwnet -o wide | grep 'pod/bda-controller' | awk '{print $1}')
kubectl port-forward $BDA_CONTROLLER_POD 9999:9999 --address='0.0.0.0' -n iwnet
```
``` 
BDA_KEYCLOAK_POD=$(kubectl get all -n iwnet -o wide | grep 'pod/bda-keycloak' | awk '{print $1}')
kubectl port-forward $BDA_KEYCLOAK_POD 8080:8080 --address='0.0.0.0' -n iwnet
```
- Execute steps 1-5 from the 'Start the BDA server' section after replacing the first command 
of step 1 with the following: ```kubectl exec -it $BDA_CONTROLLER_POD -n iwnet -- bash```

Contact
-------
ikons@cslab.ece.ntua.gr 
