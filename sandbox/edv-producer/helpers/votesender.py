from confluent_kafka import Producer
import socket
import json


def acked(err, msg):
    if err is not None:
        print(f'Failed to deliver message: {str(msg)}: {str(err)}')


class VoteSender:

    def __init__(self, topic='election.votes.raw'):
        self.topic = topic
        config = {
            'bootstrap.servers': 'localhost:9092',
            'client.id': socket.gethostname()
        }
        self.producer = Producer(config)

    def send(self, vote_event):
        event_value = json.dumps(vote_event.value())
        event_key = vote_event.key()
        print(f'Sending event with key ({event_key}) and value ({event_value})')
        self.producer.produce(self.topic, key=event_key, value=event_value, callback=acked)

    def close(self):
        self.producer.flush()
        self.producer.poll(1)
