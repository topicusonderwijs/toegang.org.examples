
const RestClient = require('node-rest-client').Client;

module.exports = async function(inputPayload, inputJws){
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