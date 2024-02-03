# Vote Integrity

Kafka Streams application responsible for filtering-out invalid / illegal votes for a given election




```
vintegrity-0 vintegrity 2024-02-01 07:51:39 INFO  [main] VoteIntegrityApplication :  ::: App topology :::%nTopologies:
    Sub-topology: 0
     Source: KSTREAM-SOURCE-0000000000 (topics: [election.votes.raw])
       --> KSTREAM-AGGREGATE-0000000001
     Processor: KSTREAM-AGGREGATE-0000000001 (stores: [votes.integrity.aggregate])
       --> KTABLE-TOSTREAM-0000000002
       <-- KSTREAM-SOURCE-0000000000
     Processor: KTABLE-TOSTREAM-0000000002 (stores: [])
       --> KSTREAM-FILTER-0000000003
       <-- KSTREAM-AGGREGATE-0000000001
     Processor: KSTREAM-FILTER-0000000003 (stores: [])
       --> KSTREAM-KEY-SELECT-0000000004
       <-- KTABLE-TOSTREAM-0000000002
     Processor: KSTREAM-KEY-SELECT-0000000004 (stores: [])
       --> KSTREAM-MAPVALUES-0000000005
       <-- KSTREAM-FILTER-0000000003
     Processor: KSTREAM-MAPVALUES-0000000005 (stores: [])
       --> KSTREAM-MAPVALUES-0000000006
       <-- KSTREAM-KEY-SELECT-0000000004
     Processor: KSTREAM-MAPVALUES-0000000006 (stores: [])
       --> KSTREAM-SINK-0000000007
       <-- KSTREAM-MAPVALUES-0000000005
     Sink: KSTREAM-SINK-0000000007 (topic: election.votes)
       <-- KSTREAM-MAPVALUES-0000000006
```