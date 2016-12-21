##############################################################################
# Copyright (c) 2016 SNLab. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v1.0 which accompanies this distribution,
# and is available at http://www.eclipse.org/legal/epl-v10.html
##############################################################################
#!/bin/bash

fix_namespace() {
	FILE=$1
	CATEGORY=$2

	sed -i "s/urn:opendaylight:alto:$CATEGORY/urn:alto/g" $FILE
}

fix_package() {
	FILE=$1
	CATEGORY=$2

	sed -i "s/urn.opendaylight.alto.$CATEGORY/urn.alto/g" $FILE
}

fix_folder() {
	CATEGORY=$1

	for d in $(find . -wholename "*/urn/opendaylight/alto/$CATEGORY"); do
		target=$(echo $d | sed "s/opendaylight\/alto\/$CATEGORY/alto/g")
		mkdir -p $target
		mv $d/* $target
		rm -rf $d
	done
}


for suffix in core basic ext; do
	for f in $(find . -name '*.xml'); do
		fix_namespace $f $suffix
	done

	for f in $(find . -name '*.yang'); do
		fix_namespace $f $suffix
	done

	for f in $(find . -name '*.java'); do
		fix_package $f $suffix
	done

	fix_folder $suffix
done
