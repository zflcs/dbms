# Distributed Database Systems - Project Manual

## Authors

Fangliang Zhao [2022210860](mailto:zfl22@mails.tsinghua.edu.cn), Hao Liu [2022210863](mailto:lh22@mails.tsinghua.edu.cn)

## Network Setup

In order to ensure normal communication between each docker container, we create a subnet:
```
docker network create --subnet=172.20.0.0/16 hnet
```

## DataSet Generatation

Before setting up the container environment, we can run the instructions to generate the data set:

```
python .\genTable_mongoDB10G.py
```

## Hadoop Distributed File System Setup

For this setup, we should firstly pull the image from DockerHub, then double-click the `start-container.bat` script. It will automatically create a master and two slave nodes and assign fixed ip addresses.

```
docker pull zflcs/hadoop-cluster:1.0
```

Before starting hadoop, we need copy the data set locally into the container:

```
docker cp .\db-generation\articles nn:/home/hadoop 
```

All 3 hadoop nodes are now running. Then We go inside the master node for the following setup instructions. For this, run the following command:

```
docker exec -it nn su hadoop
```

Now that we are inside the master node, we may run the following script for starting the all the HDFS servers:

```
start-all.sh
```

Now that We firstly copy the data set into the container. Then we can upload the articles to the DFS as follows:

```
hdfs dfs -mkdir -p articles
hdfs dfs -put ./articles/* articles
```

The setup process for the Hadoop Distributed File System is now finished and one may exit the master node and return to the main directory:

```
exit
```

## MongoDB Sharded Cluster Setup

To begin with the setup of our MongoDB Cluster, we should run our two config servers on docker containers, by running the following command on the mongodb directory:

```
cd mongo-cluster
docker-compose -f config.yml up -d
```

In order to initiate the config server replica set we should log into one of the config servers (will be selected as the primary config server):

```
docker exec -it configsvr0 mongosh
```

Then, initiate the replica set by running this mongodb command and exit afterwards:

```
rs.initiate(
  {
    _id: "cfgrs",
    configsvr: true,
    members: [
      { _id : 0, host : "172.20.1.5:27017" },
      { _id : 1, host : "172.20.1.6:27017" }
    ]
  }
)

exit
```

Config server are done at this moment. Now, we should start running the shards:

```
docker-compose -f shards.yml up -d
```

After checking if every docker container is running, we should initiate every shard replica set. Since we have three replica sets, we have to do it three times, for every replica set primary shard:

```
docker exec -it shardsvr00 mongosh

rs.initiate(
  {
    _id : "shard0",
    members: [
      { _id : 0, host : "172.20.1.7:27017" }
    ]
  }
)
db.enableFreeMonitoring()

exit
```

```
docker exec -it shardsvr01 mongosh

rs.initiate(
  {
    _id : "shardrep",
    members: [
      { _id : 0, host : "172.20.1.8:27017" },
      { _id : 1, host : "172.20.1.9:27017" }
    ]
  }
)
db.enableFreeMonitoring()

exit
```

```
docker exec -it shardsvr11 mongosh

rs.initiate(
  {
    _id : "shard1",
    members: [
      { _id : 1, host : "172.20.1.10:27017" }
    ]
  }
)
db.enableFreeMonitoring()

exit
```

Finally, we should run our last MongoDB component, the router (mongos):

```
docker-compose -f router.yml up -d
```

Since we want to configure the router, we must enter in mongos:

```
docker exec -it router mongosh
```

Inside mongos, we should run several commands as follows.

Add every shard replica set to the router:

```
sh.addShard("shard0/172.20.1.7:27017")

sh.addShard("shardrep/172.20.1.8:27017,172.20.1.9:27017")

sh.addShard("shard1/172.20.1.10:27017")
```

Name every shard replica set with MongoDB tags, so we can distinguish each other. Note: DBMS12 will be the replica set which will be present in both DBMS1 and DBMS2.

```
sh.addShardTag("shard0", "DBMS1")

sh.addShardTag("shard1", "DBMS2")

sh.addShardTag("shardrep", "DBMS12")
```

Create a new sharded database called demo:

```
use demo
sh.enableSharding("demo")
```

Create the User collection, and then define the ranges so we can allocate partitions of data, according project requirements (Beijing -> DBMS1, Hong Kong -> DBMS2):

```
sh.shardCollection("demo.user_beijing", {"uid": 1})
sh.addTagRange( 
  "demo.user_beijing",
  { "uid" : MinKey },
  { "uid" : MaxKey },
  "DBMS1"
)

sh.shardCollection("demo.user_hong_kong", {"uid": 1})
sh.addTagRange(
  "demo.user_hong_kong",
  { "uid" : MinKey },
  { "uid" : MaxKey },
  "DBMS2"
)
```

