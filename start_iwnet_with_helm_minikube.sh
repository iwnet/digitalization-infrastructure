#!/bin/bash

###### RUN ANY DOCKER COMMAND INSIDE MINIKUBE
eval $(minikube docker-env)

###### CREATE NAMESPACE FOR BDA DEPLOYMENT
kubectl create namespace iwnet

###### REMOVE OLD DEPLOYMENTS ON KUBERNETES CLUSTER
helm uninstall network-observer -n iwnet #--purge
helm uninstall metrics-server
helm uninstall zookeeper -n iwnet
helm uninstall hadoop-namenode -n iwnet
helm uninstall hadoop-journalnode -n iwnet
helm uninstall hadoop-datanode -n iwnet
helm uninstall hadoop-yarn-rm -n iwnet
helm uninstall spark -n iwnet
helm uninstall hbase-master -n iwnet
helm uninstall hbase-regionserver -n iwnet
helm uninstall livy -n iwnet
helm uninstall postgres -n iwnet
#sleep 15
if [ "$(kubectl get pods -n iwnet | grep pgo)" != "" ]
then
    while [ "$(kubectl get pods -n iwnet | grep postgres)" != "" ]; do 
        sleep 5; 
        echo "Waiting for Postgres Chart to shut down.";
    done
    helm uninstall pgo -n iwnet # if postgres HA is enabled
fi
helm uninstall kafka -n iwnet
helm uninstall keycloak -n iwnet
helm uninstall bda -n iwnet

kubectl delete pvc --all -n iwnet # Helm will not destroy PVCs since they are not created/controlled by Helm


###### MAKE CHANGES BASED ON VAULES.YAML FILES BEFORE IMAGE BUILDS

# ZOOKEEPER #
ZKreplicas=$(cat kubernetes/helm/zookeeper/values.yaml | grep replica | awk {'print $2'})
if [[ $ZKreplicas == 1 ]] ; then
# HA ZK not enabled
    echo 'ZK HA not enabled'
    echo '#!/bin/bash

    # Write myid only if it does not exist.
    if [ ! -f "$ZOOKEEPER_DATADIR/myid" ]; then
        echo "${ZOOKEEPER_MYID}" > "$ZOOKEEPER_DATADIR/myid"
    fi

    # Start zookeeper.
    echo "Starting Zookeeper daemon."
    zookeepermasterservice=$(tail -n 1 /etc/hosts | awk "{print \$NF}")
    ZOOKEEPER_PREFIX="/usr/local/zookeeper"
    sed -i "s/bda-zookeeper-master/${zookeepermasterservice}/g" ${ZOOKEEPER_PREFIX}/conf/zoo.cfg

    $ZOOKEEPER_HOME/bin/zkServer.sh start-foreground
    ' > docker/zookeeper/entrypoint.sh

        echo '
    tickTime=2000
    dataDir=/data/zookeeper
    clientPort=2181
    initLimit=5
    syncLimit=2
    server.1=bda-zookeeper-master:2888:3888 
    ' > docker/zookeeper/zoo.cfg
else
# HA ZK enabled
    echo 'ZOOKEEPER HA enabled'
    echo '#!/bin/bash

    # Write myid only if it does not exist.
    if [ ! -f "$ZOOKEEPER_DATADIR/myid" ]; then
        echo "${ZOOKEEPER_MYID}" > "$ZOOKEEPER_DATADIR/myid"
    fi

    # Start zookeeper.
    echo "Starting Zookeeper daemon."

    MYID_FILE="/data/zookeeper/myid"
    # Create myid-file
    # Extract only numbers from hostname
    id=$[$(hostname | tr -d -c 0-9)+1] #myId starts from 1, not 0
    echo $id > "${MYID_FILE}"

    $ZOOKEEPER_HOME/bin/zkServer.sh start-foreground
    ' > docker/zookeeper/entrypoint.sh

    echo '
    tickTime=2000
    dataDir=/data/zookeeper
    clientPort=2181
    initLimit=5
    syncLimit=2
    ' > docker/zookeeper/zoo.cfg
    for i in $(seq 1 $ZKreplicas); do
        echo "server.${i}=bda-zookeeper-master-$((${i}-1)).bda-zookeeper-master.iwnet.svc.cluster.local:2888:3888" >> docker/zookeeper/zoo.cfg
    done
