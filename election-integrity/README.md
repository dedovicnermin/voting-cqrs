# Election Integrity

Kafka Streams application responsible for filtering-out invalid / illegal election creation events. Potentially enriching said events with image URL metadata. 

- Subscribes to topic containing ElectionCreate events emitted from `client` component.
- Filter out illegal events
- Enrichment**
- Map to cloud event format 



