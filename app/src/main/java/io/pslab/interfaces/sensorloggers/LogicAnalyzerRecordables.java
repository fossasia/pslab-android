package io.pslab.interfaces.sensorloggers;

import io.pslab.models.LogicAnalyzerData;
import io.realm.RealmResults;

public interface LogicAnalyzerRecordables {
    LogicAnalyzerData getLAData(long timeStamp);
    void clearAllLARecords();
    void clearBlockOfLARecords(long block);
    RealmResults<LogicAnalyzerData> getAllLARecords();
    RealmResults<LogicAnalyzerData> getBlockOfLARecords(long block);
}
