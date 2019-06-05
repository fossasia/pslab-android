package io.pslab.interfaces.sensorloggers;

import io.pslab.models.ServoData;
import io.realm.RealmResults;

public interface ServoRecordables {
    ServoData getServoData(long timeStamp);

    void clearAllServoRecords();

    void clearBlockOfServoRecords(long block);

    RealmResults<ServoData> getAllServoRecords();

    RealmResults<ServoData> getBlockOfServoRecords(long block);
}