Setup the Article collection (Science -> DBMS12, Technology -> DBMS2):

```
sh.shardCollection("demo.article_science", {"aid": 1})
sh.addTagRange( 
  "demo.article_science",
  { "aid" : MinKey },
  { "aid" : MaxKey },
  "DBMS12"
)

sh.shardCollection("demo.article_tech", {"aid": 1})
sh.addTagRange(
  "demo.article_tech",
  { "aid" : MinKey },
  { "aid" : MaxKey },
  "DBMS2"
)
```

Setup the Read collection (Beijing -> DBMS1, Hong Kong -> DBMS2):

```
sh.shardCollection("demo.read_beijing", {"id": 1})
sh.addTagRange( 
  "demo.read_beijing",
  { "id" : MinKey },
  { "id" : MaxKey },
  "DBMS1"
)

sh.shardCollection("demo.read_hong_kong", {"id": 1})
sh.addTagRange(
  "demo.read_hong_kong",
  { "id" : MinKey },
  { "id" : MaxKey },
  "DBMS2"
)
```

Setup the Be-Read collection (Science -> DBMS12, Technology -> DBMS2):

```
sh.shardCollection("demo.be_read_science", {"brid": 1})
sh.addTagRange(
  "demo.be_read_science",
  { "brid" : MinKey },
  { "brid" : MaxKey },
  "DBMS12"
)

sh.shardCollection("demo.be_read_tech", {"brid": 1})
sh.addTagRange(
  "demo.be_read_tech",
  { "brid" : MinKey },
  { "brid" : MaxKey },
  "DBMS2"
)
```

Setup the Popular-Rank collection (Daily -> DBMS1, Weekly -> DBMS2, Monthly -> DBMS2):

```
sh.shardCollection("demo.popular_rank_daily", {"prid": 1})
sh.addTagRange(
  "demo.popular_rank_daily",
  { "prid" : MinKey },
  { "prid" : MaxKey },
  "DBMS1"
)

sh.shardCollection("demo.popular_rank_weekly", {"prid": 1})
sh.addTagRange(
  "demo.popular_rank_weekly",
  { "prid" : MinKey },
  { "prid" : MaxKey },
  "DBMS2"
)

sh.shardCollection("demo.popular_rank_monthly", {"prid": 1})
sh.addTagRange(
  "demo.popular_rank_monthly",
  { "prid" : MinKey },
  { "prid" : MaxKey },
  "DBMS2"
)
```

The setup process for the MongoDB Sharded Cluster is now finished and one may return to the main directory:

```
exit
cd ../
```


## Redis Setup

For this setup, we should firstly navigate to the *redis/* directory:

```
cd redis
```

We should then run our redis server on a docker container, by executing the following command:

```
docker-compose -f redis.yml up -d
```

The setup process for the Redis cache is now finished and one may return to the main directory:

```
cd ..
```

## Run the Java backend service application

The windows system used in the experiment could not communicate with the containers in docker, but could only be accessed through port forwarding. Therefore, this application is running in a container:

```
docker run -itd --name=dbms --hostname=dbms --network=hnet --ip=172.20.1.3 --add-host=nn1:172.20.1.0 --add-host=dn1:172.20.1.1 --add-host=dn2:172.20.1.2 --privileged zflcs/hadoop-cluster:1.0 /usr/sbin/init
```

After that, we connect the container with vscode, copy the dbms folder in the directory into the container. 

![image-20221222213715648](E:\ddbs-project\manual.assets\image-20221222213715648.png)

Although the mongDB is set up, the collections have not been initialized, so we need to execute `InitMongo.java` to initialize the collections. (To run the application properly, we need to install the relevant plugins in vscode, such as Springboot, Maven and so on.)

Once the collection is initialized, we can start executing `App.java` to launch the entire backend application, which can then be accessed via port 8080.

![image-20221222221050384](E:\ddbs-project\manual.assets\image-20221222221050384.png)

## Run the Vue frontend application

Before running the application, we need to install npm and run the applicationby executing the following command:

```
cd distributed_database_system_frontend
npm install
npm run dev
```

After that, we can interact with the backend via the vue application.

![image-20221222222910669](E:\ddbs-project\manual.assets\image-20221222222910669.png)

## Shut down

We can shut container down and delete it by clicking the button in the Docker Desktop. It is important to note that the mongoDB related container cannot directly click the button, and needs to execute the following commands:

```
docker kill --signal=SIGINT "container name"
```