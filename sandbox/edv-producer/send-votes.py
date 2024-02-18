import argparse

from helpers.dao import DaoHelper
from helpers.electionclient import ElectionClient
from helpers.helperutils import read_properties_file
from helpers.votesender import VoteSender

parser = argparse.ArgumentParser(description="Produce election vote events")
parser.add_argument("--election", "-e", help="Election ID", default=None)
parser.add_argument("--candidate", "-c", help="Election candidate to vote for", default=None)
parser.add_argument("--number", "-n", help="Number of times to produce event", default=1)
parser.add_argument("--unique", "-u", help="Should vote events contain unique user Ids", default=True,
                    type=lambda x: x != 'False')
parser.add_argument("--properties", "-p", default=None, help="Path to configuration properties")
args = parser.parse_args()

config = read_properties_file(args.properties)

election_client = ElectionClient(config['mongo_url'])
config.pop('mongo_url')

# vote_sender = VoteSender(config, "test")
vote_sender = VoteSender(config)
dao = DaoHelper(election_client, vote_sender)

# election_client.query_and_print_all_election_details()


if not args.election:
    print(f'Sending {args.number} random votes for each respective election available')
    dao.random_vote_all_elections(args.number, args.unique)
elif args.candidate is None:
    print(f'Sending {args.number} random votes for election with id ({args.election})')
    dao.random_vote_for_election(args.election, args.number, args.unique)
else:
    print(f'Sending {args.number} votes for candidate ({args.candidate}) running in election with id ({args.election})')
    dao.vote_for_candidate_running_in_election(args.election, args.candidate, args.number, args.unique)

election_client.close()
vote_sender.close()
