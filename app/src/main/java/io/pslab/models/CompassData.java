package io.pslab.models;

public class CompassData {
    private String Bx;
    private String By;
    private String Bz;

    public CompassData() {/**/}

    public CompassData(String Bx, String By, String Bz) {
        this.Bx = Bx;
        this.By = By;
        this.Bz = Bz;
    }

    public String getBx() {
        return Bx;
    }

    public void setBx(String Bx) {
        this.Bx = Bx;
    }

    public String getBy() {
        return By;
    }

    public void setBy(String By) {
        this.By = By;
    }

    public String getBz() {
        return Bz;
    }

    public void setBz(String Bz) {
        this.Bz = Bz;
    }
}
