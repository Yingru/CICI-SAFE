#!/bin/bash

#apt-get update;
#apt-get -y install openvswitch-switch;
#apt-get -y install iperf;
#/etc/init.d/neuca stop;
#check if there is a bridge, if yes skip
# if a new interface is not added to the ovsbridge, then add it and set controller again
string=$(ifconfig -a);
if [[ $string == *"br0"* ]]; then
    :
else
  ovs-vsctl add-br br0
  ovs-vsctl set Bridge br0 protocols=OpenFlow10,OpenFlow11,OpenFlow12,OpenFlow13,OpenFlow14,OpenFlow15
  ovs-vsctl set-manager ptcp:6632
  ovs-vsctl set-controller br0 tcp:$1
fi

manageif=$(ifconfig -a| grep -B1 "inet 10.\|inet addr:10." | awk '$1!="inet" && $1!="--" {print $1}')

interfaces=$(ifconfig -a|grep "ens\|eth"|grep -v "$manageif"|sed 's/[ :\t].*//;/^$/d')

brinterfaces=$(ovs-ofctl show br0)
vsinterfaces=$(ovs-vsctl show)

newport=0
while read -r line; do
  if [[ $brinterfaces == *"$line"* ]]; then
  :
  else
    ifconfig $line up
    ifconfig $line 0
    if [[ $vsinterfaces == *"$line"* ]]; then
        ovs-vsctl del-port br0 $line
        ovs-vsctl add-port br0 $line
    else
        ovs-vsctl add-port br0 $line
    fi
  fi
done <<< "$interfaces"
dpid=$(ovs-ofctl show br0 | grep "dpid:" |cut -d: -f3)
echo $dpid


