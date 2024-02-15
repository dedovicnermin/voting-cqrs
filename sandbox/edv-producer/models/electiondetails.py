import random


class ElectionDetails:

    def __init__(self, election_id, candidates):
        self.election_id = election_id
        self.candidates = candidates
        self.candidate_count = len(candidates)

    def print(self):
        print(f'eid: {self.election_id}, candidates: {self.candidates}, candidate_count: {self.candidate_count}')

    def select_random_candidate(self):
        return self.candidates[random.randint(0, self.candidate_count - 1)]



