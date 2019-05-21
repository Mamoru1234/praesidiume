const proxy = require('express-http-proxy');
const { signJson } = require('./SignUtility');
const cache = require('safe-memory-cache');

function addSignProxy(app, url, privateKey, redisClient) {
  app.use(createSignProxy(url, privateKey, redisClient));
  app.use(proxy(url));
}

function createSignProxy(url, privateKey, redisClient) {
  const signatures = new cache({
    limit: 100000,
    buckets: 1000,
  });
  return proxy(url, {
    filter: (req) => {
      return req.method === 'GET' && !req.url.includes('/artifact/download/');
    },
    userResDecorator: async function(proxyRes, proxyResData) {
      if (proxyRes.statusCode !== 200) {
        return proxyResData;
      }
      const text = proxyResData.toString();
      const payload = JSON.parse(text);
      const signature = await redisClient.getAsync(text);
      if (signature) {
        return ({
          signature,
          payload,
        });
      }
      return await signJson(payload, privateKey)
        .then(async (signature) => {
          console.timeEnd('signing');
          await redisClient.setAsync(text, signature);
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
