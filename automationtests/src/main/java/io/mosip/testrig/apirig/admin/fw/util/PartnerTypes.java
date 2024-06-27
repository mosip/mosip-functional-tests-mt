package io.mosip.testrig.apirig.admin.fw.util;

public enum PartnerTypes {

    DEVICE("device"), 
    RELYING_PARTY("rp"), 
    FTM("ftm"),
    EKYC("ekyc"),
    MISP("misp");

    private String filePrepend;

    private PartnerTypes(String filePrepend) {
        this.filePrepend = filePrepend;
    }

    public String getFilePrepend() {
        return this.filePrepend;
    }
}
