const pgp = require('openpgp');
const fs = require('fs-extra');
const { userIds } = require('./../src/Constants');

const options = {
  userIds: userIds,
  numBits: 4096,
};

const main = async () => {
  console.log('Runninng man');
  const keyPair = await pgp.generateKey(options);
  await fs.writeFile('../keys/private.key', keyPair.privateKeyArmored);
  await fs.writeFile('../keys/public.key', keyPair.publicKeyArmored);
};

console.log('running file');

main().catch((e) => {
  console.error(e);
});
