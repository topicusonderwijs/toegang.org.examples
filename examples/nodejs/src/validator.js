const {JWS, JWK} = require('node-jose');

/**
    PayLoad = {
        // organisation
    org?: string,
    // firstname
    fn?: string,
    // account identifier (specific for organisation)
    sub: string,
    // expiration date
    exp: number,
    //tlink code
    tlink: string,
    // ean
    ean: string,
    // audience
    aud: string,
    // referentie voor de gebruiker: meldcode
    ref: string
    }
*/

/**
 * This key should not be loaded from the url runtime to keep performance optimal.
 *
 * https://api.toegang.org/sso/jwks public key for PRODUCTION env
 * https://api-test.toegang.org/sso/jwks public key for TEST env
 */
async function getKey(){
    let keyStore = await JWK.asKeyStore({
    "keys": [
        {
            "kty": "RSA",
            "kid": "zJc5FbAGzS6R_A9cuYN70t-Y8LYtcEsnbFA9Ur09N6s",
            "use": "sig",
            "alg": "RS256",
            "e": "AQAB",
            "n": "gyHALYGsphnM-bOxXG7pIPi55m3WobL25lWklXV2ZuYqZg2WIBEMV_F868TCne4xMyN9SjP4zT_Zj-zKbaF2RJAclJQStL8iXkh4Xbwt2DNYsMsHZpclLBJ5b6KNCIf8ywQyhPLiplKjGmfZlsOICTe9aboFVrvEksUs_glU1oD1_wL8xI7HkWbhihBDVT6vNvq4Tq1tJuthE3X8KZN6FGk-1MZ692EOwkANcFa_A_OIYWe6GaLD79YSlwaQyVs9hXf1KMKxDP7-nxQn-tK-S-UygUu7kEwfnJ0a_-pl_PwUDvD0hzwRk77pWU0ChS3cO-haeHbM-CfOeBKiuaZl_Q"
        }
    ]
    });
    return keyStore.all({ kty: 'RSA' })[0];
}

module.exports.verify = async function(jws, authClientId){
    let key = await getKey();
    let buffer = await JWS.createVerify(key, {algorithms: ['RS256']}).verify(jws);

    let currentTime = Date.now();
    let payload = JSON.parse(buffer.payload);

    if (payload.aud !== authClientId) {
        throw new Error('JWT audience invalid');
    }
    if (payload.exp && payload.exp < currentTime) {
        throw new Error('JWT expired');
    }

    return payload;
}