fi

# HDFS #
HDFS_NNreplicas=$(cat kubernetes/helm/hadoop-namenode/values.yaml | grep replica | awk {'print $2'})

# Reset hdfs-site.xml conf file
echo '<?xml version="1.0" encoding="UTF-8"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<configuration>
  <property>
    <name>dfs.datanode.use.datanode.hostname</name>
    <value>false</value>
  </property>

  <property>
    <name>dfs.client.use.datanode.hostname</name>
    <value>false</value>
  </property>

  <property>
    <name>dfs.replication</name>
    <value>3</value>
  </property>

  <property>
    <name>dfs.datanode.data.dir</name>
    <value>file:///var/local/hadoop/hdfs/datanode/</value>
    <description>Comma separated list of paths on the local filesystem of a DataNode where it should store its blocks.</description>
  </property>

  <property>
    <name>dfs.namenode.name.dir</name>
    <value>file:///var/local/hadoop/hdfs/namenode/</value>
    <description>Path on the local filesystem where the NameNode stores the namespace and transaction logs persistently.</description>
  </property>

  <property>
    <name>dfs.namenode.datanode.registration.ip-hostname-check</name>
    <value>false</value>
  </property>

</configuration>' > ./docker/hadoop/base/hdfs-site.xml

if [[ $HDFS_NNreplicas == 1 ]] ; then
# HA NN not enabled
    echo 'HA HDFS NN not enabled'
    # Update for HDFS NN
    echo '#!/bin/bash

    # Format the namenode directory.
    if [[ ! -d "$HADOOP_NAMENODE_DIR" ]] || [[ -z `ls -A "$HADOOP_NAMENODE_DIR"` ]] ; then
        echo "Formating namenode root fs."
        $HADOOP_HOME/bin/hdfs namenode -format
    fi

    namenodehost=$(tail -n 1 /etc/hosts | awk "{print \$NF}")

    # Start the namenode.
    echo "Starting HDFS namenode."
    $HADOOP_HOME/bin/hdfs namenode
    ' > docker/hadoop/namenode/entrypoint.sh

    # Update for HDFS DN
    echo '#!/bin/bash

    # Start the datanode.
    
    echo "Starting HDFS datanode."
    $HADOOP_HOME/bin/hdfs datanode
    ' > docker/hadoop/datanode/entrypoint.sh

