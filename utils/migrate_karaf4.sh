#!/bin/bash

FEATURES=$1
DIR=$2

XMLSTAR_VERSION=$(xml --version)

if [ -z "$XMLSTAR_VERSION" ] ; then
    echo "xmlstarlet is REQUIRED to run this script."
    exit -1
fi

feature_headless=$(sed -E 's/(xmlns|xsi)(:[a-zA-Z]+)?=".*"//g' $FEATURES)
modules=$(echo $feature_headless | xml sel -t -m 'features/feature' -v '@name' -n)
for module in $modules; do
    # Generate feature pom for each module
    echo ">>> Generate module $module"
    module_content=$(echo $feature_headless | xml sel -t -c 'features/feature[@name="'$module'"]')
    description=$(echo $module_content | xml sel -t -v 'feature/@description')
    mkdir -p $DIR/$module
    touch $DIR/$module/pom.xml
    echo '<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright (c) 2015 Yale University and others. All rights reserved.
This program and the accompanying materials are made available under the
terms of the Eclipse Public License v1.0 which accompanies this distribution,
and is available at http://www.eclipse.org/legal/epl-v10.html INTERNAL
-->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>org.opendaylight.odlparent</groupId>
    <artifactId>single-feature-parent</artifactId>
    <version>1.8.0-SNAPSHOT</version>
    <relativePath/>
  </parent>

  <groupId>org.opendaylight.alto</groupId>
  <artifactId>'$module'</artifactId>
  <version>0.4.0-SNAPSHOT</version>
  <packaging>feature</packaging>

  <name>'$description'</name>
' > $DIR/$module/pom.xml
    echo $module_content | sed '
    sX<feature version=['\''"]\([^'\''"]*\)['\''"]>\([^<]*\)</feature>X<dependency><groupId>${project.groupId}</groupId><artifactId>\2</artifactId><version>\1</version><type>xml</type><classifier>features</classifier></dependency>Xg
sX<bundle>mvn:\([^/]*\)/\([^/]*\)/Template:VERSION</bundle>X<dependency><groupId>\1</groupId><artifactId>\2</artifactId></dependency>Xg
sX<bundle>wrap:mvn:\([^/]*\)/\([^/]*\)/Template:VERSION</bundle>X<dependency><groupId>\1</groupId><artifactId>\2</artifactId></dependency>Xg
sX<bundle>mvn:\([^/]*\)/\([^/]*\)/\([^<]*\)</bundle>X<dependency><groupId>\1</groupId><artifactId>\2</artifactId><version>\3</version></dependency>Xg
sX<bundle>wrap:mvn:\([^/]*\)/\([^/]*\)/\([^<]*\)</bundle>X<dependency><groupId>\1</groupId><artifactId>\2</artifactId><version>\3</version></dependency>Xg
    ' | xml ed -O -d 'feature/@*' -r 'feature' -v 'dependencies' >> $DIR/$module/pom.xml
    echo '</project>' >> $DIR/$module/pom.xml
done
