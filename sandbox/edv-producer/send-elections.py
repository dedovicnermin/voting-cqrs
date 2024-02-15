import argparse
from faker import Faker
from helpers.electionsender import ElectionSender
from models.electioncreate import ElectionCreate, random_bad_word

fake = Faker()

parser = argparse.ArgumentParser(description="Produce events for election creation")
parser.add_argument("--author", "-a", default=fake.name(), help="author of the election")
parser.add_argument("--title", "-t", default=fake.company(), help="title of the election")
parser.add_argument("--desc", "-d", default=fake.text(), help="description of the election")
parser.add_argument("--candidates", "-c", default=[], nargs='+', help="candidates for the election")
parser.add_argument("--illegal", "-x", default=False, type=lambda x: x != 'False', help="should election contain illegal content")
args = parser.parse_args()

election_sender = ElectionSender()

election = ElectionCreate(args.author, args.title, args.desc, args.candidates)

if args.illegal:
    election.candidates.append(random_bad_word())

election_sender.send(election)

election_sender.close()
