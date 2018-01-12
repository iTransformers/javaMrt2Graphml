#!/bin/bash

CLASSPATH=./target/java-mrt-1.0-jar-with-dependencies.jar;
JAVA_OPTS= -Xms2024m -Xmx4048m
java $JAVA_OPTS -jar  $CLASSPATH  $*
