const proxy = require('express-http-proxy');
const { signJson } = require('./SignUtility');

function addSignProxy(app, url, privateKey) {
  app.use(createSignProxy(url, privateKey));
  app.use(proxy(url));
}

function createSignProxy(url, privateKey) {
  const signatures = {};
  return proxy(url, {
    filter: (req) => {
      return req.method === 'GET' && !req.url.includes('/artifact/download/');
    },
    userResDecorator: function(proxyRes, proxyResData) {
      if (proxyRes.statusCode !== 200) {
        return proxyResData;
      }
      const payload = JSON.parse(proxyResData.toString());
      if (signatures[proxyResData] != null) {
        return {
          signature: signatures[proxyResData],
          payload,
        }
      }
      return signJson(payload, privateKey)
        .then((signature) => {
          console.timeEnd('signing');
          signatures[proxyResData] = signature;
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
