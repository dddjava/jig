const test = require('node:test');
const assert = require('node:assert/strict');

require('../../main/resources/templates/assets/jig-dom.js');

test.describe('typeLinkResolver', () => {
    test('デフォルトではnullに設定されている', () => {
        assert.equal(Jig.dom.type.getResolver(), null);
    });
});