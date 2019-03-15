package nl.topicus.example.model.request;

import nl.topicus.example.model.JwsPayload;

public class JwsPayloadRequest {

    private JwsPayload payload;
    private String jws;

    public JwsPayloadRequest(String jws, JwsPayload payload) {
        this.jws = jws;
        this.payload = payload;
    }

    public String getJws() {
        return jws;
    }

    public void setJws(String jws) {
        this.jws = jws;
    }

    public JwsPayload getPayload() {
        return payload;
    }

    public void setPayload(JwsPayload payload) {
        this.payload = payload;
    }
}
