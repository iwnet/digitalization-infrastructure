IW-NET Big Data Analytics Module 
================================


About
-----
This repository contains the source code of the IW-NET Big Data Analytics component.
The set of Dockerfiles provided in the docker folder are intended for setting up a
local development environment with all the required Big Data Analytics subcomponents. 
All required databases and modules are containerized and 
reasonable defaults are provided. This setup is not for production usage.


Requirements
------------
Latest docker.io, docker-compose v.1.24.1, python3, curl.

You can install the requirements using the script install_prequisities.sh located 
in the big_data_platform directory.


Getting Started
---------------
In order to setup the BDA with all the required components locally:
 
1. Edit the _docker/.env_ file and provide local values for the unset variables.
2. Run ```make``` from the _docker_ directory to build all the required images using
   the Dockerfiles.
3. Create a _conf/bda.properties_ file using the provided template and provide values for the highlighted parameters (similar with the _.env_ file).
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
3. Navigate to the folder big_data_platform
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


Contact
-------
ikons@cslab.ece.ntua.gr 
