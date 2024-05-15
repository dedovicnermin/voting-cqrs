FROM node:18-alpine

EXPOSE 3000

WORKDIR /app

COPY package.json /app

RUN npm install --verbose && \
    npm cache clean --force

COPY public/ /app/public
COPY src/ /app/src

CMD ["npm", "start"]