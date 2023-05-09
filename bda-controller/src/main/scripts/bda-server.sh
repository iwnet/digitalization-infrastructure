#!/bin/bash

ROOT_DIR="target"
LIB_DIR="${ROOT_DIR}/lib"
#JAR_FILE="$(ls ${ROOT_DIR}/bda-controller-*.jar)"
JAR_FILE="$(echo ${ROOT_DIR}/classes)" # used for debugging only
ENTRYPOINT="gr.ntua.ece.cslab.iwnet.bda.controller.Entrypoint"
CONF_FILE="../conf/bda.properties"
LOG_PROPERTIES_FILE="./src/main/resources/log4j.properties"

CLASSPATH="$(ls $LIB_DIR/* | tr '\n' ':')$JAR_FILE"
java -Dlog4j.configuration=file:$LOG_PROPERTIES_FILE -Dspring.config.location=$CONF_FILE -cp $CLASSPATH $ENTRYPOINT $CONF_FILE
