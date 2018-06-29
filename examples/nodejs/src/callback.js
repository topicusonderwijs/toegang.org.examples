const OAuth2 = require('client-oauth2');
const RestClient = require('node-rest-client').Client;

const ToegangOrgClient = new OAuth2({
    // replace with correct values.
    clientId: 'test',
    clientSecret: 'test',
    accessTokenUri: 'http://localhost:8080/token',
    authorizationUri: 'http://localhost:8080/auth',
    redirectUri: 'http://localhost:3000'
});

module.exports = async function(inputPayload, inputJws){
    const toegangToken = await ToegangOrgClient.credentials.getToken();
    const toegangClient = new RestClient();
    const postBody = { payload : inputPayload, jws : inputJws};
    const args = {
        data: postBody,
        headers: { "Content-Type": "application/json" }
    };
    return new Promise((resolve, reject) => {
        toegangClient.post("http://localhost:8081/callback", args, function (data, response) {
            if(response.statusCode && response.statusCode === 204)
                return resolve("OK");
            else
                return reject("Incorrect statusCode");
        });
    });
}