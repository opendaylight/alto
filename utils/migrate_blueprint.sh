##############################################################################
# Copyright (c) 2016 SNLab. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v1.0 which accompanies this distribution,
# and is available at http://www.eclipse.org/legal/epl-v10.html
##############################################################################
#!/bin/bash

recursive_remove() {
    DIR=$1

    rmdir $DIR
    while [ $? = 0 ]; do
        DIR=$(dirname $DIR)
        if [ $DIR = . ]; then
            break
        fi
        rmdir $DIR
    done
}

migrate_blueprint() {
    pushd $1

    PROVIDER=$(grep -rnl src/main/java -e '^\W*public void onSessionInitiated.*$')
    PROVIDER_CLASS=$(echo $PROVIDER | sed 's_src/main/java/__;s/.java//;s_/_._g')
    PROVIDER_NAME=$(basename $PROVIDER | sed 's/.java//')
    PACKAGE=$(dirname $PROVIDER | sed 's_src/main/java/__;s_/_._g')

    #----------------------
    # extract flags
    DB_FLAG=$(grep 'DataBroker' $PROVIDER)
    RPC_FLAG=$(grep 'RpcRegistration' $PROVIDER)
    ROUTED_RPC_FLAG=$(grep 'RoutedRpcRegistration' $PROVIDER)
    RPC_CONSUME_LIST=$(grep 'getRpcService' $PROVIDER | \
        sed 's/^\W*\([a-zA-Z_]\+\) *=.*getRpcService( *\([a-zA-Z]\+\)\.class *).*$/\1,\2/')

    #----------------------
    # remove redundancy
    rm -rf src/main/config src/main/yang
    for mf in $(find src/main -name '*Module.java'); do
        rm $mf
        recursive_remove $(dirname $mf)
    done;

    for mf in $(find src/main -name '*ModuleFactory.java'); do
        rm $mf
        recursive_remove $(dirname $mf)
    done;

    for mf in $(find src/test -name '*ModuleTest.java'); do
        rm $mf
        recursive_remove $(dirname $mf)
    done;

    for mf in $(find src/test -name '*ModuleFactoryTest.java'); do
        rm $mf
        recursive_remove $(dirname $mf)
    done;

    #----------------------
    # insert blueprint
    BLUEPRINT_DIR=src/main/resources/org/opendaylight/blueprint
    mkdir -p $BLUEPRINT_DIR
    echo '<?xml version="1.0" encoding="UTF-8"?>
<!-- vi: set et smarttab sw=4 tabstop=4: -->
<!--
Copyright © 2016 SNLab and others. All rights reserved.

This program and the accompanying materials are made available under the
terms of the Eclipse Public License v1.0 which accompanies this distribution,
and is available at http://www.eclipse.org/legal/epl-v10.html
-->
<blueprint xmlns="http://www.osgi.org/xmlns/blueprint/v1.0.0"
  xmlns:odl="http://opendaylight.org/xmlns/blueprint/v1.0.0"
  odl:use-default-for-reference-types="true">' > $BLUEPRINT_DIR/impl-blueprint.xml

    ## insert dataBroker
    if [[ $DB_FLAGS ]]; then
        echo '  <reference id="dataBroker"
    interface="org.opendaylight.controller.md.sal.binding.api.DataBroker"
    odl:type="default" />' >> $BLUEPRINT_DIR/impl-blueprint.xml
    fi

    ## insert rpcRegistry
    ## TODO:

    ## insert routedRpcRegistry
    ## TODO:

    ## insert rpcService
    for rpc in $RPC_CONSUME_LIST; do
        SERV_NAME=$(echo $rpc | sed 's/^.*,//')
        SERV_CLASS=$(grep "^import .*\.$SERV_NAME;" $PROVIDER | sed 's/^import \(.*\);$/\1/')
        if [[ $SERV_CLASS ]]; then
            SERV_CLASS="$PACKAGE.$SERV_NAME"
        fi
        SERV_ID=$(echo ${SERV_NAME:0:1} | tr '[:upper:]' '[:lower:]')${SERV_NAME:1}
        echo "  <odl:rpc-service id='$SERV_ID'
    interface='$SERV_CLASS' />" >> $BLUEPRINT_DIR/impl-blueprint.xml
    done

    ## insert provider
    echo '  <bean id="provider"
    class="'$PROVIDER_CLASS'"
    init-method="init" destroy-method="close">' >> $BLUEPRINT_DIR/impl-blueprint.xml
    if [[ $DB_FLAGS ]]; then
        echo '    <property name="dataBroker" ref="dataBroker" />' >> $BLUEPRINT_DIR/impl-blueprint.xml
    fi
    for rpc in $RPC_CONSUME_LIST; do
        SERV_VAL=$(echo $rpc | sed 's/,.*$//')
        SERV_NAME=$(echo $rpc | sed 's/^.*,//')
        SERV_ID=$(echo ${SERV_NAME:0:1} | tr '[:upper:]' '[:lower:]')${SERV_NAME:1}
        echo "    <property name='$SERV_VAL' ref='$SERV_ID' />" >> $BLUEPRINT_DIR/impl-blueprint.xml
    done
    echo '  </bean>
</blueprint>' >> $BLUEPRINT_DIR/impl-blueprint.xml

    #----------------------
    # refactor init class
    sed -i '
        s/^import .*ProviderContext;\n$//
        s/^import .*BindingAwareProvider;\n$//
        /@Override/{
            N
            s/^\W*@Override\n *public void onSessionInitiated.*/    public void init() {/
            s/^\W*@Override\n\( *public void close().*\)/\1/
        }
        s/ BindingAwareProvider,\?//
        s/ AutoCloseable,\?//
        s/implements *{/{/
    ' $PROVIDER

    ## refactor dataBroke
    if [[ $DB_FLAG ]]; then
        ### refactor dataBroker variable name
        DB_VAL=$(grep '^\W*private .* DataBroker' $PROVIDER | \
            sed 's/^\W*private.* DataBroker *\([a-zA-Z_]\+\).*$/\1/')
        sed -i "
            s/^.*DataBroker.*$//
            /^package/a\\\n\nimport org.opendaylight.controller.md.sal.binding.api.DataBroker;
            /private static final Logger/a\\\n    private DataBroker dataBroker = null;
            /public void init()/i\    public void setDataBroker(DataBroker dataBroker) {\n        this.dataBroker = dataBroker;\n    }\n
            s/$DB_VAL/dataBroker/g
        " $PROVIDER
    fi

    ## refactor rpcService
    sed -i 's/^\W*\([a-zA-Z_]\+\) *=.*getRpcService.*$//' $PROVIDER
    for rpc in $RPC_CONSUME_LIST; do
        SERV_VAL=$(echo $rpc | sed 's/,.*$//')
        SERV_NAME=$(echo $rpc | sed 's/^.*,//')
        SERV_SET_VAL=set$(echo ${SERV_VAL:0:1} | tr '[:lower:]' '[:upper:]')${SERV_VAL:1}
        sed -i "
            /public void init()/i\    public void $SERV_SET_VAL(final $SERV_NAME $SERV_VAL) {\n        this.$SERV_VAL = $SERV_VAL;\n    }\n
        " $PROVIDER
    done

    popd
}

usage() {
    echo "Usage: $0 [option]"
    echo ""
    echo "Options:"
    echo "    all        migrate all subprojects"
    echo "    <dir>      only migrate subprojects in <dir>"
    echo "    help       display help information"
}

if [ -z $1 ]; then
    usage
    exit 0
fi

case $1 in
    all)
        # Migrate all subproject
        cd $(dirname $0)/..
        PROJECT_ROOT=$(pwd)

        for suffix in alto-{core,basic,extensions}; do
            for f in $(find $PROJECT_ROOT -name pom.xml); do
                if [ $(basename $(dirname $f)) = impl ]; then
                    migrate_blueprint $(dirname $f)
                fi
            done
        done
    ;;
    help)
        usage
        ;;
    *)
        # Migrate a specific directory
        for f in $(find $1 -name pom.xml); do
            if [ $(basename $(dirname $f)) = impl ]; then
                migrate_blueprint $(dirname $f)
            fi
        done
esac
