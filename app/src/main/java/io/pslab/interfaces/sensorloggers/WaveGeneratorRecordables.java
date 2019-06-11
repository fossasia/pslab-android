package io.pslab.interfaces.sensorloggers;

import io.pslab.models.WaveGeneratorData;
import io.realm.RealmResults;

public interface WaveGeneratorRecordables {
    WaveGeneratorData getWaveData(long timeStamp);

    void clearAllWaveRecords();

    void clearBlockOfWaveRecords(long block);

    RealmResults<WaveGeneratorData> getAllWaveRecords();

    RealmResults<WaveGeneratorData> getBlockOfWaveRecords(long block);
}
