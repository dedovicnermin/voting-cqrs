# edv-producer

Test tool for generating ElectionVote events.

## Usage  


### Set up environment 

```
pyenv .
source bin/activate
pip install -r requirements.txt
```


### Examples

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

### Clean up

run `deactivate`



## In progress
- configurable kafka producer connection configs (hardcoded for sandbox env usage)
- Extend to create election create events with good/bad content 