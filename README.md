javaMrt2Graphml
========

#1. Overview

This is an extension of the original javaMrt library able parse the binary MRT format to export the information from those in structured xml or directly in Graphml file formats. 

For those that have no idea what  MRT is  - this is a Multi-Threaded Routing Toolkit (MRT) Routing Information Export file format described in RFC(Request for Comments) 6396. To state that simply this is a file format in which routers export their routing tables. 

So we have built on top of the initial javaMrt and has converted initially the output of those files into a structured xml and secondly directly into a graphml files. 
The graphmls is able to analyze BGP routing updates, to extact the AS paths and to build beautiful graphs of the world Internet. 
In addition we use also the data soure from Geoff's Huston poratoo net IRR server (whois) data export http://bgp.potaroo.net/cidr/autnums.html in order to populate our graphml with some useful data about the BGP autonomus systems. 

#2. License
javaMrt2Graphml library as the original javaMrt is released under LGPL license. For more details please read LICENSE.txt.

#3. Usage 

##3.1 Clone the project
```
git clone https://github.com/iTransformers/javaMrt2Graphml.git
```
##3.2 Built it with maven
```
cd javaMrt2Graphml
mvn package 
```
Here we assume that you know what is maven. If you don't try that one first http://maven.apache.org/guides/getting-started/maven-in-five-minutes.html

##3.3 Execute run.sh 
```
./run.sh -f rib.20140401.0000 -f2 out-mrt.xml -o network.graphml

-f is the origiranl mrt file that has been exported by your router
-f2 is the output of that file in xml
-o is the network graphml 
```

If you don't have your own mrt file (obviously not everybody has a router with full Internet BGP routing table) you can get one from the RouteViews project 
```
http://archive2.routeviews.org/route-views.isc/bgpdata/2014.04/RIBS/
```
Once you download it you have to unzip it and pass the unziped file as an "-f" argument. 


##4.4 Visualize the result
The final result (network.graphml) could be visualized with gephi or [netTransformer](https://github.com/iTransformers/netTransformer) or any other Graph Viz tool able to draw graphml file format. 

The result of this are Internet wide maps like those bellow:

![Small](http://www.itransformers.net/bgpPeeringMap/internet_iTr_small.png)


![Big](http://www.itransformers.net/bgpPeeringMap/internet_iTr.png)
