package com.yuaoq.yabiz.mobile.dt;

/**
 * Created by changsy on 2018/3/7.
 */
public class CustInfo {
    
    private String custId;
    private String custName;
    private String mobile;
    private String crmPartyId;
    private String partyId;
    
    public String getCrmPartyId() {
        return crmPartyId;
    }
    
    public void setCrmPartyId(String crmPartyId) {
        this.crmPartyId = crmPartyId;
    }
    
    public String getPartyId() {
        return partyId;
    }
    
    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }
    
    public String getCustId() {
        return custId;
    }
    
    public void setCustId(String custId) {
        this.custId = custId;
    }
    
    public String getCustName() {
        return custName;
    }
    
    public void setCustName(String custName) {
        this.custName = custName;
    }
    
    public String getMobile() {
        return mobile;
    }
    
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
