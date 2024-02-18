```
docker compose up -d 
./scripts/create-topics.sh
cd scripts
./produce-elections.sh
```

Create topics in UI:
- `localhost:9021`


# Starting applications

- ensure you're using java17
- ensure you're in `voting-cqrs` root directory
- run `mvn clean install`

```
mvn clean install -DskipTests

java -jar election-integrity/target/election-integrity.jar election-integrity/src/main/resources/application-sandbox.properties

java -jar vote-integrity/target/vote-integrity.jar vote-integrity/src/main/resources/application-sandbox.properties

java -jar -Dspring.profiles.active=sandbox event-sink/target/event-sink.jar
```