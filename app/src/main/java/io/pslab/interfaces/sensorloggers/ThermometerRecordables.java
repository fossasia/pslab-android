package io.pslab.interfaces.sensorloggers;

import io.pslab.models.ThermometerData;
import io.realm.RealmResults;

public interface ThermometerRecordables {
    ThermometerData getThermometerData(long timeStamp);

    void clearAllThermometerRecords();

    void clearBlockOfThermometerRecords(long block);

    RealmResults<ThermometerData> getAllThermometerRecords();

    RealmResults<ThermometerData> getBlockOfThermometerRecords(long block);
}
