package io.voting.command.cmdbridge.controller;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.kafka.CloudEventSerializer;
import io.voting.common.library.kafka.clients.serialization.avro.AvroCloudEventData;
import io.voting.common.library.kafka.clients.serialization.avro.KafkaAvroCloudEventSerializer;

import io.voting.events.enums.ElectionView;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AvroCloudEventDataTest {



//    @Test
//    void test() throws ExecutionException, InterruptedException {
//
//        final Map<String, Object> configs = Map.of(
//                "bootstrap.servers", "localhost:9092",
//                "schema.registry.url", "http://localhost:8081",
//                "auto.register.schemas", "false",
////                KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true",
//                CloudEventSerializer.ENCODING_CONFIG, "BINARY",
//                "key.serializer", "org.apache.kafka.common.serialization.StringSerializer",
//                "value.serializer", KafkaAvroCloudEventSerializer.class.getName()
//        );
////        final KafkaAvroCloudEventSerializer serializer = new KafkaAvroCloudEventSerializer();
////        serializer.configure(configs, false);
//        final Producer<String, CloudEvent> producer = new KafkaProducer<>(configs);
//
//        var valor = new CmdEventData(new ElectionVoteData("eId", "Foo"));
//        var data = new AvroCloudEventData<>(valor);
//        var evento = CloudEventBuilder
//                .v1()
//                .withId(UUID.randomUUID().toString())
//                .withSource(URI.create("/exemplo/enviar"))
//                .withType(valor.getCommand().getClass().getName())
//                .withSubject("TEST")
//                .withTime(OffsetDateTime.now())
//                .withData(AvroCloudEventData.MIME_TYPE, data)
//                .build();
//        System.out.println(evento);
//        final ProducerRecord<String, CloudEvent> producerRecord = new ProducerRecord<>("election.commands", evento);
//        producer.send(producerRecord).get();
//        Thread.sleep(1000);
//
//
//        final CmdEventData electionCreate = new CmdEventData(new ElectionCreateData("nerm", "election titile", "desc", ElectionCategory.Gaming, Arrays.asList("Foo", "Bar")));
//        final AvroCloudEventData<CmdEventData> eCreateData = new AvroCloudEventData<>(electionCreate);
//        final CloudEvent ceElectionCreate = CloudEventBuilder.v1()
//                .withId(UUID.randomUUID().toString())
//                .withSource(URI.create("/exemplo/enviar"))
//                .withType(electionCreate.getCommand().getClass().getName())
//                .withSubject("TEST")
//                .withTime(OffsetDateTime.now())
//                .withData(AvroCloudEventData.MIME_TYPE, eCreateData)
//                .build();
//        producer.send(new ProducerRecord<>("election.commands", ceElectionCreate)).get();
//        Thread.sleep(1000);
//
//        final CmdEventData cmdElectionView = new CmdEventData(new ElectionViewData("electionId", ElectionView.PENDING));
//        final AvroCloudEventData<CmdEventData> eViewData = new AvroCloudEventData<>(cmdElectionView);
//        final CloudEvent ceElectionView = CloudEventBuilder.v1()
//                .withId(UUID.randomUUID().toString())
//                .withSource(URI.create("/exemplo/enviar"))
//                .withType(cmdElectionView.getCommand().getClass().getName())
//                .withSubject("TEST")
//                .withTime(OffsetDateTime.now())
//                .withData(AvroCloudEventData.MIME_TYPE, eViewData)
//                .build();
//        producer.send(new ProducerRecord<>("election.commands", ceElectionView)).get();
//        Thread.sleep(1000);
//
//
//
//
//    }

}
