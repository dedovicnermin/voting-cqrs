from pymongo import MongoClient
from bson.objectid import ObjectId
from bson.errors import InvalidId
import pprint

from models.electiondetails import ElectionDetails


class ElectionClient:
    host = "localhost"
    port = 27017
    user_name = "edv"
    password = "edv-secret"

    def __init__(self, mongo_url='mongodb://edv:edv-secret@localhost:27017/edv'):
        self.conn = MongoClient(mongo_url)
        db = self.conn.edv
        self.elections = db.election

    def query_all_elections(self):
        elections = []
        for doc in self.elections.find():
            candidates = []
            for k in doc['candidates'].keys():
                candidates.append(k)
            election_details = ElectionDetails(str(doc['_id']), candidates)
            elections.append(election_details)
        return elections

    def query_and_print_all_election_details(self):
        elections = []
        for doc in self.elections.find():
            print()
            pprint.pprint(doc)
            print()
            print(type(doc))
            print(doc['candidates'])
            candidates = []
            for k in doc['candidates'].keys():
                print(f"key: {k} / value: {doc['candidates'][k]}")
                candidates.append(k)
            election_details = ElectionDetails(doc['_id'], candidates)
            elections.append(election_details)
        for e in elections:
            print()
            e.print()
            print(e.select_random_candidate())
            print()
        return elections

    def query_election(self, election_id):
        try:
            # ObjectId(election_id)
            election = self.elections.find_one({'_id': election_id})
            candidates = []
            for k in election['candidates'].keys():
                candidates.append(k)

            return ElectionDetails(str(election['_id']), candidates)
        except InvalidId as e:
            print(f"Nothing found for given election_id ({election_id}) : {e}")
            return None

    def print_elections(self):
        for doc in self.elections.find():
            print()
            print()
            pprint.pprint(doc)
            print()
            print()

    def close(self):
        self.conn.close()
