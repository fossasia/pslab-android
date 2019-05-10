package io.pslab.interfaces.sensorloggers;

import io.pslab.models.CompassData;
import io.realm.RealmResults;

public interface CompassRecordables {
    CompassData getCompassData(long timeStamp);

    void clearAllCompassRecords();

    void clearBlockOfCompassRecords(long block);

    RealmResults<CompassData> getAllCompassRecords();

    RealmResults<CompassData> getBlockOfCompassRecords(long block);
}
