const bluebird = require('bluebird');
const redis = require('redis');
bluebird.promisifyAll(redis);

function getClient(url) {
  return new Promise((res, rej) => {
    const redisClient = redis.createClient(url);
    redisClient.on('ready', () => {
      res(redisClient);
    });
    redisClient.on('error', (err) => {
      rej(err);
    })
  });
}

module.exports = {
  getClient,
};
