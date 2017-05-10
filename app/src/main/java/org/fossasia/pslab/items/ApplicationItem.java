package org.fossasia.pslab.items;

/**
 * Created by Padmal on 5/7/17.
 */

public class ApplicationItem {

    private String applicationName;
    private int applicationIcon;

    public ApplicationItem() {}

    public ApplicationItem(String applicationName, int applicaitonIcon) {
        this.applicationName = applicationName;
        this.applicationIcon = applicaitonIcon;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public int getApplicationIcon() {
        return applicationIcon;
    }

    public void setApplicationIcon(int applicationIcon) {
        this.applicationIcon = applicationIcon;
    }

}
