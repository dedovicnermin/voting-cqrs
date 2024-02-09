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
  "author": "user_id",
  "title": "election_name",
  "description": "additional context user can provide if necessary",
  "category": "election_category",
  "candidates": ["Bob", "Foo", "Bar", "XYZ"]
}
```