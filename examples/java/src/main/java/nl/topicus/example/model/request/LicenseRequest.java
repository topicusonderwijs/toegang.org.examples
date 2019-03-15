package nl.topicus.example.model.request;

import nl.topicus.example.model.LicenseInformation;

import java.util.HashMap;
import java.util.Map;

public class LicenseRequest {

    private Map<String,String> headers;
    private LicenseInformation parameters;

    public LicenseRequest(String contentType, String authType, String token, LicenseInformation parameters) {
        headers = new HashMap<>();
        headers.put("Content-Type", contentType);
        headers.put("Authorization", authType  +" " + token);
        this.parameters = parameters;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public LicenseInformation getParameters() {
        return parameters;
    }
}
