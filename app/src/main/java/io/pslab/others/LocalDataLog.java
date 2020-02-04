package io.pslab.others;

import io.pslab.interfaces.sensorloggers.AccelerometerRecordables;
import io.pslab.interfaces.sensorloggers.BaroMeterRecordables;
import io.pslab.interfaces.sensorloggers.CompassRecordables;
import io.pslab.interfaces.sensorloggers.DustSensorRecordables;
import io.pslab.interfaces.sensorloggers.GasSensorRecordables;
import io.pslab.interfaces.sensorloggers.GyroscopeRecordables;
import io.pslab.interfaces.sensorloggers.LogicAnalyzerRecordables;
import io.pslab.interfaces.sensorloggers.LuxMeterRecordables;
import io.pslab.interfaces.sensorloggers.MultimeterRecordables;
import io.pslab.interfaces.sensorloggers.OscilloscopeRecordables;
import io.pslab.interfaces.sensorloggers.PowerSourceRecordables;
import io.pslab.interfaces.sensorloggers.SensorRecordables;
import io.pslab.interfaces.sensorloggers.ServoRecordables;
import io.pslab.interfaces.sensorloggers.SoundMeterRecordables;
import io.pslab.interfaces.sensorloggers.ThermometerRecordables;
import io.pslab.interfaces.sensorloggers.WaveGeneratorRecordables;
import io.pslab.models.AccelerometerData;
import io.pslab.models.BaroData;
import io.pslab.models.CompassData;
import io.pslab.models.DustSensorData;
import io.pslab.models.GasSensorData;
import io.pslab.models.GyroData;
import io.pslab.models.LogicAnalyzerData;
import io.pslab.models.LuxData;
import io.pslab.models.MultimeterData;
import io.pslab.models.OscilloscopeData;
import io.pslab.models.PowerSourceData;
import io.pslab.models.SensorDataBlock;
import io.pslab.models.ServoData;
import io.pslab.models.SoundData;
import io.pslab.models.ThermometerData;
import io.pslab.models.WaveGeneratorData;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Padmal on 11/5/18.
 */

public class LocalDataLog implements SoundMeterRecordables, DustSensorRecordables, LuxMeterRecordables, BaroMeterRecordables, SensorRecordables, CompassRecordables, AccelerometerRecordables, GyroscopeRecordables, ThermometerRecordables, ServoRecordables, WaveGeneratorRecordables, OscilloscopeRecordables, PowerSourceRecordables, MultimeterRecordables, LogicAnalyzerRecordables, GasSensorRecordables {

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

    /***********************************************************************************************
     * Wave Generator Section
     ***********************************************************************************************/
    @Override
    public WaveGeneratorData getWaveData(long timeStamp) {
        return realm.where(WaveGeneratorData.class)
                .equalTo("time", timeStamp)
                .findFirst();
    }

    @Override
    public void clearAllWaveRecords() {
        realm.beginTransaction();
        realm.delete(WaveGeneratorData.class);
        realm.commitTransaction();
    }

    @Override
    public void clearBlockOfWaveRecords(long block) {
        realm.beginTransaction();
        RealmResults<WaveGeneratorData> data = getBlockOfWaveRecords(block);
        data.deleteAllFromRealm();
        realm.commitTransaction();
    }

    @Override
    public RealmResults<WaveGeneratorData> getAllWaveRecords() {
        return realm.where(WaveGeneratorData.class).findAll();
    }

    @Override
    public RealmResults<WaveGeneratorData> getBlockOfWaveRecords(long block) {
        return realm.where(WaveGeneratorData.class)
                .equalTo("block", block)
                .findAll();
    }

    /***********************************************************************************************
     * Oscilloscope Section
     ***********************************************************************************************/

    @Override
    public OscilloscopeData getOscilloscopeData(long timeStamp) {
        return realm.where(OscilloscopeData.class)
                .equalTo("time", timeStamp)
                .findFirst();
    }

    @Override
    public void clearAllOscilloscopeRecords() {
        realm.beginTransaction();
        realm.delete(OscilloscopeData.class);
        realm.commitTransaction();
    }

    @Override
    public void clearBlockOfOscilloscopeRecords(long block) {
        realm.beginTransaction();
        RealmResults<OscilloscopeData> data = getBlockOfOscilloscopeRecords(block);
        data.deleteAllFromRealm();
        realm.commitTransaction();
    }

    @Override
    public RealmResults<OscilloscopeData> getAllOscilloscopeRecords() {
        return realm.where(OscilloscopeData.class).findAll();
    }

    @Override
    public RealmResults<OscilloscopeData> getBlockOfOscilloscopeRecords(long block) {
        return realm.where(OscilloscopeData.class)
                .equalTo("block", block)
                .findAll();
    }

    /***********************************************************************************************
     * Power Source Section
     ***********************************************************************************************/

    @Override
    public PowerSourceData getPowerData(long timeStamp) {
        return realm.where(PowerSourceData.class)
                .equalTo("time", timeStamp)
                .findFirst();
    }

    @Override
    public void clearAllPowerRecords() {
        realm.beginTransaction();
        realm.delete(PowerSourceData.class);
        realm.commitTransaction();
    }

    @Override
    public void clearBlockOfPowerRecords(long block) {
        realm.beginTransaction();
        RealmResults<PowerSourceData> data = getBlockOfPowerRecords(block);
        data.deleteAllFromRealm();
        realm.commitTransaction();
    }

