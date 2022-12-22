docker rm -f nn
docker run -itd --name=nn --hostname=nn --network=hnet -P -p 9870:9870 -p 8088:8088 -p 9000:9000 --ip=172.20.1.0 --add-host=dn1:172.20.1.1 --add-host=dn2:172.20.1.2 --privileged zflcs/hadoop-cluster:1.0 /usr/sbin/init
docker rm -f dn1
docker run -d --name=dn1 --hostname=dn1 --network=hnet --ip=172.20.1.1 --add-host=nn:172.20.1.0 --add-host=dn2:172.20.1.2 --privileged zflcs/hadoop-cluster:1.0 /usr/sbin/init
docker rm -f dn2
docker run -d --name=dn2 --hostname=dn2 --network=hnet --ip=172.20.1.2 --add-host=nn:172.20.1.0 --add-host=dn1:172.20.1.1 --privileged zflcs/hadoop-cluster:1.0 /usr/sbin/init
