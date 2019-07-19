package io.pslab.interfaces.sensorloggers;

import io.pslab.models.PowerSourceData;
import io.realm.RealmResults;

public interface PowerSourceRecordables {
    PowerSourceData getPowerData(long timeStamp);
    void clearAllPowerRecords();
    void clearBlockOfPowerRecords(long block);
    RealmResults<PowerSourceData> getAllPowerRecords();
    RealmResults<PowerSourceData> getBlockOfPowerRecords(long block);
}