else
# HA NN enabled
    echo 'HA HDFS NN enabled'
    # Update for HDFS NN & DN ^ JN
    echo '#!/bin/bash
    #    # Format the namenode directory.
    #    if [[ ! -d "$HADOOP_NAMENODE_DIR" ]] || [[ -z `ls -A "$HADOOP_NAMENODE_DIR"` ]] ; then
    #        echo "Formating namenode root fs."
    #        $HADOOP_HOME/bin/hdfs namenode -format
    #    fi
    ' > docker/hadoop/namenode/entrypoint.sh

    echo '#!/bin/bash
    ' > docker/hadoop/datanode/entrypoint.sh
    #
    echo '#!/bin/bash
    ' > docker/hadoop/journalnode/entrypoint.sh    
    #
    nn_ids=""
    for i in $(seq 1 $HDFS_NNreplicas); do
        nn_ids+="nn$((${i})),"
    done
    nn_ids=${nn_ids%?}
    ## Edit hdfs-site.xml to set HADOOP parameters for NameNode HA
    HDFS_SITE_CONTENT="\t<property>\n\t\t<name>dfs.nameservices</name>\n\t\t<value>bda-hadoop-namenode</value>\n\t</property>"
    HDFS_SITE_CONTENT="${HDFS_SITE_CONTENT}\n\t<property>\n\t\t<name>dfs.ha.namenodes.bda-hadoop-namenode</name>\n\t\t<value>$nn_ids</value>\n\t</property>"
    HDFS_SITE_CONTENT="${HDFS_SITE_CONTENT}\n\t<property>\n\t\t<name>dfs.client.failover.proxy.provider.bda-hadoop-namenode</name>\n\t\t<value>org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider</value>\n\t</property>"
    HDFS_SITE_CONTENT="${HDFS_SITE_CONTENT}\n\t<property>\n\t\t<name>dfs.ha.fencing.methods</name>\n\t\t<value>shell(/bin/true)</value>\n\t</property>"
    HDFS_SITE_CONTENT="${HDFS_SITE_CONTENT}\n\t<property>\n\t\t<name>dfs.ha.fencing.ssh.private-key-files</name>\n\t\t<value>/root/.ssh/id_rsa</value>\n\t</property>"

    for i in $(seq 1 $HDFS_NNreplicas); do 
        HDFS_SITE_CONTENT="${HDFS_SITE_CONTENT}\n\t<property>\n\t\t<name>dfs.namenode.rpc-address.bda-hadoop-namenode.nn${i}</name>\n\t\t<value>bda-hadoop-namenode-$((${i}-1)).bda-hadoop-namenode.iwnet.svc.cluster.local:8020</value>\n\t</property>"
        HDFS_SITE_CONTENT="${HDFS_SITE_CONTENT}\n\t<property>\n\t\t<name>dfs.namenode.http-address.bda-hadoop-namenode.nn${i}</name>\n\t\t<value>bda-hadoop-namenode-$((${i}-1)).bda-hadoop-namenode.iwnet.svc.cluster.local:50070</value>\n\t</property>"
    done


    zk_addr="" 
    for i in $(seq 1 $ZKreplicas); do
        zk_addr+="bda-zookeeper-master-$((${i}-1)).bda-zookeeper-master.iwnet.svc.cluster.local:2181,"
    done
    zk_addr=${zk_addr%?}
    # for automatic failover
    HDFS_SITE_CONTENT="${HDFS_SITE_CONTENT}\n\t<property>\n\t\t<name>dfs.ha.automatic-failover.enabled</name>\n\t\t<value>true</value>\n\t</property>"
    HDFS_SITE_CONTENT="${HDFS_SITE_CONTENT}\n\t<property>\n\t\t<name>ha.zookeeper.quorum</name>\n\t\t<value>$zk_addr</value>\n\t</property>"

    HDFS_JNreplicas=$(cat kubernetes/helm/hadoop-journalnode/values.yaml | grep replica | awk {'print $2'})
    jn_addr="" 
    for i in $(seq 1 $HDFS_JNreplicas); do
        jn_addr+="bda-hadoop-journalnode-$((${i}-1)).bda-hadoop-journalnode.iwnet.svc.cluster.local:8485;"
    done
    jn_addr=${jn_addr%?}
    # for journalnodes
    HDFS_SITE_CONTENT="${HDFS_SITE_CONTENT}\n\t<property>\n\t\t<name>dfs.namenode.shared.edits.dir</name>\n\t\t<value>qjournal://$jn_addr/bda-hadoop-namenode</value>\n\t</property>"
    HDFS_SITE_CONTENT="${HDFS_SITE_CONTENT}\n\t<property>\n\t\t<name>dfs.journalnode.edits.dir</name>\n\t\t<value>/var/local/hadoop/hdfs/journal</value>\n\t</property>" #/home/hadoop/data/hadoop/jndata

    INPUT_HDFS_SITE_CONTENT=$(echo $HDFS_SITE_CONTENT | sed 's/\//\\\//g')
    sed -i "/<\/configuration>/ s/.*/${INPUT_HDFS_SITE_CONTENT}\n&/" ./docker/hadoop/base/hdfs-site.xml

    sed -i "s/bda-hadoop-namenode:8020/bda-hadoop-namenode/g" ./docker/hadoop/base/core-site.xml

    echo '
        hostname=$(cat /proc/sys/kernel/hostname)
        echo $hostname

        if [[ $hostname = "bda-hadoop-namenode-0" ]]; then
            if [[ ! -d "/hadoop-ha/bda-hadoop-namenode" ]] || [[ -z `ls -A "/hadoop-ha/bda-hadoop-namenode"` ]] ; then
                echo "Y" | $HADOOP_HOME/bin/hdfs zkfc -formatZK
                echo "Formatting ZKFC."
                # Enable ZKFC for HA Namenode automatic failover
            fi
            if [[ ! -d "$HADOOP_NAMENODE_DIR" ]] || [[ -z `ls -A "$HADOOP_NAMENODE_DIR"` ]] ; then
                echo "Formating namenode root fs."
                $HADOOP_HOME/bin/hdfs namenode -format
                # Format HDFS Cluster
            fi
        else
            echo "Doing bootstrapStandby."
            echo "N" | $HADOOP_HOME/bin/hdfs namenode -bootstrapStandby
        fi

        # if [[ ! -d "/hadoop-ha/bda-hadoop-namenode" ]] || [[ -z `ls -A "/hadoop-ha/bda-hadoop-namenode"` ]] ; then
        #     echo "Y" | $HADOOP_HOME/bin/hdfs zkfc -formatZK && $HADOOP_HOME/sbin/hadoop-daemon.sh start zkfc
        #     echo "Formatting & Starting ZKFC."
        # else
        echo "Starting ZKFC."
        $HADOOP_HOME/sbin/hadoop-daemon.sh start zkfc
        # fi

        # Start the namenode.
        echo "Starting HDFS namenode."
        $HADOOP_HOME/bin/hdfs namenode
    ' >> docker/hadoop/namenode/entrypoint.sh

    # Update for HDFS DN
    echo '  
        # Start the datanode. 
        echo "Starting HDFS datanode."
        $HADOOP_HOME/bin/hdfs datanode
    ' >> docker/hadoop/datanode/entrypoint.sh
    # Update for HDFS JN
    echo '  
        # Start the journalnode. 
        echo "Starting HDFS journalnode."
        #$HADOOP_HOME/sbin/hadoop-daemon.sh start journalnode
        /usr/local/hadoop/bin/hdfs journalnode

        #cat /usr/local/hadoop/logs/hadoop--journalnode-bda-hadoop-journalnode-0.out
        #cat /usr/local/hadoop/logs/hadoop--journalnode-bda-hadoop-journalnode-0.log
    ' >> docker/hadoop/journalnode/entrypoint.sh
