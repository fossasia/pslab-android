package io.pslab.interfaces.sensorloggers;

import io.pslab.models.DustSensorData;
import io.realm.RealmResults;

public interface DustSensorRecordables {

    DustSensorData getDustSensorData(long timeStamp);
    void clearAllDustSensorRecords();
    void clearBlockOfDustSensorRecords(long block);
    RealmResults<DustSensorData> getAllDustSensorRecords();
    RealmResults<DustSensorData> getBlockOfDustSensorRecords(long block);
}
