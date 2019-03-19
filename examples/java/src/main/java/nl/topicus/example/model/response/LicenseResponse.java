package nl.topicus.example.model.response;

import java.util.Date;
import java.util.List;

public class LicenseResponse {

    private List<String> codes;
    private Date startDate = new Date();
    private Date endDate = new Date();

    public List<String> getCodes() {
        return codes;
    }

    public void setCodes(List<String> codes) {
        this.codes = codes;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
