import argparse
from faker import Faker
from helpers.electionsender import ElectionSender
from helpers.helperutils import read_properties_file
from models.electioncreate import ElectionCreate, random_bad_word

fake = Faker()

parser = argparse.ArgumentParser(description="Produce events for election creation")
parser.add_argument("--author", "-a", default=fake.name(), help="author of the election")
parser.add_argument("--title", "-t", default=fake.company(), help="title of the election")
parser.add_argument("--desc", "-d", default=fake.text(), help="description of the election")
parser.add_argument("--candidates", "-c", default=[], nargs='+', help="candidates for the election")
parser.add_argument("--illegal", "-x", default=False, type=lambda x: x != 'False', help="should election contain illegal content")
parser.add_argument("--properties", "-p", default=None, help="Path to configuration properties")

args = parser.parse_args()

config = read_properties_file(args.properties)
config.pop('mongo_url')

# election_sender = ElectionSender(config, "test")
election_sender = ElectionSender(config)

election = ElectionCreate(args.author, args.title, args.desc, args.candidates)

if args.illegal:
    election.candidates.append(random_bad_word())

election_sender.send(election)

election_sender.close()
