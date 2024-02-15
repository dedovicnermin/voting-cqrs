import json
import random
from faker import Faker
from uuid import uuid4

fake = Faker()

bad_words = ["fuck", "shit", "asshole", "bitch", "pussy", "cunt", "prick"]


def random_bad_word():
    return bad_words[random.randint(0, len(bad_words) - 1)]


class ElectionCreate:

    def __init__(self, author, title, desc, candidates):
        self.author = author
        self.title = title
        self.desc = desc
        self.category = 'python'
        self.candidates = candidates

    def value(self):
        if len(self.candidates) < 2:
            for _ in range(0, random.randint(2, 5)):
                self.candidates.append(fake.name())

        return json.dumps(self, default=vars)

    @staticmethod
    def key():
        return str(uuid4())
