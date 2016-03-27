# IvoNet services

JAX-RS based services for IvoNet.nl

# Testing

## Unit testing

```sh
mvn clean test
```

## Integration testing

Integration testing is done with Arquillian

```sh
mvn clean verify -P with-integration
```

## curl

curl -H "Content-Type: application/json" -X POST -L -d '{"name":"Stoker, Bram"}' http://192.168.99.100:8081/ivonet/api/epub