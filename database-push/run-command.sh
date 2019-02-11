#!/bin/bash

TOMCAT=tomcat

cd run_directory

# /bin/su -c "java  -jar  ../target/repository-pull-1.0.one-jar.jar" $TOMCAT
/bin/su -c "java  -cp  '../target/repository-pull-1.0.one-jar.jar:../target/repository-pull-1.0.jar'  dk.kb.pullStuff.RunLoad" $TOMCAT
