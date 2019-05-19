const {
  parsePrivateKey,
} = require('./SignUtility');
const { addSignProxy } = require('./SignProxy');
const express = require('express');
const morgan = require('morgan');

const main = async () => {
  const url = 'http://localhost:8080';
  const privateKey = await parsePrivateKey('../keys/private.key');
  const app = express();
  app.use(morgan(':method :url :response-time ms'));
  addSignProxy(app, url, privateKey);
  app.listen(3000, (err) => {
    if (err) {
      console.error(err);
    }
    console.log('Listening: ', 3000);
  })
};

main().catch((e) => {
  console.error(e);
});
