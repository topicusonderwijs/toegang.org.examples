const oauth2lib = require('simple-oauth2');
const config = require('../config');

const configuration = {
    client: {
        id: config.oauth_client_id,
        secret: config.oauth_client_secret
    },
    auth: {
        tokenHost: config.idp_base_uri,
        tokenPath: '/token'
    }
};
const oauth2 = oauth2lib.create(configuration);

let accessToken;

module.exports['getClientCredentialsToken'] = async function() {
    try {
        if(accessToken && !accessToken.expired()){
            console.log('using cached token');
            return accessToken;
        }
        const result = await oauth2.clientCredentials.getToken({scope: 'eck'});
        accessToken = oauth2.accessToken.create(result);
        return accessToken;
    } catch (error) {
        console.log(`Access Token error : ${error}`);
    }
    return null;
}
