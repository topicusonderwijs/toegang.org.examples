package nl.topicus.example.controller;

import org.jboss.resteasy.annotations.jaxrs.HeaderParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public interface Toegang {

    @POST
    @Path("/callback")
    @Consumes(MediaType.APPLICATION_JSON)
    Response jwsCallback(String req);

    @POST
    @Path("/tlinklicenses/getLicenseCodes")
    @Consumes(MediaType.APPLICATION_JSON)
    Response requestLicenses(@HeaderParam("Authorization") String header, @QueryParam("productId") String productId, @QueryParam("requestReferenceId") String requestReferenceId,
                             @QueryParam("amount") int amount, @QueryParam("distributorId") String distributorId);
}
