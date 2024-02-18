import socket
import sys


def read_properties_file(path):
    if not path:
        return {
            'mongo_url': 'mongodb://edv:edv-secret@localhost:27017/edv',
            'bootstrap.servers': 'localhost:9092',
            'client.id': socket.gethostname()
        }
    if ".properties" not in path:
        print("Error: configuration file should be in '.properties' format")
        sys.exit(1)

    properties = {}
    with open(path, 'r') as file:
        for line in file:
            line = line.strip()
            if line and not line.startswith('#'):
                key, value = line.split('=', 1)
                properties[key.strip()] = value.strip()
    properties['client.id'] = socket.gethostname()
    return properties
