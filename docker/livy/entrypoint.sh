#!/bin/bash

echo "Starting livy server."
export SPARK_DIST_CLASSPATH=$($HADOOP_HOME/bin/hadoop classpath)
$LIVY_HOME/bin/livy-server
