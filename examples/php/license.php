<?php

/**
* This is an open resource for the purpose of showing how to
* use our license API. It should not be used as is, as this resource has no
* protection and would allow anyone to create licenses by loading an url.
*/
require_once 'oauthservice.php';
$config = include('config.php');

/**
* Information on product to create licenses for.
*
*/
$productEan = urlencode('1111111111111');
$referenceId = urlencode(uniqid().uniqid());
$uitgeverId = urlencode('Uitgever ID');
$aantalLicenties = "12";

session_start(); //Needed for storage of oauth token. Save in database if cached in production setting.

$token = getClientCredentialsAccessToken();

if(isset($token)){
        $headers = array('Content-Type: application/json', 'Authorization: Bearer '.$token);
        $queryString = "productId=".$productEan."&requestReferenceId=".$referenceId."&distributorId=".$uitgeverId."&amount=".$aantalLicenties;
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
            foreach ($json['codes'] as $licentie){
                echo "You have created a license with code : ".$licentie."<br/>";
            }
            return;
        }
        echo "No licenses created, the call to the license API failed.";
        return;
}
else{
    echo "Could not retrieve token, did you configure the correct oauth2 credentials?";
}

?>
