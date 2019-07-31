package io.pslab.interfaces.sensorloggers;

import io.pslab.models.MultimeterData;
import io.realm.RealmResults;

public interface MultimeterRecordables {
    MultimeterData getMultimeterData(long timeStamp);
    void clearAllMultimeterRecords();
    void clearBlockOfMultimeterRecords(long block);
    RealmResults<MultimeterData> getAllMultimeterRecords();
    RealmResults<MultimeterData> getBlockOfMultimeterRecords(long block);
}
