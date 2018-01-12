#!/bin/bash

CLASSPATH=target/java-mrt-1.0-jar-with-dependencies.jar;
JAVA_OPTS=
java -Xms2024m -Xmx4048m  -cp $CLASSPATH org.javamrt.dumper.Route2GraphmlDumper $*
