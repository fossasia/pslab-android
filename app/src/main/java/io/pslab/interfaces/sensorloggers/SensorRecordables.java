package io.pslab.interfaces.sensorloggers;

import io.pslab.models.SensorDataBlock;
import io.realm.RealmResults;

/**
 * Created by Padmal on 11/5/18.
 */

public interface SensorRecordables {

    SensorDataBlock getSensorBlock(long block);

    void clearAllSensorBlocks();

    void clearTypeOfSensorBlock(String type);

    void clearSensorBlock(long block);

    RealmResults<SensorDataBlock> getAllSensorBlocks();

    RealmResults<SensorDataBlock> getTypeOfSensorBlocks(String type);
}
