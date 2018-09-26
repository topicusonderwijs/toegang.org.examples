<?php

$config = include('config.php');

function getClientCredentialsAccessToken()
{
    global $config;

    $token = $_SESSION["token"];
    $expiry = $_SESSION["expiry"];
    if(isset($token) && isset($expiry) && (time() < $expiry)) {
        return $token;
    }

    /**
     * Please change the following values to your uitgevers oauth credentials
     * You could also use a oauth2 client library, but as our php.minversion = 5.3 we use curl.
     * Preferably store your oauth-credentials in your default secure storage.
     */

    $headers = array('Content-Type: application/x-www-form-urlencoded');

    $options = [
        CURLOPT_URL => $config['oauth_token_uri'],
        CURLOPT_HTTPHEADER => $headers,
        CURLOPT_POST => true,
        CURLOPT_USERPWD => $config['oauth_client_id'].":".$config['oauth_client_secret'],
        CURLOPT_HTTPAUTH => CURLAUTH_BASIC,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_POSTFIELDS => "grant_type=client_credentials&client_id=".$config['oauth_client_id']
    ];

    $curl = curl_init();
    curl_setopt_array($curl, $options);

    $result = curl_exec($curl);
    $httpcode = curl_getinfo($curl, CURLINFO_HTTP_CODE);
    curl_close($curl);
    if($httpcode == '200') {
        $json = json_decode($result, true);
        $_SESSION["token"] = $json['access_token'];
        $_SESSION["expiry"] = time() + intval($json['expires_in']);
        return $json['access_token'];
    }
    return;
}
?>
