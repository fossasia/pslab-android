package io.pslab.models;

public class PowerSourceData {

    private String pv1;
    private String pv2;
    private String pv3;
    private String pcs;

    public PowerSourceData() {/**/}

    public PowerSourceData(String pv1, String pv2, String pv3,String pcs) {
        this.pv1 = pv1;
        this.pv2 = pv2;
        this.pv3 = pv3;
        this.pcs = pcs;
    }

    public String getPv1() {
        return pv1;
    }
    public void setPv1(String pv1) {
        this.pv1 = pv1;
    }
    public String getPv2() {
        return pv2;
    }
    public void setPv2(String pv2) {
        this.pv2 = pv2;
    }
    public String getPv3() {
        return pv3;
    }
    public void setPv3(String pv3) {
        this.pv3 = pv3;
    }
    public String getPcs() {
        return pcs;
    }
    public void setPcs(String pcs) {
        this.pcs = pcs;
    }



}
