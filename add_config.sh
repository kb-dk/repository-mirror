#!/bin/bash

#
# use to copy the config file in place whereever it is needed.
# 


if [ -f "$1" ]; then
    CONF=$1
else
    CONF=config_secret.xml
fi


#ln -s $CONF database-push/src/main/resources/config.xml
#ln -s $CONF repository-pull/src/main/resources/config.xml
#ln -s $CONF repository-mirror-web/src/main/resources/config.xml


cp $CONF database-push/src/main/resources/config.xml
cp $CONF repository-pull/src/main/resources/config.xml
cp $CONF repository-mirror-web/src/main/resources/config.xml
