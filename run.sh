#!/bin/bash

CLASSPATH=/Users/niau/Projects/java-mrt/target/java-mrt-1.0.jar:~/.m2/repository/org/apache/commons/commons-compress/1.0/commons-compress-1.0.jar
JAVA_OPTS=
java -Xms1024m -Xmx2048m  -cp $CLASSPATH org.javamrt.dumper.Route2GraphmlDumper $*
