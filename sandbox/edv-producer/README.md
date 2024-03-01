# edv-producer

Test tool for generating ElectionVote / ElectionCreate events.

## Usage  


### Set up environment 

```
pyenv .
source bin/activate
pip install -r requirements.txt
```


### Examples

#### Send votes

```
python3 send-votes.py
python3 send-votes.py -n 10
python3 send-votes.py -n <> -u False
```

```
python3 send-votes.py -e <election_id>
python3 send-votes.py -e <election_id> -n 10
python3 send-votes.py -e <election_id> -n <> -u False
```

```
python3 send-votes.py -e <election_id> -c "<candidate>"
python3 send-votes.py -e <election_id> -c "<candidate>" -n 10
python3 send-votes.py -e <election_id> -c "<candidate>" -n <> -u False
```

#### Send elections

```
python3 send-elections.py
python3 send-elections.py -x True
python3 send-elections.py -a author -t title -d description =x True -c candidate1 candidate2 candidate3 candidateN
```

#### Query elections

`python3 query-elections.py`


### Clean up

run `deactivate`



## In progress
- configurable kafka producer connection configs (hardcoded for sandbox env usage)
- Extend to create election create events with good/bad content 