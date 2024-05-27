# Voting CQRS

An event-driven voting system adhering to the [CQRS design pattern](https://martinfowler.com/bliki/CQRS.html).

[![Build status](https://badge.buildkite.com/d750a8c7d7a696b2031ce9daa4f388be791369ab851270d920.svg)](https://buildkite.com/nerm/voting-cqrs)

## Components

1. [library](library/README.md) 
2. [client](client/README.md)
3. [cmd-bridge](cmd-bridge/README.md) 
4. [election-integrity](election-integrity/README.md)
5. [event-sink](event-sink/README.md)
6. [query-service](query-service/README.md)


## Tech Stack

### Java 17
- [JIB](https://github.com/GoogleContainerTools/jib) 
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Data](https://spring.io/projects/spring-data)
- [Cloud Events](https://cloudevents.io/)
- [Project Lombok](https://projectlombok.org/)
- [Apache Kafka](https://kafka.apache.org/)
- [RSocket](https://rsocket.io/)
- [Apache Avro](https://avro.apache.org/)
- [cloudevents-kafka-avro-serializer](https://github.com/kattlo/cloudevents-kafka-avro-serializer)

### NodeJS
- [React](https://react.dev/)


## Contributors

Hirui Huang
Sergei Kalachev
Nermin Dedovic
