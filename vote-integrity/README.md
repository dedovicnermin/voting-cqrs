# Vote Integrity

Kafka Streams application responsible for filtering-out invalid / illegal votes for a given election

- Ensures only the first user vote for a given election is counted
- Legal events are sent to Kafka in cloudEvent format