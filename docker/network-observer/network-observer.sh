#!/bin/bash

while true
do

    PODS=$(kubectl get pods -n iwnet -o wide | grep 'bda-' | awk '{print $1}')
    IPS=$(kubectl get pods -n iwnet -o wide | grep 'bda-' | awk '{print $6 " " $8}' | sed -E 's/<none>+\ ?//g' | sed -E 's/ago\)+\ ?//g')

    echo "Running Pods: " $PODS
    echo "Their IPs:" $IPS
    echo "......"

    echo $IPS > /tmp/ip_list.txt
    #old_ips=$(head -n 1 /tmp/old_ip_list.txt)
    #for ip in $old_ips; do
    #    if [ "$ip" == "$yourValue" ] ; then
    #        echo "Found"
    #    fi
    #
    #done

    diff -q /tmp/old_ip_list.txt /tmp/ip_list.txt 1>/dev/null
    if [[ $? == "0" ]]
    then
    echo "IPs are the same."
    else
    echo "IPs are not the same. Updating hostnames..."

        i=1
        for pod in $PODS; do

            j=1
            # keep existing 10 first lines of /etc/hosts
            kubectl exec -it $pod -n iwnet -- sh -c "echo '\n\n\n\n\n' >> /etc/hosts"
            kubectl exec -it $pod -n iwnet -- sh -c "head -n 15 /etc/hosts > /etc/tempfile && cat /etc/tempfile > /etc/hosts"

            for other_pod in $PODS; do
                if [[ "$other_pod" != "$pod" ]]
                then
                    cur_pod_hostname=${other_pod#*/}
                    cur_ip=$(echo $IPS | awk '{print $'$j'}')
                    echo "{" $cur_ip "-" $cur_pod_hostname "} added at Pod" $pod
                    kubectl exec -i $pod -n iwnet -- sh -c "echo '$cur_ip $cur_pod_hostname' >> /etc/hosts"
                    j=$((j+1))
                # avoid adding hostname-ip combination for the same pod since it already exists in /etc/hosts
                else
                    j=$((j+1))
                fi
            #j=$((j+1))
            done

            i=$((i+1))
        done
        echo $IPS > /tmp/old_ip_list.txt
        echo "Hostnames-IPs added in /etc/hosts for every running Pod."

    fi

    sleep 15
done

