<?php

$idp_base_uri = 'https://idp-test.toegang.org';
$api_base_uri = 'https://api-test.toegang.org';
$ui_base_uri = 'https://test.toegang.org';
$public_key = <<<EOD
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

return array(
    'idp_base_uri' => $idp_base_uri,
    'api_base_uri' => $api_base_uri,
    'ui_base_uri' => $ui_base_uri,
    'uitgever_naam' => 'Uitgever X',
    'oauth_client_id' => 'YOUR_OAUTH_CLIENT_ID',
    'oauth_client_secret' => 'YOUR_OAUTH_CLIENT_SECRET',
    'oauth_token_uri' => $idp_base_uri.'/token',
    'oauth_auth_uri' => $idp_base_uri.'/auth',
    'oauth_redirect_uri' => 'http://localhost:3000',
    'sso_callback_uri' => $api_base_uri.'/callback',
    'api_license_uri' => $api_base_uri.'/tlinklicenses/getLicenseCodes',
    'jwt_public_key' => $public_key
);
?>