fi

# HADOOP YARN RM #
YARN_RMreplicas=$(cat kubernetes/helm/hadoop-yarn-rm/values.yaml | grep replica | awk {'print $2'})
if [[ $YARN_RMreplicas == 1 ]] ; then
# HA YARNRM not enabled
    echo 'HA HADOOP YARN RM not enabled'
    echo '#!/bin/bash
        # Start YARN resourcemanager
        echo "Starting YARN resource manager daemon."
        $HADOOP_HOME/bin/yarn resourcemanager
    ' > docker/hadoop/resourcemanager/entrypoint.sh 
    #continue
else
# HA YARNRM enabled
    echo 'HA HADOOP YARN RM enabled'
    echo '#!/bin/bash

        ## Edit yarn-site.xml to set YARN parameters for HA
        YARN_SITE_CONTENT="\t<property>\n\t\t<name>yarn.resourcemanager.ha.enabled</name>\n\t\t<value>true</value>\n\t</property>"
    ' > docker/hadoop/resourcemanager/entrypoint.sh 

    zk_addr="" 
    for i in $(seq 1 $ZKreplicas); do
        zk_addr+="bda-zookeeper-master-$((${i}-1)).bda-zookeeper-master.iwnet.svc.cluster.local:2181,"
    done
    zk_addr=${zk_addr%?}
    echo '
        YARN_SITE_CONTENT="${YARN_SITE_CONTENT}\n\t<property>\n\t\t<name>yarn.resourcemanager.zk-address</name>\n\t\t<value>'$zk_addr'</value>\n\t</property>"
    ' >> docker/hadoop/resourcemanager/entrypoint.sh 

    rm_ids=""
    for i in $(seq 1 $YARN_RMreplicas); do
        rm_ids+="rm$((${i})),"
    done
    rm_ids=${rm_ids%?}
    echo '
        YARN_SITE_CONTENT="${YARN_SITE_CONTENT}\n\t<property>\n\t\t<name>yarn.resourcemanager.ha.rm-ids</name>\n\t\t<value>'$rm_ids'</value>\n\t</property>"
    ' >> docker/hadoop/resourcemanager/entrypoint.sh 

    for i in $(seq 1 $YARN_RMreplicas); do
        echo '     
            YARN_SITE_CONTENT="${YARN_SITE_CONTENT}\n\t<property>\n\t\t<name>yarn.resourcemanager.hostname.rm'${i}'</name>\n\t\t<value>bda-hadoop-yarn-resourcemanager-'$((${i}-1))'</value>\n\t</property>"
        ' >> docker/hadoop/resourcemanager/entrypoint.sh 
    done

    echo '        
        YARN_SITE_CONTENT="${YARN_SITE_CONTENT}\n\t<property>\n\t\t<name>yarn.resourcemanager.recovery.enabled</name>\n\t\t<value>true</value>\n\t</property>"
        YARN_SITE_CONTENT="${YARN_SITE_CONTENT}\n\t<property>\n\t\t<name>yarn.resourcemanager.ha.automatic-failover.enabled</name>\n\t\t<value>true</value>\n\t</property>"
        YARN_SITE_CONTENT="${YARN_SITE_CONTENT}\n\t<property>\n\t\t<name>yarn.resourcemanager.cluster-id</name>\n\t\t<value>cluster1</value>\n\t</property>"
        INPUT_YARN_SITE_CONTENT=$(echo $YARN_SITE_CONTENT | sed '"'"'s/\//\\\//g'"'"')
        sed -i "/<\/configuration>/ s/.*/${INPUT_YARN_SITE_CONTENT}\n&/" $HADOOP_CONF_DIR/yarn-site.xml

        # Start YARN resourcemanager
        echo "Starting YARN resource manager daemon."
        $HADOOP_HOME/bin/yarn resourcemanager
    ' >> docker/hadoop/resourcemanager/entrypoint.sh 
