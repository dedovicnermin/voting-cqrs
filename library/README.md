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


Configure
- Serializer
  ```properties
  cloudevents.serializer.encoding=BINARY
  schema.registry.url=http://configure.me:8081
  auto.register.schemas=true

  value.serializer=KafkaAvroCloudEventSerializer
  ```
- Deserializer
  ```properties
  specific.avro.reader=false #to use GenericRecord data
  #specific.avro.reader=true #to use strong typed data
  schema.registry.url=http://configure.me:8081

  value.deserializer=KafkaAvroCloudEventDeserializer
  ```

3. Use
- Serialization
  ```java
  import java.net.URI;
  import java.time.OffsetDateTime;
  import java.util.UUID;
  import io.cloudevents.core.builder.CloudEventBuilder;
  import org.apache.kafka.clients.producer.ProducerRecord;

  // . . .

  var event = CloudEventBuilder
      .v1()
      .withId(UUID.randomUUID().toString())
      .withSource(URI.create("/example"))
      .withType("type.example")
      .withTime(OffsetDateTime.now())
      .withData(AvroCloudEventData.MIME_TYPE, data)
      .build();

  var record = new ProducerRecord<>("my-topic", event);

  // --- create KafkaProducer with Serializer configurations --- //

  // producer.send(record);
  ```
- Deserialization
  ```java
  import io.cloudevents.CloudEvent;
  import org.apache.avro.generic.GenericRecord;

  // --- create KafkaConsumer with Deserializer configurations --- //

  // consumer.subscribe(...)

  //var records = consumer.pool()

  records.forEach(record -> {

    // Get the CloudEvent instance
    CloudEvent event = record.value();

    // when specific.avro.reader=false
    GenericRecord data = AvroCloudEventData.dataOf(event);

    // when specific.avro.reader=true
    YourType data = AvroCloudEventData.dataOf(event);

  });
  ```
