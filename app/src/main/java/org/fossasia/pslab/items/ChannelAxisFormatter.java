package org.fossasia.pslab.items;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

/**
 * Created by viveksb007 on 14/6/17.
 */

public class ChannelAxisFormatter implements IAxisValueFormatter {

    String[] laChannelNames = new String[]{"ID1", "ID2", "ID3", "ID4"};

    public ChannelAxisFormatter() {

    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        switch ((int) value) {
            case 1:
                return "ID1";
            case 3:
                return "ID2";
            case 5:
                return "ID3";
            case 7:
                return "ID4";
        }
        return "";
    }

}