fi


# HBASE #
HBASE_MASTERreplicas=$(cat kubernetes/helm/hbase-master/values.yaml | grep replica | awk {'print $2'})
# Update for HBASE Master & HBASE Regionserver
if [[ $HDFS_NNreplicas > 1 ]] ; then
    echo '#!/bin/bash

        # Start the hbase master.
        echo "Starting HBASE master."

        sed -i "s/bda-hadoop-namenode\/hbase/bda-hadoop-namenode:8020\/hbase/g" ${HBASE_HOME}/conf/hbase-site.xml ##

        hbasehost=$(tail -n 1 /etc/hosts | awk "{print \$NF}")
        sed -i "s/bda-hbase-master/${hbasehost}/g" ${HBASE_HOME}/conf/hbase-site.xml
    ' > docker/hbase/master/entrypoint.sh
    echo '#!/bin/bash
        sed -i "s/bda-hadoop-namenode\/hbase/bda-hadoop-namenode:8020\/hbase/g" ${HBASE_HOME}/conf/hbase-site.xml ##
        # Start the hbase regionserver.
        echo "Starting HBASE region server."

    ' > docker/hbase/regionserver/entrypoint.sh
else
    echo '#!/bin/bash

        # Start the hbase master.
        echo "Starting HBASE master."

        hbasehost=$(tail -n 1 /etc/hosts | awk "{print \$NF}")
        sed -i "s/bda-hbase-master/${hbasehost}/g" ${HBASE_HOME}/conf/hbase-site.xml
        cat ${HBASE_HOME}/conf/hbase-site.xml
        sleep 30

    ' > docker/hbase/master/entrypoint.sh
    echo '#!/bin/bash

        # Start the hbase regionserver.
        echo "Starting HBASE region server."
    
        sed -i "s/bda-hadoop-namenode\/hbase/bda-hadoop-namenode:8020\/hbase/g" ${HBASE_HOME}/conf/hbase-site.xml

        sleep 30
    ' > docker/hbase/regionserver/entrypoint.sh
