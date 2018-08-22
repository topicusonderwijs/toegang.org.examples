const validator = require('./validator')
const callback = require('./callback');

/**
 * This function checks the token and validates
 * if this is a signed token by toegang.org so you can login this specific user.
 */
module.exports = async function(req, res, next) {
    console.log(req.body.token);
    const token = req.body.token;
    console.log(`Token found : ${token}`);

    try {
        const payload = await validator.verify(token, /* Use your publisher name here to check if this token is meant for you*/"UitgeverX");
        if (!payload) {
            res.write(JSON.parse('{"error": "Token invalid!"}'));
            res.end();
            return;
        }
        /**
         * The payload will contain a 'rnd' property.
         * It is wise to validate if this value hasn't been used before by storing it in a cache/db.
         */
        console.log(payload);
        res.write(`<html><body>Validation SUCCESS : ${JSON.stringify(payload)} <br/><br/>EXP = expiry, SUB = subject (account), ingelogde gebruiker, EAN = europese artikelnummering, AUD = audience, your publisher name. ORG (optional) = organisation of the subject account, FN (optional) = first name of the user account, LAC: linked accounts, historic account identifiers (merges) </body></html>`);
        var callbackResult = await callback(payload, token);
        if(callbackResult === "OK"){
            res.write('Callback done');
        }
        else{
            res.write('Callback failed: ' + callbackResult);
        }
        res.end();
    } catch (e) {
        res.write(String(e));
        res.end();
        return;
    }
}
