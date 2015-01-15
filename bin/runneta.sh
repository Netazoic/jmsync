#!/bin/sh
set NETA_PATH=./build/netampg.jar
set CLASSPATH="$NETA_PATH:$CLASSPATH"
java -classpath "build/netampg.jar" com.netazoic.netampg.EncodeS4S
