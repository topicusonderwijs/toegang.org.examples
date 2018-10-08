const IDP_BASE_URI = 'https://idp-test.toegang.org';
const API_BASE_URI = 'https://api-test.toegang.org';

module.exports = {
    uitgever_naam: 'Naam van uitgever',
    oauth_client_id: 'test',
    oauth_client_secret: 'test',
    oauth_token_uri: IDP_BASE_URI + '/token',
    oauth_auth_uri: IDP_BASE_URI + '/auth',
    oauth_redirect_uri: 'http://localhost:3000',
    sso_callback_uri: API_BASE_URI + '/callback',
    api_license_uri: API_BASE_URI + '/tlinklicenses/getLicenseCodes',
    idp_base_uri: IDP_BASE_URI,
    api_base_uri: API_BASE_URI
}