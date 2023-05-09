#!/bin/bash

echo "Starting bda server."
export SPARK_DIST_CLASSPATH=$($HADOOP_HOME/bin/hadoop classpath)
export CLASSPATH="$BDA_HOME/jars/$JAR_FILE:$(ls -1 $BDA_HOME/jars/lib/*.jar | tr '\n' ':')"
java -cp "$CLASSPATH" "$ENTRYPOINT" "$CONF_FILE"
