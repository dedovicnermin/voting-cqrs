# mock-server

Mocking req/resp interacitons with query-service


## Data

Located in `db.json`

## How to Run

```
cd voting-cqrs/client/mock-server
npm run db.start


curl http://localhost:8080/api/elections | jq
curl http://localhost:8080/api/elections/1 | jq
```
