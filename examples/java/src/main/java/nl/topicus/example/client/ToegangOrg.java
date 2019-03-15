package nl.topicus.example.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.topicus.example.controller.Toegang;
import nl.topicus.example.model.request.JwsPayloadRequest;
import nl.topicus.example.model.JwsPayload;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

public class ToegangOrg {

    private static final String PATH = "https://api-ontwikkel.toegang.org";

    private ToegangOrg() { }

    public static ToegangOrg getInstance(){
        return new ToegangOrg();
    }

    public synchronized Response sendPayloadAndToken(JwsPayload payload, String jws) {

        try {
            ResteasyClient client = new ResteasyClientBuilder().build();
            ResteasyWebTarget target = client.target(UriBuilder.fromPath(PATH));
            Toegang proxy = target.proxy(Toegang.class);
            String callbackRequest = new ObjectMapper().writeValueAsString(new JwsPayloadRequest(jws, payload));
            Response callbackResponse = proxy.postCallback(callbackRequest);
            callbackResponse.close();

            return callbackResponse;

        } catch (ProcessingException | JsonProcessingException jpe) {
            return Response.status(400, jpe.getMessage()).build();
        }

    }
}
