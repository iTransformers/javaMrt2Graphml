#!/bin/bash

CLASSPATH=./target/java-mrt-1.0.jar:~/.m2/repository/org/apache/commons/commons-compress/1.0/commons-compress-1.0.jar
JAVA_OPTS=
java -Xms1024m -Xmx2048m  -cp $CLASSPATH org.javamrt.dumper.Route2GraphmlDumper -f rib.20140401.0000 -f2 out-mrt.xml -o network.graphml
