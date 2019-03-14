package nl.topicus.example.model.request;

import nl.topicus.example.model.LicenseModel;

import java.util.HashMap;
import java.util.Map;

public class LicenseRequestModel {

    private Map<String,String> headers;
    private LicenseModel parameters;

    public LicenseRequestModel(String contentType, String authType, String token, LicenseModel parameters) {
        headers = new HashMap<>();
        headers.put("Content-Type", contentType);
        headers.put("Authorization", authType  +" " + token);
        this.parameters = parameters;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public LicenseModel getParameters() {
        return parameters;
    }
}