fi

if [[ $HBASE_MASTERreplicas == 1 ]] ; then
# HA not enabled
     echo 'HBASE MASTER HA not enabled'
else
# HA enabled
    echo 'HBASE MASTER HA enabled'
    echo '

    ln -s $HADOOP_HOME/etc/hadoop/conf/hdfs-site.xml $HBASE_HOME/conf/hdfs-site.xml
    ' >> docker/hbase/master/entrypoint.sh
fi

if [[ $ZKreplicas == 1 ]] ; then
    echo '
        $HBASE_HOME/bin/hbase master start
    ' >> docker/hbase/master/entrypoint.sh
    echo '
        $HBASE_HOME/bin/hbase regionserver start
    ' >> docker/hbase/regionserver/entrypoint.sh
else
    zk_addr=""
    for i in $(seq 1 $ZKreplicas); do     
        zk_addr+="bda-zookeeper-master-$((${i}-1)).bda-zookeeper-master.iwnet.svc.cluster.local,"
    done
    zk_addr=${zk_addr%?}
    echo '
        sed -i "s/bda-zookeeper-master/'${zk_addr}'/g" ${HBASE_HOME}/conf/hbase-site.xml

        $HBASE_HOME/bin/hbase master start
    ' >> docker/hbase/master/entrypoint.sh
    echo '
        sed -i "s/bda-zookeeper-master/'${zk_addr}'/g" ${HBASE_HOME}/conf/hbase-site.xml
        
        $HBASE_HOME/bin/hbase regionserver start
    ' >> docker/hbase/regionserver/entrypoint.sh
fi

# SPARK #
SPARKreplicas=$(cat kubernetes/helm/spark/values.yaml | grep replica | awk {'print $2'})
if [[ $SPARKreplicas > 1 ]] ; then
# HA SPARK enabled
    echo 'SPARK HA enabled'
else
    echo 'SPARK HA not enabled'
fi

# BDA #
BDAreplicas=$(cat kubernetes/helm/bda/values.yaml | grep replica | awk {'print $2'})
if [[ $BDAreplicas > 1 ]] ; then
# HA BDA enabled
    echo 'BDA HA enabled'
else
    echo 'BDA HA not enabled'
fi
if [[ $HDFS_NNreplicas > 1 ]] ; then
    # update backend.hdfs.master.url in bda.properties
    nnAvailable=""
    for i in $(seq 1 $HDFS_NNreplicas); do
        nnAvailable+="hdfs://bda-hadoop-namenode-$((${i}-1)).bda-hadoop-namenode.iwnet.svc.cluster.local,"
    done
    nnAvailable=${nnAvailable%?}
    awk '!/backend.hdfs.master.url/' conf/bda.properties > temp && mv temp conf/bda.properties
    echo "backend.hdfs.master.url = $nnAvailable" >> conf/bda.properties
    # add backend.hdfs.clusterName in bda.properties
    awk '!/backend.hdfs.clusterName/' conf/bda.properties > temp && mv temp conf/bda.properties
    echo 'backend.hdfs.clusterName = bda-hadoop-namenode' >> conf/bda.properties
