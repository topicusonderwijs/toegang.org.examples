package nl.topicus.example.security.helper;

import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class IdpHelper {

    private String tokenUri = "https://idp-ontwikkel.toegang.org/token";
    private Scope scope = new Scope("eck");

    public IdpHelper() {}

    public IdpHelper(String tokenUri, Scope scope) {
        this.tokenUri = tokenUri;
        this.scope = scope;
    }

    public IdpHelper(Scope scope) {
        this.scope = scope;
    }

    public IdpHelper(String tokenUri) {
        this.tokenUri = tokenUri;
    }

    private TokenResponse GetTokenResponse(String clientId, String clientSecret) throws ParseException, IOException, URISyntaxException
    {
        URI tokenEndpoint = new URI(tokenUri);

        AuthorizationGrant clientGrant = new ClientCredentialsGrant();

        ClientID clientID = new ClientID(clientId);
        Secret secret = new Secret(clientSecret);
        ClientAuthentication clientAuth = new ClientSecretBasic(clientID, secret);

        TokenRequest request = new TokenRequest(tokenEndpoint, clientAuth, clientGrant ,scope);

        return TokenResponse.parse(request.toHTTPRequest().send());
    }

    public AccessToken getAccessToken(String clientId,String clientSecret) throws Exception
    {
        TokenResponse tokenResponse = this.GetTokenResponse(clientId, clientSecret);
         AccessTokenResponse successResponse = tokenResponse.toSuccessResponse();
        return successResponse.getTokens().getAccessToken();
    }
}
