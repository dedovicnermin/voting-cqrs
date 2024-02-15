from confluent_kafka import Producer
import socket


def acked(err, msg):
    if err is not None:
        print(f'Failed to deliver message: {str(msg)}: {str(err)}')


class ElectionSender:

    def __init__(self, topic='election.requests.raw'):
        self.topic = topic
        config = {
            'bootstrap.servers': 'localhost:9092',
            'client.id': socket.gethostname()
        }
        self.producer = Producer(config)

    def send(self, election_create):
        event_key = election_create.key()
        event_value = election_create.value()
        print(f'Sending event with key ({event_key}) and value ({event_value})')
        self.producer.produce(self.topic, key=event_key, value=event_value, callback=acked)

    def close(self):
        self.producer.flush()
        self.producer.poll(1)
