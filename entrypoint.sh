#!/bin/bash  

for file in `ls /bgpdata/*.bz2`; do 
   graphmlfile="$file.graphml"
   if [ ! -f $graphmlfile ]; then
         echo $file; 
        ./run.sh -f $file -f2 /dev/null -o $file.graphml;
   fi 
done
