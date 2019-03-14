package nl.topicus.example.controller;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/callback")
public interface Callback {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response postCallback(String req);
}
