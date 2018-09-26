const RestClient = require('node-rest-client').Client;
const config = require('./config');

module.exports = async function(inputPayload, inputJws){
    const toegangClient = new RestClient();
    const postBody = { payload : inputPayload, jws : inputJws};
    const args = {
        data: postBody,
        headers: { "Content-Type": "application/json" }
    };
    return new Promise((resolve, reject) => {
        toegangClient.post(config.sso_callback_uri, args, function (data, response) {
            if(response.statusCode && response.statusCode === 204)
                return resolve("OK");
            else
                return reject("Incorrect statusCode");
        });
    });
}