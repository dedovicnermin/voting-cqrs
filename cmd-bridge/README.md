# cmd-bridge (command bridge)

This component uses RSocket over websocket to accept FNF requests made by users interacting with UI (`client`):
  - Emit an event when user submits new election request
  - Emit an event when user clicks/views a respective election 
  - Emit an event when user registers a vote for a respective election

## Vote CMD 
```
rsc --debug --fnf \
  --route new-vote \
  --metadataMimeType=messaging/key --metadata "111:3c8eea22-7dda-4406-bb32-235d146d61c4" \
  --data '{"electionId": "3c8eea22-7dda-4406-bb32-235d146d61c4", "votedFor": "Brunson"}' \
  --stacktrace ws://cmd-bridge.test.nermdev.io/cmd
```


## Election Create CMD
```
rsc --debug --fnf \
  --route new-election \
  --metadataMimeType=messaging/key --metadata "111" \
  --data '{
    "author": "Leonardo",
    "title": "NBA Playoffs MVP",
    "description": "The best player in the NBA playoffs",
    "category": "Sports",
    "candidates": ["Luka Doncic", "Nikola Jokic", "Anthony Edwards", "Brunson"]
    }' \
  --stacktrace ws://cmd-bridge.test.nermdev.io/cmd
```

## Election view CMD
```
rsc --debug --fnf \
  --route new-view \
  --metadataMimeType=messaging/key --metadata "3c8eea22-7dda-4406-bb32-235d146d61c4" \
  --data "OPEN" \
  --stacktrace ws://cmd-bridge.test.nermdev.io/cmd
```


## Get elections
```
rsc --debug \
  --stream \
  --route e-getElections \
  --stacktrace ws://localhost:7000/cmd
```


