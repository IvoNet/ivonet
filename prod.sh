#!/bin/sh

open http://192.168.99.100:8081/ivonet/api/epub
open http://192.168.99.100:8081/ivonet/api/bliki
# This command will enable "autodeployment" in the wildfly docker container on maven build
docker run -it --rm -p 8081:8080 -p 9991:9990 -v $(pwd)/artifact:/opt/jboss/wildfly/standalone/deployments/ -v /Users/ivonet/dev/ivonet-home/IvoNet.nl/microservices/ivonet/src/test/resources/books:/books -v /Users/ivonet/Dropbox/Apps/Editorial/ivonet.it:/bliki ivonet/wildfly-admin-keycloak-adapter
