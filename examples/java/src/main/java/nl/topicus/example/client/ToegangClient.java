package nl.topicus.example.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.topicus.example.api.LicenseCreator;
import nl.topicus.example.controller.Toegang;
import nl.topicus.example.model.request.LicenseBodyRequest;
import nl.topicus.example.model.request.JwsPayloadRequest;
import nl.topicus.example.model.JwsPayload;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

public class ToegangClient {

    private static final String PATH_API = "https://api-ontwikkel.toegang.org";

    private ToegangClient() { }

    public static ToegangClient getInstance(){
        return new ToegangClient();
    }

    public Response sendPayloadAndToken(JwsPayload payload, String jws) {

        try {
            Toegang proxy = setupProxy();
            String callbackRequest = new ObjectMapper().writeValueAsString(new JwsPayloadRequest(jws, payload));
            Response callbackResponse = proxy.jwsCallback(callbackRequest);
            callbackResponse.close();

            return callbackResponse;

        } catch (ProcessingException | JsonProcessingException jpe) {

            return Response.status(400, jpe.getMessage()).build();
        }

    }

    public Response sendLicenseRequest(String accessToken){

        LicenseBodyRequest licenseBodyRequest = LicenseCreator.getInstance().getLicenseBodyRequest();

        try {
            Toegang proxy = setupProxy();

            return proxy.requestLicenses("Bearer " + accessToken, licenseBodyRequest.getProductId(), licenseBodyRequest.getRequestReferenceId(), licenseBodyRequest.getAmount(), licenseBodyRequest.getDistributorId());

        } catch (ProcessingException jpe) {
            jpe.printStackTrace();

            return Response.status(400, jpe.getMessage()).build();
        }
    }

    private Toegang setupProxy(){
        ResteasyClient client = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = client.target(UriBuilder.fromPath(PATH_API));

        return target.proxy(Toegang.class);
    }
}
