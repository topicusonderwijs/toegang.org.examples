<?php

/**
* This is an open resource for the purpose of showing how to
* use our license API. It should not be used as is, as this resource has no
* protection and would allow anyone to create licenses by loading an url.
*/
require_once 'oauthservice.php';

$config = include('config.php');

class LicenseApi {

    public static function createLicenses ($productEan, $referenceId, $uitgeverId, $aantalLicenties){

        global $config;

        session_start(); //Needed for storage of oauth token. Save in database if cached in production setting.

        $token = OAuthService::getClientCredentialsAccessToken();

        if(isset($token)){
                $headers = array('Content-Type: application/json', 'Authorization: Bearer '.$token);
                $queryString = "productId=".$productEan."&requestReferenceId=".$referenceId."&eckUsername=".$uitgeverId."&amount=".$aantalLicenties;
                $options = [
                    CURLOPT_URL => $config['api_license_uri']."?".$queryString,
                    CURLOPT_HTTPHEADER => $headers,
                    CURLOPT_POST => true,
                    CURLOPT_POSTFIELDS => "",
                    CURLOPT_RETURNTRANSFER => true
                ];
                $curl = curl_init();
                curl_setopt_array($curl, $options);
                $result = curl_exec($curl);
                $httpcode = curl_getinfo($curl, CURLINFO_HTTP_CODE);
                curl_close($curl);

                if($httpcode == '200') {
                    $json = json_decode($result, true);
                    return $json;
                }
        }
        error_log("Fout bij ophalen licenties, waarschijnlijk incorrect oauth credentials configured");
    }
}

?>