    @Override
    public RealmResults<PowerSourceData> getAllPowerRecords() {
        return realm.where(PowerSourceData.class).findAll();
    }

    @Override
    public RealmResults<PowerSourceData> getBlockOfPowerRecords(long block) {
        return realm.where(PowerSourceData.class)
                .equalTo("block", block)
                .findAll();
    }

    /***********************************************************************************************
     * Multimeter Section
     ***********************************************************************************************/

    @Override
    public MultimeterData getMultimeterData(long timeStamp) {
        return realm.where(MultimeterData.class)
                .equalTo("time", timeStamp)
                .findFirst();
    }

    @Override
    public void clearAllMultimeterRecords() {
        realm.beginTransaction();
        realm.delete(MultimeterData.class);
        realm.commitTransaction();
    }

    @Override
    public void clearBlockOfMultimeterRecords(long block) {
        realm.beginTransaction();
        RealmResults<MultimeterData> data = getBlockOfMultimeterRecords(block);
        data.deleteAllFromRealm();
        realm.commitTransaction();
    }

    @Override
    public RealmResults<MultimeterData> getAllMultimeterRecords() {
        return realm.where(MultimeterData.class).findAll();
    }

    @Override
    public RealmResults<MultimeterData> getBlockOfMultimeterRecords(long block) {
        return realm.where(MultimeterData.class)
                .equalTo("block", block)
                .findAll();
    }

    /***********************************************************************************************
     * Logic Analyzer Section
     ***********************************************************************************************/

    @Override
    public LogicAnalyzerData getLAData(long timeStamp) {
        return realm.where(LogicAnalyzerData.class)
                .equalTo("time", timeStamp)
                .findFirst();
    }

    @Override
    public void clearAllLARecords() {
        realm.beginTransaction();
        realm.delete(LogicAnalyzerData.class);
        realm.commitTransaction();
    }

    @Override
    public void clearBlockOfLARecords(long block) {
        realm.beginTransaction();
        RealmResults<LogicAnalyzerData> data = getBlockOfLARecords(block);
        data.deleteAllFromRealm();
        realm.commitTransaction();
    }

    @Override
    public RealmResults<LogicAnalyzerData> getAllLARecords() {
        return realm.where(LogicAnalyzerData.class).findAll();
    }

    @Override
    public RealmResults<LogicAnalyzerData> getBlockOfLARecords(long block) {
        return realm.where(LogicAnalyzerData.class)
                .equalTo("block", block)
                .findAll();
    }

    /***********************************************************************************************
     * Gas Sensor Section
     ***********************************************************************************************/

    @Override
    public GasSensorData getGasSensorData(long timeStamp) {
        return realm.where(GasSensorData.class)
                .equalTo("time", timeStamp)
                .findFirst();
    }

    @Override
    public void clearAllGasSensorRecords() {
        realm.beginTransaction();
        realm.delete(GasSensorData.class);
        realm.commitTransaction();
    }

    @Override
    public void clearBlockOfGasSensorRecords(long block) {
        realm.beginTransaction();
        RealmResults<GasSensorData> data = getBlockOfGasSensorRecords(block);
        data.deleteAllFromRealm();
        realm.commitTransaction();
    }

    @Override
    public RealmResults<GasSensorData> getAllGasSensorRecords() {
        return realm.where(GasSensorData.class).findAll();
    }

    @Override
    public RealmResults<GasSensorData> getBlockOfGasSensorRecords(long block) {
        return realm.where(GasSensorData.class)
                .equalTo("block", block)
                .findAll();
    }

    /***********************************************************************************************
     * Dust Sensor Section
     ***********************************************************************************************/
    @Override
    public DustSensorData getDustSensorData(long timestamp) {
        return realm.where(DustSensorData.class).equalTo("time", timestamp).findFirst();
    }

    @Override
    public void clearAllDustSensorRecords() {
        realm.beginTransaction();
        realm.delete(LuxData.class);
        realm.commitTransaction();
    }

    @Override
    public void clearBlockOfDustSensorRecords(long block) {
        realm.beginTransaction();
        RealmResults<DustSensorData> data = getBlockOfDustSensorRecords(block);
        data.deleteAllFromRealm();
        realm.commitTransaction();
    }

    @Override
    public RealmResults<DustSensorData> getAllDustSensorRecords() {
        return realm.where(DustSensorData.class).findAll();
    }

    @Override
    public RealmResults<DustSensorData> getBlockOfDustSensorRecords(long block) {
        return realm.where(DustSensorData.class)
                .equalTo("block", block)
                .findAll();
    }


    /***********************************************************************************************
     * Sound Meter Section
     ***********************************************************************************************/
    @Override
    public SoundData getSoundMeterData(long timeStamp) {
        return realm.where(SoundData.class).equalTo("time",timeStamp).findFirst();
    }

    @Override
    public void clearAllSoundRecords() {
        realm.beginTransaction();
        realm.delete(SoundData.class);
        realm.commitTransaction();
    }

    @Override
    public void clearBlockOfSoundRecords(long block) {
        realm.beginTransaction();
        RealmResults<SoundData> data = getBlockOfSoundRecords(block);
        data.deleteAllFromRealm();
        realm.commitTransaction();
    }

    @Override
    public RealmResults<SoundData> getAllSoundRecords() {
        return realm.where(SoundData.class).findAll();
    }

    @Override
    public RealmResults<SoundData> getBlockOfSoundRecords(long block) {
        return realm.where(SoundData.class).equalTo("block",block)
                .findAll();
    }
}
