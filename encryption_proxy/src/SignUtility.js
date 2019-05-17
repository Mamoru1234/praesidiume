const pgp = require('openpgp');
const fs = require('fs-extra');
const crypto = require('crypto');

function signJson(data, privateKey) {
  const message = JSON.stringify(data);
  const hash = crypto.createHash('sha512');
  hash.update(message);
  return signMessage('sha512-' + hash.digest('base64'), privateKey);
}

async function signMessage(message, privateKey) {
  const signOptions = {
    message: pgp.cleartext.fromText(message),
    privateKeys: [privateKey],
  };
  return (await pgp.sign(signOptions)).data
}

async function verifyJson(data, signature, publicKey) {
  const message = await verifyMessage(signature, publicKey);
  const [algorithm, hashValue] = message.split('-');
  const hash = crypto.createHash(algorithm);
  hash.update(JSON.stringify(data));
  return hash.digest('base64') === hashValue;
}

async function verifyMessage(message, publicKey) {
  const verifyOptions = {
    message: await pgp.cleartext.readArmored(message), // parse armored message
    publicKeys: [publicKey]
  };
  const verify = await pgp.verify(verifyOptions);
  verify.signatures.forEach((sign) => {
    if (!sign.valid) {
      throw new Error('Invalid signature');
    }
  });
  return verify.data;
}

async function parsePrivateKey(keyPath, secret) {
  const privateKey = await fs.readFile(keyPath);
  const armoredKeys = (await pgp.key.readArmored(privateKey.toString())).keys;
  if (armoredKeys.length !== 1) {
    throw new Error(`Wrong keys number ${armoredKeys.length}`);
  }
  const parsedKey = armoredKeys[0];
  if (secret) {
    parsedKey.decrypt(secret);
  }
  return parsedKey;
}

async function parsePublicKey(keyPath) {
  const publicKey = await fs.readFile(keyPath);
  const keys = (await pgp.key.readArmored(publicKey)).keys;
  if (keys.length !== 1) {
    throw new Error(`Wrong keys number ${keys.length}`);
  }
  return keys[0];
}

module.exports = {
  parsePrivateKey,
  signMessage,
  signJson,
  parsePublicKey,
  verifyMessage,
  verifyJson,
};
