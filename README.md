# docker-spark

## Overview
This is a learner project to understand how to write a Spark dataset to ElasticSearch and read it.

1. git clone https://github.com/panggd/docker-hadoop.git and run init-hadoop script
2. Run the init-elasticsearch script to deploy a docker container of Spark and ElasticSearch
3. Build the Java based Spark-ElasticSearch application to a Jar file
4. Docker cp the Jar file and CSV dataset to the container
5. Run Jar to process CSV dataset
6. Read results

## Tech stack
- Docker
- ElasticSearch
- Spark
- Java
- Gradle

## data
This folder consists of a CSV dataset that describes the total attendance group by medical institutions and year.

## elasticsearch
This folder consists of a Spark-ElasticSearch application that will process the CSV dataset to return the total attendance group by medical institutions.

## docker-compose.yml
This is a script that will deploy the Spark and ElasticSearch docker images in a container.

## Prerequsites

### Download and install Docker. Follow the below guides.
https://docs.docker.com/install

### Container resource requirements
You need to allocate more cpu and memory to each container for this project to work.
I set mine to 4 cpu cores, 4gb ram in this project.


## How to run

### Start your docker daemon
This is really depend on your OS. For my case, it is just starting the Docker app.

### Deploy docker-hadoop
```bash
git clone https://github.com/panggd/docker-hadoop.git
./init-hadoop
```

### Deploy Spark and ElasticSearch containers
This will deploy the docker container holding Spark and ElasticSearch.
```bash
docker-compose up -d
```

### Build the Spark-ElasticSearch application
Use your favorite IDE and build the jar in the spark folder.
```bash
# go to the output jar folder
zip -d spark.jar META-INF/*.RSA META-INF/*.DSA META-INF/*.SF
```

### Copy the Jar and dataset into the Hadoop & Spark container
```bash
# Go to data folder and copy the dataset to hadoop namenode container
# bash into namenode container
# copy the dataset to hdfs
docker cp hospital-and-outpatient-attendances.csv \
<hadoop_namenode_container_id>:hospital-and-outpatient-attendances.csv

docker exec -it <hadoop_namenode_container_id> bash

hdfs dfs -mkdir /data
hdfs dfs -put hospital-and-outpatient-attendances.csv /data/

# Go to spark folder
docker cp elasticsearch.jar <spark_master_container_id>:elasticsearch.jar
```

### Process the dataset and enjoy the output results
```bash
# Get into the Hadoop cluster server
docker exec -it <spark_master_container_id> bash

# Process the dataset
java -cp elasticsearch.jar SparkElasticApplication hdfs://namenode:9000/data/hospital-and-outpatient-attendances.csv
```

## Housekeeping
Here are some housekeeping tips if you are on a low memory resource machine like me.

```bash
# This is to have a clean state of your docker environment
docker stop $(docker ps -a -q) && \
docker system prune -a
```

## TODO
1. Create and integrate a REST API
3. Extract the output result to the REST API