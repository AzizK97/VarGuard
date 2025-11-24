FROM node:22-alpine
WORKDIR /usr/src/app
COPY package.json package-lock.json* ./
RUN npm install --production --no-audit --no-fund
COPY . .
EXPOSE 4000
CMD ["node", "index.js"]
