package nl.topicus.example.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import nl.topicus.example.client.ToegangClient;
import nl.topicus.example.model.request.LicenseBodyRequest;
import nl.topicus.example.model.response.LicenseResponse;
import nl.topicus.example.security.helper.IdpHelper;

import javax.ws.rs.core.Response;

public class LicenseCreator {

    private static LicenseCreator instance;

    private LicenseResponse licenseResponse = new LicenseResponse();
    private LicenseBodyRequest licenseBodyRequest;
    private AccessToken accessToken;

    private LicenseCreator() { }

    public synchronized static LicenseCreator getInstance() {
        if (instance == null) {
            instance = new LicenseCreator();
            return instance;
        } else {
            return instance;
        }
    }

    /**
     * Roept de methode aan die het verzoek, om de licensies op te halen, verstuurd. Er wordt een access token opgevraagd doormiddel van de client id en secret
     * Zet het object dat wordt gegeven als response om naar de LicenseResponse class
     *
     * @param clientId hiermee wordt de access token opgevraagd
     * @param clientSecret in combinatie met de clientId verkrijgen we een access token
     * @return het LicenseResponse object dat de licensies, start en eind datum bevat
     * @throws Exception zodra er geen token kan worden gevonden met de client id & secret combinatie. Wanneer de json waarde niet kan worden geplaatst in het aangegeven object
     */
    public LicenseResponse requestLicenses(String clientId, String clientSecret) throws Exception {
        Response response = ToegangClient.getInstance().sendLicenseRequest(requestAccessToken(clientId, clientSecret).toString());
        this.licenseResponse = new ObjectMapper().readValue(response.readEntity(String.class), LicenseResponse.class);
        response.close();
        return this.licenseResponse;
    }

    /*
     * idp-ontwikkel:
     * client ID: "6872181b-b4f5-4e92-9574-f8ae058485fa"
     * client secret: "735cec20-50de-49ed-828d-f269c9cdf89a"
     */
    private AccessToken requestAccessToken(String clientId, String clientSecret) throws Exception {
        this.accessToken = new IdpHelper().getAccessToken(clientId, clientSecret);
        return this.accessToken;
    }

    /**
     * Instantieërt het object dat gebruikt zal worden gebruikt om de licensies aan te vragen. Deze moet geinstantieërd worden al voordat je een verzoek indient
     * Het object dat wordt geinstantieërd, zal worden opgeslagen in de LicenseCreator class
     *
     * @return het geinstantieerde object
     */
    public LicenseBodyRequest setLicenseBodyRequest(String productId, String distributorId, int amountOfLicenses, String requestReferenceId) {
        this.licenseBodyRequest = new LicenseBodyRequest(productId, distributorId,
                amountOfLicenses, requestReferenceId);
        return this.licenseBodyRequest;
    }

    public LicenseResponse getLicenseResponse() {
        return licenseResponse;
    }

    public LicenseBodyRequest getLicenseBodyRequest() {
        return licenseBodyRequest;
    }

    public AccessToken getAccessToken() {
        return accessToken;
    }
}
