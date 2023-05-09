#!/bin/bash

export SPARK_DIST_CLASSPATH=$($HADOOP_HOME/bin/hadoop classpath)

cd bda-controller

/bin/sh -c ./src/main/scripts/bda-server.sh
