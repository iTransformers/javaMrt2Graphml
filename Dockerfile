from maven:3.5.2-jdk-8
RUN apt-get update -y && apt-get install -y vim bzip2
RUN git clone https://github.com/iTransformers/javaMrt2Graphml.git
RUN cd javaMrt2Graphml && mvn clean install -DskipTests=true
ADD entrypoint.sh /javaMrt2Graphml/entrypoint.sh
WORKDIR /javaMrt2Graphml
ENTRYPOINT ./entrypoint.sh
