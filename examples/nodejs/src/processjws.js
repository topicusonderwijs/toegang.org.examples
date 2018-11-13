const validator = require('./validator');
const callback = require('./callback');
const config = require('./config');

/**
 * This POST endpoint expects a JWS token, based on which it will, or will not, login the user.
 * This depends on the validity of the signature and the payload of the JWS.
 */
module.exports = async function(req, res, next) {
    console.log(req.body.token);
    const token = req.body.token;
    console.log(`Token found : ${token}`);

    try {
        /*
         * (Step 3b) verify signature, audience and expiry date of JWs.
         */
        const payload = await validator.verify(token, config.uitgever_uuid);
        if (!payload) {
            res.write(JSON.parse('{"error": "Token invalid!"}'));
            res.end();
            return;
        }
        /*
         * The payload will contain a 'rnd' property.
         * It is also wise to validate if this value hasn't been used before by storing it in a cache/db.
         */

        res.write('<head><meta charset="UTF-8"></head>');
        res.write('<html><body>');
        console.log(payload);
        res.write(`Validation SUCCESS : ${JSON.stringify(payload)} <br/><br/>EXP = expiry, SUB = subject (account), ingelogde gebruiker, EAN = europese artikelnummering, AUD = audience, your publisher name. ORG (optional) = organisation of the subject account, FN (optional) = first name of the user account, LAC: linked accounts, historic account identifiers (merges)`);

        /*
         * (Step 4) in your real application, at this point you can authorize the user to access the product mentioned in the EAN field.
         */

        /*
         * (Step 5) callback to TOEGANG.ORG to let them know the user has logged in successfully.
         */
        var callbackResult = await callback(payload, token);

        if(callbackResult === "OK"){
            res.write('<br/><br/>Callback done');
        }
        else{
            res.write('<br/><br/>Callback failed: ' + callbackResult);
        }
        res.write('</body></html>');
        res.end();
    } catch (e) {
        res.write(String(e));
        res.end();
        return;
    }
}