## Insert Election
```
rsc --debug \
  --request \
  --data '{
    "author":"nermin",
    "title":"NBA Playoffs MVP (2024)",
    "description":"Best player performance in the playoffs",
    "category":"RANDOM",
    "candidates":{
      "Doncic":0,
      "Alexander":0,
      "Jokic":0,
      "Mitchell":0,
      "Edwards (ANT)":0
    },
    "startTs":1715307896,
    "endTs":1715308122,
    "status":"OPEN"}' \
    --route e-insert \
    --stacktrace ws://localhost:7000/cmd
    
2024-05-09 21:30:05.735 DEBUG 62829 --- [actor-tcp-nio-2] io.rsocket.FrameLogger                   : sending ->
Frame => Stream ID: 0 Type: SETUP Flags: 0b0 Length: 75
Data:

2024-05-09 21:30:05.736 DEBUG 62829 --- [actor-tcp-nio-2] io.rsocket.FrameLogger                   : sending ->
Frame => Stream ID: 1 Type: REQUEST_RESPONSE Flags: 0b100000000 Length: 312
Metadata:
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| fe 00 00 09 08 65 2d 69 6e 73 65 72 74          |.....e-insert   |
+--------+-------------------------------------------------+----------------+
Data:
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 7b 22 61 75 74 68 6f 72 22 3a 20 22 6e 65 72 6d |{"author": "nerm|
|00000010| 69 6e 22 2c 20 22 74 69 74 6c 65 22 3a 20 22 4e |in", "title": "N|
|00000020| 42 41 20 50 6c 61 79 6f 66 66 73 20 4d 56 50 20 |BA Playoffs MVP |
|00000030| 28 32 30 32 34 29 22 2c 20 22 64 65 73 63 72 69 |(2024)", "descri|
|00000040| 70 74 69 6f 6e 22 3a 20 22 42 65 73 74 20 70 6c |ption": "Best pl|
|00000050| 61 79 65 72 20 70 65 72 66 6f 72 6d 61 6e 63 65 |ayer performance|
|00000060| 20 69 6e 20 74 68 65 20 70 6c 61 79 6f 66 66 73 | in the playoffs|
|00000070| 22 2c 20 22 63 61 74 65 67 6f 72 79 22 3a 20 22 |", "category": "|
|00000080| 52 41 4e 44 4f 4d 22 2c 20 22 63 61 6e 64 69 64 |RANDOM", "candid|
|00000090| 61 74 65 73 22 3a 20 7b 22 44 6f 6e 63 69 63 22 |ates": {"Doncic"|
|000000a0| 3a 20 30 2c 20 22 41 6c 65 78 61 6e 64 65 72 22 |: 0, "Alexander"|
|000000b0| 3a 20 30 2c 20 22 4a 6f 6b 69 63 22 3a 20 30 2c |: 0, "Jokic": 0,|
|000000c0| 20 22 4d 69 74 63 68 65 6c 6c 22 3a 20 30 2c 20 | "Mitchell": 0, |
|000000d0| 22 45 64 77 61 72 64 73 20 28 41 4e 54 29 22 3a |"Edwards (ANT)":|
|000000e0| 20 30 7d 2c 20 22 73 74 61 72 74 54 73 22 3a 20 | 0}, "startTs": |
|000000f0| 31 37 31 35 33 30 37 38 39 36 2c 20 22 65 6e 64 |1715307896, "end|
|00000100| 54 73 22 3a 20 31 37 31 35 33 30 38 31 32 32 2c |Ts": 1715308122,|
|00000110| 20 22 73 74 61 74 75 73 22 3a 20 22 4f 50 45 4e | "status": "OPEN|
|00000120| 22 7d                                           |"}              |
+--------+-------------------------------------------------+----------------+
2024-05-09 21:30:06.465 DEBUG 62829 --- [actor-tcp-nio-2] io.rsocket.FrameLogger                   : receiving ->
Frame => Stream ID: 1 Type: NEXT_COMPLETE Flags: 0b1100000 Length: 304
Data:
         +-------------------------------------------------+
         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
+--------+-------------------------------------------------+----------------+
|00000000| 7b 22 69 64 22 3a 22 36 36 33 64 38 36 61 64 36 |{"id":"663d86ad6|
|00000010| 32 62 39 66 31 31 38 66 37 35 61 38 61 34 61 22 |2b9f118f75a8a4a"|
|00000020| 2c 22 61 75 74 68 6f 72 22 3a 22 6e 65 72 6d 69 |,"author":"nermi|
|00000030| 6e 22 2c 22 74 69 74 6c 65 22 3a 22 4e 42 41 20 |n","title":"NBA |
|00000040| 50 6c 61 79 6f 66 66 73 20 4d 56 50 20 28 32 30 |Playoffs MVP (20|
|00000050| 32 34 29 22 2c 22 64 65 73 63 72 69 70 74 69 6f |24)","descriptio|
|00000060| 6e 22 3a 22 42 65 73 74 20 70 6c 61 79 65 72 20 |n":"Best player |
|00000070| 70 65 72 66 6f 72 6d 61 6e 63 65 20 69 6e 20 74 |performance in t|
|00000080| 68 65 20 70 6c 61 79 6f 66 66 73 22 2c 22 63 61 |he playoffs","ca|
|00000090| 74 65 67 6f 72 79 22 3a 22 52 41 4e 44 4f 4d 22 |tegory":"RANDOM"|
|000000a0| 2c 22 63 61 6e 64 69 64 61 74 65 73 22 3a 7b 22 |,"candidates":{"|
|000000b0| 44 6f 6e 63 69 63 22 3a 30 2c 22 41 6c 65 78 61 |Doncic":0,"Alexa|
|000000c0| 6e 64 65 72 22 3a 30 2c 22 4a 6f 6b 69 63 22 3a |nder":0,"Jokic":|
|000000d0| 30 2c 22 4d 69 74 63 68 65 6c 6c 22 3a 30 2c 22 |0,"Mitchell":0,"|
|000000e0| 45 64 77 61 72 64 73 20 28 41 4e 54 29 22 3a 30 |Edwards (ANT)":0|
|000000f0| 7d 2c 22 73 74 61 72 74 54 73 22 3a 31 37 31 35 |},"startTs":1715|
|00000100| 33 30 37 38 39 36 2c 22 65 6e 64 54 73 22 3a 31 |307896,"endTs":1|
|00000110| 37 31 35 33 30 38 31 32 32 2c 22 73 74 61 74 75 |715308122,"statu|
|00000120| 73 22 3a 22 4f 50 45 4e 22 7d                   |s":"OPEN"}      |
+--------+-------------------------------------------------+----------------+
{"id":"663d86ad62b9f118f75a8a4a","author":"nermin","title":"NBA Playoffs MVP (2024)","description":"Best player performance in the playoffs","category":"RANDOM","candidates":{"Doncic":0,"Alexander":0,"Jokic":0,"Mitchell":0,"Edwards (ANT)":0},"startTs":1715307896,"endTs":1715308122,"status":"OPEN"}
```

