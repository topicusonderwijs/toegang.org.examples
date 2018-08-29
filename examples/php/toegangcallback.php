<?php

use \Firebase\JWT\JWT;

require_once 'vendor/autoload.php';

/**
 * This key should not be loaded from the url runtime to keep performance optimal.
 *
 * https://api.toegang.org/sso/jwks public key for PRODUCTION env
 * https://api-test.toegang.org/sso/jwks public key for TEST env
 *
 * The public part of the JWK is this:
 *  {
 *   "kty": "RSA",
 *   "kid": "zJc5FbAGzS6R_A9cuYN70t-Y8LYtcEsnbFA9Ur09N6s",
 *   "use": "sig",
 *   "alg": "RS256",
 *   "e": "AQAB",
 *   "n": "gyHALYGsphnM-bOxXG7pIPi55m3WobL25lWklXV2ZuYqZg2WIBEMV_F868TCne4xMyN9SjP4zT_Zj-zKbaF2RJAclJQStL8iXkh4Xbwt2DNYsMsHZpclLBJ5b6KNCIf8ywQyhPLiplKjGmfZlsOICTe9aboFVrvEksUs_glU1oD1_wL8xI7HkWbhihBDVT6vNvq4Tq1tJuthE3X8KZN6FGk-1MZ692EOwkANcFa_A_OIYWe6GaLD79YSlwaQyVs9hXf1KMKxDP7-nxQn-tK-S-UygUu7kEwfnJ0a_-pl_PwUDvD0hzwRk77pWU0ChS3cO-haeHbM-CfOeBKiuaZl_Q"
 *  }
 *
 * For this example we have converted the JWK to a PEM public key:
 */
$publicKey = <<<EOD
-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgyHALYGsphnM+bOxXG7p
IPi55m3WobL25lWklXV2ZuYqZg2WIBEMV/F868TCne4xMyN9SjP4zT/Zj+zKbaF2
RJAclJQStL8iXkh4Xbwt2DNYsMsHZpclLBJ5b6KNCIf8ywQyhPLiplKjGmfZlsOI
CTe9aboFVrvEksUs/glU1oD1/wL8xI7HkWbhihBDVT6vNvq4Tq1tJuthE3X8KZN6
FGk+1MZ692EOwkANcFa/A/OIYWe6GaLD79YSlwaQyVs9hXf1KMKxDP7+nxQn+tK+
S+UygUu7kEwfnJ0a/+pl/PwUDvD0hzwRk77pWU0ChS3cO+haeHbM+CfOeBKiuaZl
/QIDAQAB
-----END PUBLIC KEY-----
EOD;

/**
 * Replace this with your publisher name:
 */
$uitgeverNaam = 'UitgeverX';

/**
 * Replace this with the actual endpoint of the toegang.org Callback API.
 * For more information, contact our support team.
 */
$callbackEndpoint = 'http://localhost:8081/callback/'

/**
 * Get the posted JWT
 */
$jws = $_POST['jws'];

function doCallback($payload, $endpoint)
{
    $headers = array('Content-Type: application/json', 'Content-Length: ' . strlen($payload));

    $options = [
        CURLOPT_URL => $endpoint,
        CURLOPT_HTTPHEADER => $headers,
        CURLOPT_POST => true,
        CURLOPT_POSTFIELDS => $payload,
    ];

    $curl = curl_init();
    curl_setopt_array($curl, $options);

    $result = curl_exec($curl);
    $httpcode = curl_getinfo($curl, CURLINFO_HTTP_CODE);
    echo "<br/>Callback return status: " . $httpcode;

    curl_close($curl);
}

if (empty($jws)) {
    echo 'JWS should not be empty!';
} else {
    /*
     * The supported algorithm is RS256. JWT:decode does the validation on the number of segments, algorithm, key,
     * signature, etcetera. We have to check the relevant claims in the payload, like 'exp' and 'aud'.
     */
    $decoded = JWT::decode($jws, $publicKey, array('RS256'));
    $decoded_array = (array)$decoded;
    echo '<h2>Decoded JWS</h2>' . print_r($decoded_array, true) . '<br/><br/>';

    $currentTime = time() * 1000;
    $exp = $decoded_array['exp'];
    if (!empty($exp) && $exp < $currentTime) {
        echo '- <Error></Error>. JWS has expired<br/>';
    } else {
        echo '+ JWS has not expired yet<br/>';
    }
    // name of your organisation, check if correct!
    $aud = $decoded_array['aud'];

    if ($aud === $uitgeverNaam) {
        echo "Token for incorrect organisation";
    }

    /**
     * The payload will contain a 'rnd' property. It is wise to validate that this value hasn't been used before, by
     * storing it in a cache/db. In this example we won't check 'rnd'.
     */

    echo '<br/><br/>EXP = expiry, SUB = subject (account), ingelogde gebruiker, EAN = europese artikelnummering, AUD = audience, your publisher name. ORG (optional) = organisation of the subject account, FN (optional) = first name of the user account, LAC (optional) = linked accounts, historic account identifiers (merges)<br/>';
    $data = array("jws" => $jws, "payload" => $decoded_array);
    $payload = json_encode($data);

    doCallback($payload, $callbackEndpoint);
}
?>
