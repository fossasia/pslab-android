package io.pslab.others;

import io.pslab.interfaces.sensorloggers.AccelerometerRecordables;
import io.pslab.interfaces.sensorloggers.BaroMeterRecordables;
import io.pslab.interfaces.sensorloggers.GyroscopeRecordables;
import io.pslab.interfaces.sensorloggers.CompassRecordables;
import io.pslab.interfaces.sensorloggers.LuxMeterRecordables;
import io.pslab.interfaces.sensorloggers.SensorRecordables;
import io.pslab.interfaces.sensorloggers.ServoRecordables;
import io.pslab.interfaces.sensorloggers.ThermometerRecordables;
import io.pslab.models.AccelerometerData;
import io.pslab.models.BaroData;
import io.pslab.models.GyroData;
import io.pslab.models.CompassData;
import io.pslab.models.LuxData;
import io.pslab.models.SensorDataBlock;
import io.pslab.models.ServoData;
import io.pslab.models.ThermometerData;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Padmal on 11/5/18.
 */

public class LocalDataLog implements LuxMeterRecordables, BaroMeterRecordables, SensorRecordables, CompassRecordables, AccelerometerRecordables, GyroscopeRecordables, ThermometerRecordables, ServoRecordables {

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
     * Accelerometer Sensor Section
     ***********************************************************************************************/
    @Override
    public AccelerometerData getAccelerometerData(long timestamp) {
        return realm.where(AccelerometerData.class).equalTo("time", timestamp).findFirst();
    }

    @Override
    public void clearAllAccelerometerRecords() {
        realm.beginTransaction();
        realm.delete(AccelerometerData.class);
        realm.commitTransaction();
    }

    @Override
    public void clearBlockOfAccelerometerRecords(long block) {
        realm.beginTransaction();
        RealmResults<AccelerometerData> data = getBlockOfAccelerometerRecords(block);
        data.deleteAllFromRealm();
        realm.commitTransaction();
    }

    @Override
    public RealmResults<AccelerometerData> getAllAccelerometerRecords() {
        return realm.where(AccelerometerData.class).findAll();
    }

    @Override
    public RealmResults<AccelerometerData> getBlockOfAccelerometerRecords(long block) {
        return realm.where(AccelerometerData.class)
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
     * Gyroscope Section
     ***********************************************************************************************/
    @Override
    public GyroData getGyroData(long timeStamp) {
        return realm.where(GyroData.class).equalTo("time", timeStamp).findFirst();
    }

    @Override
    public void clearAllGyroRecords() {
        realm.beginTransaction();
        realm.delete(GyroData.class);
        realm.commitTransaction();
    }

    @Override
    public void clearBlockOfGyroRecords(long block) {
        realm.beginTransaction();
        RealmResults<GyroData> data = getBlockOfGyroRecords(block);
        data.deleteAllFromRealm();
        realm.commitTransaction();
    }

    @Override
    public RealmResults<GyroData> getAllGyroRecords() {
        return realm.where(GyroData.class).findAll();
    }

    @Override
    public RealmResults<GyroData> getBlockOfGyroRecords(long block) {
        return realm.where(GyroData.class).equalTo("block", block).findAll();
    }

    /***********************************************************************************************
     * Compass Section
     ***********************************************************************************************/
    @Override
    public CompassData getCompassData(long timeStamp) {
        return realm.where(CompassData.class)
                .equalTo("time", timeStamp)
                .findFirst();
    }

    @Override
    public void clearAllCompassRecords() {
        realm.beginTransaction();
        realm.delete(CompassData.class);
        realm.commitTransaction();
    }

    public void clearBlockOfCompassRecords(long block) {
        realm.beginTransaction();
        RealmResults<CompassData> data = getBlockOfCompassRecords(block);
        data.deleteAllFromRealm();
        realm.commitTransaction();
    }

    public RealmResults<CompassData> getAllCompassRecords() {
        return realm.where(CompassData.class).findAll();
    }

    @Override
    public RealmResults<CompassData> getBlockOfCompassRecords(long block) {
        return realm.where(CompassData.class)
                .equalTo("block", block)
                .findAll();
    }

    /***********************************************************************************************
     * Thermometer Section
     ***********************************************************************************************/
    @Override
    public ThermometerData getThermometerData(long timeStamp) {
        return realm.where(ThermometerData.class)
                .equalTo("time", timeStamp)
                .findFirst();
    }

    @Override
    public void clearAllThermometerRecords() {
        realm.beginTransaction();
        realm.delete(CompassData.class);
        realm.commitTransaction();
    }

    public void clearBlockOfThermometerRecords(long block) {
        realm.beginTransaction();
        RealmResults<ThermometerData> data = getBlockOfThermometerRecords(block);
        data.deleteAllFromRealm();
        realm.commitTransaction();
    }

    public RealmResults<ThermometerData> getAllThermometerRecords() {
        return realm.where(ThermometerData.class).findAll();
    }

    @Override
    public RealmResults<ThermometerData> getBlockOfThermometerRecords(long block) {
        return realm.where(ThermometerData.class)
                .equalTo("block", block)
                .findAll();
    }

    /***********************************************************************************************
     * Servo Section
     ***********************************************************************************************/
    @Override
    public ServoData getServoData(long timeStamp) {
        return realm.where(ServoData.class)
                .equalTo("time", timeStamp)
                .findFirst();
    }

    @Override
    public void clearAllServoRecords() {
        realm.beginTransaction();
        realm.delete(ServoData.class);
        realm.commitTransaction();
    }

    @Override
    public void clearBlockOfServoRecords(long block) {
        realm.beginTransaction();
        RealmResults<ServoData> data = getBlockOfServoRecords(block);
        data.deleteAllFromRealm();
        realm.commitTransaction();
    }

    @Override
    public RealmResults<ServoData> getAllServoRecords() {
        return realm.where(ServoData.class).findAll();
    }

    @Override
    public RealmResults<ServoData> getBlockOfServoRecords(long block) {
        return realm.where(ServoData.class)
                .equalTo("block", block)
                .findAll();
    }

}
