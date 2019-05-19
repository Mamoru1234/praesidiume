const proxy = require('express-http-proxy');
const { signJson } = require('./SignUtility');

function addSignProxy(app, url, privateKey) {
  app.use(createSignProxy(url, privateKey));
  app.use(proxy(url));
}

function createSignProxy(url, privateKey) {
  return proxy(url, {
    filter: (req) => {
      return req.method === 'GET' && !req.url.includes('/artifact/download/');
    },
    userResDecorator: function(proxyRes, proxyResData) {
      if (proxyRes.statusCode !== 200) {
        return proxyResData;
      }
      const payload = JSON.parse(proxyResData.toString());
      return signJson(payload, privateKey)
        .then((signature) => {
          console.timeEnd('signing');
          return ({
            signature,
            payload,
          });
        });
    }
  });
}

module.exports = {
  addSignProxy,
};
