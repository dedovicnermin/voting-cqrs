# Query Service

Responsible for retrieving state and responding to queries originating from `client` component. Also tasked with persisting new users and performing user credential validation

- GET all elections
- GET specific election
- POST user login credential verification
- POST register new user


Dependant on `spring-web` / `spring-security` / `spring-data`


## Endpoints

- Register/Persist new user : `/api/auth/register`
  - POST request
  - Request body == `RegisterUserRequest.java`
  - If username already taken, returns error response
  - User password is hashed before persisting user
- Login (basic authN) : `/api/auth/login`
  - POST request
  - Request body == null/empty
  - User credentials are stored in header : `Authorization: Basic base64(username:password)`
  - User password is hashed before validating credentials with persisted user record
  - Returns 401 when credentials are invalid
  - Returns `JwtResponse.java` when credentials are valid 

- Elections (jwt authN)
  - All requests scoped to elections required JWT token to be present in request or else returns 401
    - `Authorization: Bearer <token_value_here>`
  - GET requests only
    - All elections : `/api/elections`
    - Specific election : `/api/elections/<election_id>`
