#!/bin/bash

TOMCAT=tomcat

cd run_directory

/bin/su -c "java  -jar  ../target/text-service-backend-1.0.one-jar.jar" $TOMCAT


