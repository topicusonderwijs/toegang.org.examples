<?php

$idp_base_uri = 'https://idp-test.toegang.org';
$api_base_uri = 'https://api-test.toegang.org';

return array(
    'idp_base_uri' => $idp_base_uri,
    'api_base_uri' => $api_base_uri,
    'oauth_client_id' => 'test',
    'oauth_client_secret' => 'test',
    'oauth_token_uri' => $idp_base_uri.'/token',
    'oauth_auth_uri' => $idp_base_uri.'/auth',
    'oauth_redirect_uri' => 'http://localhost:3000',
    'sso_callback_uri' => $api_base_uri.'/callback',
    'api_license_uri' => $api_base_uri.'/tlinklicenses/getLicenseCodes',
);
?>