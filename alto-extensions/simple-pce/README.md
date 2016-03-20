# ALTO SPCE (Simple Path Computation Engine)

The ALTO SPCE module provides a simple path computation engine for ALTO and other projects. It works in the
OpenDaylight(ODL) SDN Controller.

## Installation

One prerequisite for installing ALTO SPCE is that the ODL development environment should be setup by
following [this link](https://wiki.opendaylight.org/view/GettingStarted:Development_Environment_Setup).

With this prerequisite satisfied, execute `mvn clean install` in the directory of alto-spce project
to start the installation. After the installation, you can execute `./karaf/target/assembly/bin/karaf`
to start the alto-spce with ODL.

## Deployment

You can also deploy this module into a running ODL controller without stopping the controller, by
running `./deploy.sh <distribution_directory>`, where `<distribution_directory>` is the path of your own running ODL distribution.

For example, if you start your ODL controller from `/root/distribution-karaf-0.4.0-SNAPSHOT/bin/karaf`, you can use the command `./deploy.sh /root/distribution-karaf-0.4.0-SNAP`.

> **NOTE:** If you have checked out the latest commit in master branch, you will need a `karaf-0.4.1-SNAPSHOT` distribution rather than `karaf-0.4.0-SNAPSHOT`.

And then, you can check whether the features of alto-spce are loaded in your karaf shell as follows:

```
karaf@root()> feature:list | grep alto-spce
```

If the features are loaded, you can install them by:

```
karaf@root()> feature:install odl-alto-spce
```

**Tip:**

```
karaf@root()> log:tail
```

You could use this command to get the tailing log. If you see **AltoSpceProvider Session Initiated!**,
it means that alto-spce has been installed successfully. 

## Usage

### Prerequisites

Please make sure that your have configured **l2switch** correctly. Two .xml files below could be found in `/karaf/target/assembly/etc/opendaylight/karaf`.

In **54-arphandler.xml**, please set

```
<is-proactive-flood-mode>false</is-proactive-flood-mode>
```

In **58-l2switchmain.xml**, please set

```
<is-learning-only-mode>true</is-learning-only-mode>
```

### Creating a network using Mininet

```
sudo mn --controller=remote,ip=<Controller IP> --mac --topo=linear,3 --switch ovsk,protocols=OpenFlow13
```

The command above will create a virtual network consisting of 3 switches. And one host is attached to each switch.

### Discover hosts

l2switch uses ARP packets to discover hosts. 

If we use mininet, we could use `ping` to let l2switch get the ARP packets it needs. 

```
mininet> h1 ping h5
```

After this command, l2switch will discover host1 and host5.


### Setup/Remove a path with python-odl library

We have forked [python-odl](https://github.com/SPRACE/python-odl) project to support alto-spce. You could get the code [here](https://github.com/snlab/python-odl).

```
＃ Import essential modules.
>>> import odl.instance
>>> import odl.altospce

# Initiate a ODLInstance object.
>>> myodl = odl.instance.ODLInstance("http://127.0.0.1:8181",("admin","admin"))

# Initiate a ALTOSpce object.
>>> myaltospce = odl.altospce.ALTOSpce(server="http://127.0.0.1:8181",credentials=("admin","admin"),odl_instance=myodl)

# Setup a path between host1(10.0.0.1) and host5(10.0.0.5)
>>> myaltospce.path_setup(src="10.0.0.1",dst="10.0.0.5",objective_metrics=["bandwidth"])
{'path': [u'10.0.0.5|openflow:6:3|openflow:5:3|openflow:1:1|openflow:2:1|openflow:3:1|10.0.0.1', u'10.0.0.1|openflow:3:3|openflow:2:3|openflow:1:2|openflow:5:1|openflow:6:1|10.0.0.5'], 'error-code': 'OK'}

# Remove the path between host1(10.0.0.1) and host5(10.0.0.5)
# To identify the path please use the 'path' indicated in myaltospce.path_setup
>>> myaltospce.path_remove(["10.0.0.5|openflow:6:3|openflow:5:3|openflow:1:1|openflow:2:1|openflow:3:1|10.0.0.1","10.0.0.1|openflow:3:3|openflow:2:3|openflow:1:2|openflow:5:1|openflow:6:1|10.0.0.5"])
{'error-code': 'OK'}
```

Enjoy your alto-spce!

## Try the demo system out

We have deploy a demo system in http://alto.yale.edu:8181/index.html and you can try it out. There is a brief usage:

You can setup or remove a path by using `python-odl` library. Just follow the section ["Setup/Remove a path with python-odl library"](#setupremove-a-path-with-python-odl-library) and replace `127.0.0.1` by `alto.yale.edu`.

Also you can send a HTTP request like the following template to query ALTO Endpoint Cost Service (ECS):

```
curl -X POST -H "Content-type: application/alto-endpointcostfilter+json" \
    -d '{"cost-type":{"cost-mode":"numerical","cost-metric":"hopcount"},"endpoints":{"srcs":[<SOURCE_IP_LIST>],"dsts":[<DESTINATION_IP_LIST>]}}' \
    http://alto.yale.edu:8181/alto/endpointcost/default
```
