const {describe, it, before} = require("mocha");
const { expect } = require('chai');
const { parsePublicKey, verifyJson } = require('../src/SignUtility');
const { get } = require('axios');
const path = require('path');

describe('Simple case of using proxy', function () {
  let publicKey = null;
  const package = 'lodash';
  const version = '4.17.4';
  this.timeout(60000);
  before(async () => {
    publicKey = await parsePublicKey(path.join(__dirname, '../keys/public.key'));
  });
  it('package metadata should be signed', async () => {
    const response = await get(`http://localhost:3000/artifact/metadata/${package4}`);
    expect(response.status).is.eql(200);
    const { data } = response;
    const { payload, signature } = data;
    const isValid = await verifyJson(payload, signature, publicKey);
    expect(isValid).is.eql(true);
  });
  it('package version should be signed', async () => {
    const response = await get(`http://localhost:3000/artifact/metadata/${package}/${version}`);
    expect(response.status).is.eql(200);
    const { data } = response;
    const { payload, signature } = data;
    const isValid = await verifyJson(payload, signature, publicKey);
    expect(isValid).is.eql(true);
  });
  it('package download without sign', async () => {
    const response = await get(`http://localhost:3000/artifact/download/${package}/${version}`);
    expect(response.status).is.eql(200);
    const { data } = response;
    expect(data).to.be.a('string');
    expect(() => JSON.parse(data)).to.throw(SyntaxError);
  });
});
