#FROM node:21-alpine3.17
FROM node:14.21-alpine3.16

WORKDIR /app

COPY public/ /app/public
COPY src/ /app/src
COPY package.json /app


ENV REACT_APP_QUERY_SERVICE_URL="http://query-service:8080"
ENV REACT_APP_VOTE_ENDPOINT="http://broker:8090/kafka/v3/clusters/GJjTu-METQy8Ud-d0EQ9ZA/topics/elections.votes.raw/records"
ENV REACT_APP_ELECTION_ENDPOINT="http://broker:8090/kafka/v3/clusters/GJjTu-METQy8Ud-d0EQ9ZA/topics/elections.requests.raw/records"


RUN npm install --verbose

CMD ["npm", "start"] 


