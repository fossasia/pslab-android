package io.pslab.interfaces.sensorloggers;

import io.pslab.models.SoundData;
import io.realm.RealmResults;

/**
 * @author reckoner1429
 *
 */
public interface SoundMeterRecordables {

    SoundData getSoundMeterData(long timeStamp);

    void clearAllSoundRecords();

    void clearBlockOfSoundRecords(long block);

    RealmResults<SoundData> getAllSoundRecords();

    RealmResults<SoundData> getBlockOfSoundRecords(long block);
}
