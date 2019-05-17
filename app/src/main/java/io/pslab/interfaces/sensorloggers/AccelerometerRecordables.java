package io.pslab.interfaces.sensorloggers;

import io.pslab.models.AccelerometerData;
import io.realm.RealmResults;

public interface AccelerometerRecordables {

    AccelerometerData getAccelerometerData(long timeStamp);

    void clearAllAccelerometerRecords();

    void clearBlockOfAccelerometerRecords(long block);

    RealmResults<AccelerometerData> getAllAccelerometerRecords();

    RealmResults<AccelerometerData> getBlockOfAccelerometerRecords(long block);
}
