package nl.topicus.example.model;

public class LicenseInformation {

    private String productId;
    private String distributorId;
    private long aantalLicenties;
    private String requestReferenceId;

    public LicenseInformation(String productId, String distributorId, long aantalLicenties, String requestReferenceId) {
        this.productId = productId;
        this.distributorId = distributorId;
        this.aantalLicenties = aantalLicenties;
        this.requestReferenceId = requestReferenceId;
    }

    public String getProductId() {
        return productId;
    }

    public String getDistributorId() {
        return distributorId;
    }

    public long getAantalLicenties() {
        return aantalLicenties;
    }

    public String getRequestReferenceId() {
        return requestReferenceId;
    }
}
