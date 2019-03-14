package nl.topicus.example.model.request;

import nl.topicus.example.model.JwsPayload;

public class CallbackRequestModel {

    private JwsPayload payload;
    private String jws;

    public CallbackRequestModel(String jws, JwsPayload payload) {
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
