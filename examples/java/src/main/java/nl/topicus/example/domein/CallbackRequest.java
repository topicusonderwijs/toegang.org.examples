package nl.topicus.example.domein;

public class CallbackRequest {

    private Payload payload;
    private String jws;

    public CallbackRequest(String jws, Payload payload) {
        this.jws = jws;
        this.payload = payload;
    }

    public String getJws() {
        return jws;
    }

    public void setJws(String jws) {
        this.jws = jws;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }
}
