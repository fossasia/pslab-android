package io.pslab.others;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;

public class LogicAnalyzerAxisFormatter extends ValueFormatter {

    private ArrayList<String> laChannelNames;

    public LogicAnalyzerAxisFormatter(ArrayList<String> channelNames) {
        this.laChannelNames = channelNames;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        if (value > laChannelNames.size() * 2 - 1) {
            switch (laChannelNames.size()) {
                case 1:
                    return laChannelNames.get(0);
                case 2:
                    return laChannelNames.get(1);
                case 3:
                    return laChannelNames.get(2);
                case 4:
                    return laChannelNames.get(3);
                default:
                    return "";
            }
        } else {
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

}
