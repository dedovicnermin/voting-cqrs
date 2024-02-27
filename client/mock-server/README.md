# mock-server

Mocking req/resp interacitons with query-service


## Data

Located in `db.json`

## How to Run

```
cd voting-cqrs/client/mock-server
npm run db.start
```

### Elections

```
curl http://localhost:8080/api/elections | jq
curl http://localhost:8080/api/elections/1 | jq
```

### Users

> NOTE: Mock-server does not 100% match behavior expected from `query-service`. 

#### Register

- `query-service` will return GenericResponse - response body contains message (success/error)

```
curl -X POST -s localhost:8080/api/auth/register -H "Content-Type: application/json" -d '{
    "email": "test@email.com",
    "password": "test-secret",
    "id": "1000",
    "username": "test"
}'

RESPONSE: >
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6InRlc3RAZW1haWwuY29tIiwiaWF0IjoxNzA5MDY4OTc0LCJleHAiOjE3MDkwNzI1NzQsInN1YiI6IjEwMDAifQ.4oIjw8SFllP2f0nFi8kexUHP-S_e2tH7WI-1YWeBLTQ",
  "user": {
    "email": "test@email.com",
    "id": "1000",
    "username": "test"
  }
}
```

#### Login

- `query-service` will ignore request body and expect basicAuth header to be set instead (`Authorization: Basic dGVzdDp0ZXN0LXNlY3JldAo=`)
  - `echo "test:test-secret" | base64`
  - The above command returns `dGVzdDp0ZXN0LXNlY3JldAo=`
- `query-service` will return response body containing `id`, `username`, and `token` on valid credentials
  - example: `{"id": "001", "username": "Bob", "token": "xyz"}`

```
curl -X POST -s localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{
    "email": "test@email.com",
    "password": "test-secret"
}'   

RESPONSE: >
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6InRlc3RAZW1haWwuY29tIiwiaWF0IjoxNzA5MDY5NDU4LCJleHAiOjE3MDkwNzMwNTgsInN1YiI6IjEwMDAifQ.mXgum6mDEV3a_WszzLtabSVzpZvIoLbiwfKPL8h3ICw",
  "user": {
    "email": "test@email.com",
    "id": "1000",
    "username": "test"
  }
}
```
