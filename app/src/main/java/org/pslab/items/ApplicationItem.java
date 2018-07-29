package org.pslab.items;

/**
 * Created by Padmal on 5/7/17.
 */

public class ApplicationItem {

    private String applicationName;
    private int applicationIcon;
    private String applicationDescription;

    public ApplicationItem() {}

    public ApplicationItem(String applicationName, int applicaitonIcon, String applicationDescription) {
        this.applicationName = applicationName;
        this.applicationDescription = applicationDescription;
        this.applicationIcon = applicaitonIcon;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getApplicationDescription(){ return applicationDescription; }

    public int getApplicationIcon() {
        return applicationIcon;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public void setApplicationDescription(String applicationDescription){
        this.applicationDescription = applicationDescription;
    }

    public void setApplicationIcon(int applicationIcon) {
        this.applicationIcon = applicationIcon;
    }

}
