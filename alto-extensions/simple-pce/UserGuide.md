# ALTO SPCE (Simple Path Computation Engine) User Guide

The ALTO SPCE module provides a simple path computation engine for ALTO and other projects. It works in the
OpenDaylight(ODL) SDN Controller.

## Step 1. Download alto-spce kar

```
$ wget https://github.com/snlab/alto-spce/releases/download/1.0.0/alto-spce-features-1.0.0-SNAPSHOT.kar
```

## Step 2. Deploy alto-spce into a running OpenDaylight controller
```
$ DISTRO_DIR=<odl_distribution_directory>
$ cp alto-spce-features-1.0.0-SNAPSHOT.kar $DISTRO_DIR/deploy/
$ mkdir tmp
$ unzip alto-spce-features-1.0.0-SNAPSHOT.kar -d tmp
$ cp -r tmp/repository/org $DISTRO_DIR/system/
$ rm -rf tmp
```

## Sept 3. Install the newer version of python-odl to include the two alto-spce methods:
```
$ git clone https://github.com/snlab/python-odl
$ cd python-odl
$ sudo python setup.py install
```

## Step 4. Run python-odl
```
$ python
>>> import odl.instance
>>> import odl.altospce

# setup server
>>> serverURL = "http://140.221.143.143:8080"
>>> myodl = odl.instance.ODLInstance(serverURL, ("admin", "admin"))
>>> myaltospce = odl.altospce.ALTOSpce(server=serverURL, credentials=("admin", "admin"), odl_instance=myodl)

# Set up a path
>>> srcIP = "198.188.136.11"
>>> dstIP = "198.188.136.21"
>>> mypath = myaltospce.path_setup(src=srcIP, dst=dstIP, objective_metrics=["hopcount"], constraint_metric=[{"metric": "hopcount", "min": 1, "max": 3}])

# This command should return the path setup. To see it, 
>>> print mypath['path']
# You should see something like ["198.188.136.11|openflow:365545302388672:185|198.188.136.21",  "198.168.136.21|openflow:365545302388672:185|198.168.136.11"]

# Remove the path setup above, for example, after showing the path on UI,
>>> myaltospce.path_remove(mypath['path'])
```
