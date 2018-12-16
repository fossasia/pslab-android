package io.pslab.interfaces.sensorloggers;

import io.pslab.models.BaroData;
import io.realm.RealmResults;

/**
 * Created by Padmal on 12/13/18.
 */

public interface BaroMeterRecordables {

    BaroData getBaroData(long timeStamp);

    void clearAllBaroRecords();

    void clearBlockOfBaroRecords(long block);

    RealmResults<BaroData> getAllBaroRecords();

    RealmResults<BaroData> getBlockOfBaroRecords(long block);
}
