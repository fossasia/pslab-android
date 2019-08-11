package io.pslab.interfaces.sensorloggers;

import io.pslab.models.GasSensorData;
import io.realm.RealmResults;

public interface GasSensorRecordables {
    GasSensorData getGasSensorData(long timeStamp);
    void clearAllGasSensorRecords();
    void clearBlockOfGasSensorRecords(long block);
    RealmResults<GasSensorData> getAllGasSensorRecords();
    RealmResults<GasSensorData> getBlockOfGasSensorRecords(long block);
}
