package io.pslab.interfaces.sensorloggers;

import io.pslab.models.LuxData;
import io.realm.RealmResults;

/**
 * Created by Padmal on 11/5/18.
 */

public interface LuxMeterRecordables {

    LuxData getLuxData(long timeStamp);

    void clearAllLuxRecords();

    void clearBlockOfLuxRecords(long block);

    RealmResults<LuxData> getAllLuxRecords();

    RealmResults<LuxData> getBlockOfLuxRecords(long block);
}
