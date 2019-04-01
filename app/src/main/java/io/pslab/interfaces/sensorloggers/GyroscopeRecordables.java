package io.pslab.interfaces.sensorloggers;

import io.pslab.models.GyroData;
import io.realm.RealmResults;

public interface GyroscopeRecordables {
    GyroData getGyroData(long timeStamp);

    void clearAllGyroRecords();

    void clearBlockOfGyroRecords(long block);

    RealmResults<GyroData> getAllGyroRecords();

    RealmResults<GyroData> getBlockOfGyroRecords(long block);
}
