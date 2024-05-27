# Election Integrity

Kafka Streams application responsible for filtering-out invalid / illegal election creation events and vote events. Potentially enriching said events with image URL metadata. 

- Subscribes to topic containing ElectionCreate/ElectionVote/ElectionView events emitted from `client` component.
- Filter out illegal events
  - Elections containing inappropriate words
  - Duplicate vote entries
- Responsible for signaling end of life for an election (ttl expiry)
- Map to cloud event format with specific CloudEventType metadata  



```
Topologies:
   Sub-topology: 0
    Source: election.cmd.src (topics: [election.commands])
      --> global.ce.upcast
    Processor: global.ce.upcast (stores: [])
      --> ttl.cmd.filter, vi.cmd.filter, ei.cmd.filter
      <-- election.cmd.src
    Processor: vi.cmd.filter (stores: [])
      --> vi.unwrap.ce.data
      <-- global.ce.upcast
    Processor: vi.unwrap.ce.data (stores: [])
      --> vi.dao.ce.data
      <-- vi.cmd.filter
    Processor: ei.cmd.filter (stores: [])
      --> ei.unwrap.ce.data
      <-- global.ce.upcast
    Processor: vi.dao.ce.data (stores: [])
      --> vi.integrity.aggregator
      <-- vi.unwrap.ce.data
    Processor: ei.unwrap.ce.data (stores: [])
      --> ei.integrity.filter
      <-- ei.cmd.filter
    Processor: vi.integrity.aggregator (stores: [election.vote.aggregate])
      --> vi.integrity.aggregate.stream
      <-- vi.dao.ce.data
    Processor: ei.integrity.filter (stores: [])
      --> ei.legal.election.mapper
      <-- ei.unwrap.ce.data
    Processor: ttl.cmd.filter (stores: [])
      --> ttl.unwrap.ce.data
      <-- global.ce.upcast
    Processor: vi.integrity.aggregate.stream (stores: [])
      --> vi.vote.filter
      <-- vi.integrity.aggregator
    Processor: ei.legal.election.mapper (stores: [])
      --> ei.ce.mapper
      <-- ei.integrity.filter
    Processor: ttl.unwrap.ce.data (stores: [])
      --> ttl.pending.view.filter
      <-- ttl.cmd.filter
    Processor: vi.vote.filter (stores: [])
      --> vi.eid.extractor
      <-- vi.integrity.aggregate.stream
    Processor: ei.ce.mapper (stores: [])
      --> ei.key.selector
      <-- ei.legal.election.mapper
    Processor: ttl.pending.view.filter (stores: [])
      --> ttl.ce.mapper
      <-- ttl.unwrap.ce.data
    Processor: vi.eid.extractor (stores: [])
      --> vi.ce.mapper
      <-- vi.vote.filter
    Processor: ei.key.selector (stores: [])
      --> ei.object.upcast
      <-- ei.ce.mapper
    Processor: ttl.ce.mapper (stores: [])
      --> ttl.object.upcast
      <-- ttl.pending.view.filter
    Processor: vi.ce.mapper (stores: [])
      --> vi.object.upcast
      <-- vi.eid.extractor
    Processor: ei.object.upcast (stores: [])
      --> ei.sink
      <-- ei.key.selector
    Processor: ttl.object.upcast (stores: [])
      --> ttl.sink
      <-- ttl.ce.mapper
    Processor: vi.object.upcast (stores: [])
      --> vi.sink
      <-- vi.ce.mapper
    Sink: ei.sink (topic: election.events)
      <-- ei.object.upcast
    Sink: ttl.sink (topic: election.events)
      <-- ttl.object.upcast
    Sink: vi.sink (topic: election.events)
      <-- vi.object.upcast
```