# Election Integrity

Kafka Streams application responsible for filtering-out invalid / illegal election creation events and vote events. Potentially enriching said events with image URL metadata. 

- Subscribes to topic containing ElectionCreate/ElectionVote events emitted from `client` component.
- Filter out illegal events
  - Elections containing inappropriate words
  - Duplicate vote entries
- Enrichment**
- Map to cloud event format 



