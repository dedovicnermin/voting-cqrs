# Library 

## Purpose 

To serve as a common library / re-usability

- Common models (POJOs)
- Utility/Helper classes
- Behavioral interfaces 
- Common serializers 
- Reusable abstract implementations of interfaces


### Kafka package 

Reusable classes scoped for kafka-client integration

#### clients 

- `EventReceiver`'s subscribe to events 
- `EventListener`'s process events consumed from a receiver
- `EventSender`'s produce events

- `serialization` : common implementation for consumers to safely handle events that are incorrectly formatted. If no issues, `PayloadOrError` contains payload and null error field. Otherwise, payload is null and error is nonnull. Encoded value can be used to get human-readable representation of data before deserialization
