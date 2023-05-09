#!/bin/bash

$HADOOP_HOME/bin/hadoop jar \
    $HADOOP_HOME/share/hadoop/yarn/hadoop-yarn-applications-distributedshell-2.9.1.jar \
    org.apache.hadoop.yarn.applications.distributedshell.Client \
    --jar $HADOOP_HOME/share/hadoop/yarn/hadoop-yarn-applications-distributedshell-2.9.1.jar \
    --shell_command date \
    --num_containers 2