else
    awk '!/backend.hdfs.clusterName/' conf/bda.properties > temp && mv temp conf/bda.properties
fi

# KAFKA #
KAFKAreplicas=1
echo 'KAFKA HA not enabled'


# POSTGRES #
POSTGRESreplicas=$(cat kubernetes/helm/postgres/values.yaml | grep replica | awk {'print $2'})
if [[ $POSTGRESreplicas > 1 ]] ; then
# HA POSTGRES enabled
    echo 'POSTGRES HA enabled'
    # update url of primary postgres instance in bda.properties
    sed -i "s/bda-postgres:/bda-postgres-primary.iwnet.svc.cluster.local:/g" conf/bda.properties    
else
    echo 'POSTGRES HA not enabled'
    # update url of unique postgres instance in bda.properties
    sed -i "s/bda-postgres-primary.iwnet.svc.cluster.local:/bda-postgres:/g" conf/bda.properties    
fi

###### DOCKER BUILD TO ALL K8S NODES WITH LATEST CODE
cd docker/ && make && cd .. 

###### (ADDED) BUILD K8S OPERATOR NETWORK OBSERVER
docker build \
    --tag iwnet/bda-network-observer:latest \
    ./docker/network-observer

#cd docker/ && make push && cd .. 

###### (ADDED) BUILD JOURNALNODE IMAGE FOR HADOOP NAMENODE HA
if [[ $HDFS_NNreplicas > 1 ]] ; then
    HADOOP_VERSION=$(cat docker/.env | grep HADOOP_VERSION | cut -d "=" -f2)
    docker build \
        --build-arg HADOOP_VERSION=$HADOOP_VERSION \
        --tag iwnet/hadoop-journalnode-$HADOOP_VERSION \
        ./docker/hadoop/journalnode 
fi

###### DELETE OLD DOCKER IMAGES
docker image rm $(docker image ls|grep "<none>"|awk '$2=="<none>" {print $3}') #&& 

###### START BDA ON KUBERNETES CLUSTER WITH HELM
helm install network-observer kubernetes/helm/network-observer -n iwnet

#Start metrics-server for HPA to work
#helm install metrics-server kubernetes/helm/metrics-server
helm install zookeeper kubernetes/helm/zookeeper -n iwnet
#sleep 5

if [[ $HDFS_NNreplicas > 1 ]] ; then
    helm install hadoop-journalnode kubernetes/helm/hadoop-journalnode -n iwnet
    sleep 10
fi
helm install bda kubernetes/helm/bda -n iwnet
helm install hadoop-namenode kubernetes/helm/hadoop-namenode -n iwnet
sleep 10
helm install hadoop-datanode kubernetes/helm/hadoop-datanode -n iwnet
sleep 5
helm install hadoop-yarn-rm kubernetes/helm/hadoop-yarn-rm -n iwnet
sleep 5
helm install spark kubernetes/helm/spark -n iwnet
helm install hbase-master kubernetes/helm/hbase-master -n iwnet
sleep 5
helm install hbase-regionserver kubernetes/helm/hbase-regionserver -n iwnet
sleep 5
helm install livy kubernetes/helm/livy -n iwnet

if [[ $POSTGRESreplicas > 1 ]] ; then
    helm install pgo kubernetes/helm/postgres-operator/pgo -n iwnet
    # Create configmap to execute sql when bootstraping postgresql db
    kubectl -n iwnet create configmap bootstrap-sql --from-file=init.sql=./docker/postgres/bootstrap-postgres.sql
    sleep 15
    helm install postgres kubernetes/helm/postgres-operator/postgres -n iwnet
else
    helm install postgres kubernetes/helm/postgres -n iwnet
fi
sleep 10
helm install keycloak kubernetes/helm/keycloak -n iwnet
sleep 25
helm install kafka kubernetes/helm/kafka -n iwnet

eval $(minikube docker-env -u)
