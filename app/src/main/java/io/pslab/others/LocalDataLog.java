package io.pslab.others;

import io.pslab.interfaces.sensorloggers.BaroMeterRecordables;
import io.pslab.interfaces.sensorloggers.CompassRecordables;
import io.pslab.interfaces.sensorloggers.LuxMeterRecordables;
import io.pslab.interfaces.sensorloggers.SensorRecordables;
import io.pslab.models.BaroData;
import io.pslab.models.CompassData;
import io.pslab.models.LuxData;
import io.pslab.models.SensorDataBlock;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Padmal on 11/5/18.
 */

public class LocalDataLog implements LuxMeterRecordables, BaroMeterRecordables, SensorRecordables, CompassRecordables {

    private static LocalDataLog instance;
    private final Realm realm;

    private LocalDataLog() {
        realm = Realm.getDefaultInstance();
    }

    public static LocalDataLog with() {
        if (instance == null) {
            instance = new LocalDataLog();
        }
        return instance;
    }

    public static LocalDataLog getInstance() {
        return instance;
    }

    public Realm getRealm() {
        return realm;
    }

    public void refresh() {
        realm.refresh();
    }

    /***********************************************************************************************
     * Generic Sensor Section
     ***********************************************************************************************/
    @Override
    public SensorDataBlock getSensorBlock(long block) {
        return realm.where(SensorDataBlock.class).equalTo("block", block).findFirst();
    }

    @Override
    public void clearAllSensorBlocks() {
        realm.beginTransaction();
        realm.delete(SensorDataBlock.class);
        realm.commitTransaction();
    }

    @Override
    public void clearTypeOfSensorBlock(String type) {
        realm.beginTransaction();
        RealmResults<SensorDataBlock> data = getTypeOfSensorBlocks(type);
        data.deleteAllFromRealm();
        realm.commitTransaction();
    }

    @Override
    public void clearSensorBlock(long block) {
        realm.beginTransaction();
        SensorDataBlock dataBlock = getSensorBlock(block);
        dataBlock.deleteFromRealm();
        realm.commitTransaction();
    }

    @Override
    public RealmResults<SensorDataBlock> getAllSensorBlocks() {
        return realm.where(SensorDataBlock.class)
                .findAll().sort("block", Sort.DESCENDING);
    }

    @Override
    public RealmResults<SensorDataBlock> getTypeOfSensorBlocks(String type) {
        return realm.where(SensorDataBlock.class)
                .equalTo("sensorType", type)
                .findAll().sort("block", Sort.DESCENDING);
    }

    /***********************************************************************************************
    * Lux Sensor Section
    ***********************************************************************************************/
    @Override
    public LuxData getLuxData(long timestamp) {
        return realm.where(LuxData.class).equalTo("time", timestamp).findFirst();
    }

    @Override
    public void clearAllLuxRecords() {
        realm.beginTransaction();
        realm.delete(LuxData.class);
        realm.commitTransaction();
    }

    @Override
    public void clearBlockOfLuxRecords(long block) {
        realm.beginTransaction();
        RealmResults<LuxData> data = getBlockOfLuxRecords(block);
        data.deleteAllFromRealm();
        realm.commitTransaction();
    }

    @Override
    public RealmResults<LuxData> getAllLuxRecords() {
        return realm.where(LuxData.class).findAll();
    }

    @Override
    public RealmResults<LuxData> getBlockOfLuxRecords(long block) {
        return realm.where(LuxData.class)
                .equalTo("block", block)
                .findAll();
    }

    /***********************************************************************************************
     * Baro Sensor Section
     ***********************************************************************************************/
    @Override
    public BaroData getBaroData(long timestamp) {
        return realm.where(BaroData.class).equalTo("time", timestamp).findFirst();
    }

    @Override
    public void clearAllBaroRecords() {
        realm.beginTransaction();
        realm.delete(BaroData.class);
        realm.commitTransaction();
    }

    @Override
    public void clearBlockOfBaroRecords(long block) {
        realm.beginTransaction();
        RealmResults<BaroData> data = getBlockOfBaroRecords(block);
        data.deleteAllFromRealm();
        realm.commitTransaction();
    }

    @Override
    public RealmResults<BaroData> getAllBaroRecords() {
        return realm.where(BaroData.class).findAll();
    }

    @Override
    public RealmResults<BaroData> getBlockOfBaroRecords(long block) {
        return realm.where(BaroData.class)
                .equalTo("block", block)
                .findAll();
    }

    /***********************************************************************************************
     * Compass Section
     ***********************************************************************************************/
    @Override
    public CompassData getCompassData(long timeStamp) {
        return realm.where(CompassData.class).equalTo("time", timeStamp).findFirst();
    }

    @Override
    public void clearAllCompassRecords() {
        realm.beginTransaction();
        realm.delete(CompassData.class);
        realm.commitTransaction();
    }

    @Override
    public void clearBlockOfCompassRecords(long block) {
        realm.beginTransaction();
        RealmResults<CompassData> data = getBlockOfCompassRecords(block);
        data.deleteAllFromRealm();
        realm.commitTransaction();
    }

    @Override
    public RealmResults<CompassData> getAllCompassRecords() {
        return realm.where(CompassData.class).findAll();
    }

    @Override
    public RealmResults<CompassData> getBlockOfCompassRecords(long block) {
        return realm.where(CompassData.class)
                .equalTo("block", block)
                .findAll();
    }
}
