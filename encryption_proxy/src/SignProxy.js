const proxy = require('express-http-proxy');
const { signJson } = require('./SignUtility');
const cache = require('safe-memory-cache');

function addSignProxy(app, url, privateKey) {
  app.use(createSignProxy(url, privateKey));
  app.use(proxy(url));
}

function createSignProxy(url, privateKey) {
  const signatures = new cache({
    limit: 100000,
    buckets: 1000,
  });
  return proxy(url, {
    filter: (req) => {
      return req.method === 'GET' && !req.url.includes('/artifact/download/');
    },
    userResDecorator: function(proxyRes, proxyResData) {
      if (proxyRes.statusCode !== 200) {
        return proxyResData;
      }
      const text = proxyResData.toString();
      const payload = JSON.parse(text);
      if (signatures.get(text) == null) {
        return signJson(payload, privateKey)
          .then((signature) => {
            console.timeEnd('signing');
            signatures.set(text, signature);
            return ({
              signature,
              payload,
            });
          });
      }
      return {
        signature: signatures.get(text),
        payload,
      };
    }
  });
}

module.exports = {
  addSignProxy,
};
