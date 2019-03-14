const oauthservice = require('./authentication/oauthservice');
const RestClient = require('node-rest-client').Client;
const uuid = require('uuid/v4');
const config = require('./config');

/**
 * @param productEan EAN om licenties voor aan te maken
 * @param organisatieDigiDeliveryId digiDeliveryId van de organisatie waarvoor licenties worden aangemaakt
 * @param uitgeverId identifier van de uitgever, ter validatie
 * @param aantal aantal licenties aan te maken voor dit product
 * @returns JSON object consumed by license API.
 */
const _createLicenseProperties = function(productEan, uitgeverId, aantal){
    return {
        productId: productEan,
        requestReferenceId: uuid(),
        distributorId: uitgeverId,
        amount: aantal
    }
};

/**
 * In a real application this endpoint would require administrator permissions!
 * Do not use as-is, for demonstration purposes only.
 */
module.exports = async function(req, res) {
    const accessToken = await oauthservice.getClientCredentialsToken();
    if(!accessToken){
        res.status(500);
        res.write("Could not fetch accessToken, possibly due to incorrect client/secret. See logging.");
        res.end();
        return;
    }
    const  aantalLicenties = 12;
    const licenseObject = _createLicenseProperties('9789999999664', config.uitgever_naam, aantalLicenties) ;
    const toegangClient = new RestClient();
    const args = {
        headers: {
            "Content-Type": "application/json",
                "Authorization": "Bearer " + accessToken.token.access_token
        },
        parameters: licenseObject
    };
    await toegangClient.post(config.api_license_uri, args, function (data, response) {
        if(response.statusCode && response.statusCode == 200) {
            res.status(200);
            if(data['codes']){
                data['codes'].forEach(license => {
                    res.write(`Licentie aangemaakt met code: ${license}\n`);
                });
            } else {
                console.log('Licenties konden niet worden opgehaald, fout in communicatie met backend');
            }
        }
        else {
            res.status(500);
            console.log(response.statusMessage);
            res.write('Failed to create license, check console for error');
        }
        res.end();
    });
}


