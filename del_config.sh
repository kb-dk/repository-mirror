#!/bin/bash

#
# removes the config file from whereever it was needed when building.
# 


rm database-push/src/main/resources/config.xml
rm repository-pull/src/main/resources/config.xml
rm repository-mirror-web/src/main/resources/config.xml