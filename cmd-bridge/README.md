

## Vote CMD 
```
rsc --debug --fnf \
  --route new-vote \
  --metadataMimeType=messaging/key --metadata "777:000" \
  --data '{"electionId": "000", "votedFor": "nerm"}' \
  --stacktrace ws://localhost:7000/cmd
```


## Election CMD
```
rsc --debug --fnf \
  --route new-election \
  --metadataMimeType=messaging/key --metadata "777" \
  --data '{
    "author": "Leonardo",
    "title": "the best gatsby",
    "description": "xyz123",
    "category": "RANDOM",
    "candidates": ["Doug", "Smug", "Yug"]
    }' \
  --stacktrace ws://localhost:7000/cmd
```