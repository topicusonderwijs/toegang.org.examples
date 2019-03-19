package nl.topicus.example.model.request;

public class LicenseBodyRequest {

    private String productId;
    private String distributorId;
    private int amount;
    private String requestReferenceId;

    public LicenseBodyRequest(String productId, String distributorId, int amount, String requestReferenceId) {
        this.productId = productId;
        this.distributorId = distributorId;
        this.amount = amount;
        this.requestReferenceId = requestReferenceId;
    }

    public String getProductId() {
        return productId;
    }

    public String getDistributorId() {
        return distributorId;
    }

    public int getAmount() {
        return amount;
    }

    public String getRequestReferenceId() {
        return requestReferenceId;
    }
}
