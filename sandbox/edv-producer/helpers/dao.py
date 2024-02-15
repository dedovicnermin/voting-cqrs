from models.voteevent import VoteEvent
import uuid


class DaoHelper:

    def __init__(self, election_client, vote_sender):
        self.election_client = election_client
        self.vote_sender = vote_sender

    def random_vote_all_elections(self, count, unique):
        election_details = self.election_client.query_all_elections()
        for elem in election_details:
            for i in range(0, int(count)):
                vote = VoteEvent(elem.election_id, str(uuid.uuid4()) if unique else 'python', elem.select_random_candidate())
                self.vote_sender.send(vote)

    def random_vote_for_election(self, election_id, count, unique):
        election = self.election_client.query_election(election_id)
        if election:
            for i in range(0, int(count)):
                vote = VoteEvent(election.election_id, str(uuid.uuid4()) if unique else 'python', election.select_random_candidate())
                self.vote_sender.send(vote)

    def vote_for_candidate_running_in_election(self, election_id, candidate, count, unique):
        election = self.election_client.query_election(election_id)
        if election:
            candidate_check = list(filter(lambda c: c == candidate, election.candidates))
            if len(candidate_check) == 0:
                print(f"Candidate provided ({candidate}) is not a valid candidate for election with candidates ({election.candidates})")
                return
            for i in range(0, int(count)):
                vote = VoteEvent(election.election_id, str(uuid.uuid4()) if unique else 'python', candidate)
                self.vote_sender.send(vote)






