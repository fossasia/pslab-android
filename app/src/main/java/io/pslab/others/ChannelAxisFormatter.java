package io.pslab.others;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.List;

/**
 * Created by viveksb007 on 14/6/17.
 */

public class ChannelAxisFormatter implements IAxisValueFormatter {

    private final List<String> laChannelNames;

    public ChannelAxisFormatter(List<String> channelNames) {
        this.laChannelNames = channelNames;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        switch ((int) value) {
            case 1:
                return laChannelNames.get(0);
            case 3:
                return laChannelNames.get(1);
            case 5:
                return laChannelNames.get(2);
            case 7:
                return laChannelNames.get(3);
            default:
                return "";
        }
    }

}
