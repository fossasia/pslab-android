package io.pslab.items;

/**
 * Created by Padmal on 5/22/18.
 */

public class PinDetails {

    private final String name;
    private final String description;
    private final int categoryColor;
    private final int colorID;

    public PinDetails(String name, String description, int categoryColor, int colorID) {
        this.name = name;
        this.description = description;
        this.categoryColor = categoryColor;
        this.colorID = colorID;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getCategoryColor() {
        return categoryColor;
    }

    public int getColorID() {
        return colorID;
    }
}
