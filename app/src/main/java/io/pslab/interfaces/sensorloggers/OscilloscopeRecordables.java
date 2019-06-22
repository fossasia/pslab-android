package io.pslab.interfaces.sensorloggers;

import io.pslab.models.OscilloscopeData;
import io.realm.RealmResults;

public interface OscilloscopeRecordables {
    OscilloscopeData getOscilloscopeData(long timeStamp);

    void clearAllOscilloscopeRecords();

    void clearBlockOfOscilloscopeRecords(long block);

    RealmResults<OscilloscopeData> getAllOscilloscopeRecords();

    RealmResults<OscilloscopeData> getBlockOfOscilloscopeRecords(long block);
}
