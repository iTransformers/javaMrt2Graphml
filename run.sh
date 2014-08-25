#!/bin/bash

CLASSPATH=.:./target/java-mrt-1.0.jar:~/.m2/repository/org/apache/commons/commons-compress/1.0/commons-compress-1.0.jar
JAVA_OPTS=
echo $CLASSPATH
java -Xms1024m -Xmx3000m -cp $CLASSPATH org.javamrt.dumper.Route2GraphmlDumper $*
