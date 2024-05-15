# Client (React)

Front-end component of the EDV system. Tasked with emitting command events to Kafka and displaying state served by `query-service`.

## Behavior

### Command scenarios

- user requests to vote for an election
- user requests to create an election

In the above scenarios, component will package the data into event format and produce to respective Kafka topics.


### Query scenarios

- user requests view on all elections
- user requests view on specific election

In the above scenarios, component issues `GET` requests to `query-service` in-order to retrieve/display system state 


### Login/Register

#### Login

Interact with `query-service` component for validation

#### Register

Interact with `query-service` component for creating an account 



## Events

### ElectionView
- each time user clicks a particular election, an event is emitted containing
  - election ID
  - Election view (sends PENDING on OPEN elections if `.now()` > election.endTs)

### ElectionVote

```
key : <userID>:<electionId>
value : {
  "electionId": "000",
  "votedFor": "Bob"
}
```


### ElectionCreate

```
key : <userId>
value : {
  "author": "username",
  "title": "election_name",
  "description": "additional context user can provide if necessary",
  "category": "election_category",
  "candidates": ["Bob", "Foo", "Bar", "XYZ"]
}
```

```
docker run -d --name edv-client \
  -p 3000:3000 \
  -e REACT_APP_QUERY_SERVICE_URL=http://query-service.test.nermdev.io \
  -e REACT_APP_CMD_ENDPOINT=ws://cmd-bridge.test.nermdev.io/cmd \
  registry.nermdev.io/apps/edv-client:$VERSION
```