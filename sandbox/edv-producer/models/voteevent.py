
class VoteEvent:

    def __init__(self, election_id, user_id, candidate):
        self.election_id = election_id
        self.candidate = candidate
        self.user_id = user_id

    def key(self):
        return f'{self.user_id}:{self.election_id}'

    def value(self):
        return {'electionId': self.election_id, 'votedFor': self.candidate}
