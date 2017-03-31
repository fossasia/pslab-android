package com.viveksb007.pslab.communication;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by akarshan on 3/31/17.
 */

public class SensorList {

    Map< Integer ,String[]> sensors  = new LinkedHashMap<>();
    void SensorList()
    {
        sensors.put(0x0,new String[]{"Could be MLX90614. Try 0x5A"});
        sensors.put(0x13,new String[]{"VCNL4000"});
        sensors.put(0x3c,new String[]{"OLED SSD1306"});
        sensors.put(0x3d,new String[]{"OLED SSD1306"});
        sensors.put(0x48,new String[]{"PN532 RFID"});
        sensors.put(0x29,new String[]{"TSL2561"});
        sensors.put(0x39,new String[]{"TSL2561"});
        sensors.put(0x49,new String[]{"OLED SSD1306"});
        sensors.put(0x1D,new String[]{"ADXL345", "MMA7455L", "LSM9DSO"});
        sensors.put(0x53,new String[]{"ADXL345"});
        sensors.put(0x5A,new String[]{"MLX90614 PIR temperature"});
        sensors.put(0x1E,new String[]{"HMC5883L magnetometer","LSM303 magnetometer"});
        sensors.put(0x77,new String[]{"BMP180/GY-68 altimeter","MS5607","MS5611"});
        sensors.put(0x68,new String[]{"MPU-6050/GY-521 accel+gyro+temp","ITG3200","DS1307","DS3231"});
        sensors.put(0x69,new String[]{"ITG3200"});
        sensors.put(0x76,new String[]{"MS5607","MS5611"});
        sensors.put(0x6B,new String[]{"LSM9DSO gyro"});
        sensors.put(0x19,new String[]{"LSM303 accel"});
        sensors.put(0x20,new String[]{"MCP23008","MCP23017"});
        sensors.put(0x21,new String[]{"MCP23008","MCP23017"});
        sensors.put(0x22,new String[]{"MCP23008","MCP23017"});
        sensors.put(0x23,new String[]{"BH1750","MCP23008","MCP23017"});
        sensors.put(0x24,new String[]{"MCP23008","MCP23017"});
        sensors.put(0x25,new String[]{"MCP23008","MCP23017"});
        sensors.put(0x26,new String[]{"MCP23008","MCP23017"});
        sensors.put(0x27,new String[]{"MCP23008","MCP23017"});
        sensors.put(0x40,new String[]{"SHT21(Temp/RH)"});
        sensors.put(0x60,new String[]{"MCP4725A0 4 chan DAC (onBoard)"});
        sensors.put(0x61,new String[]{"MCP4725A0 4 chan DAC"});
        sensors.put(0x62,new String[]{"MCP4725A1 4 chan DAC"});
        sensors.put(0x63,new String[]{"MCP4725A1 4 chan DAC", "Si4713"});
        sensors.put(0x64,new String[]{"MCP4725A2 4 chan DAC"});
        sensors.put(0x65,new String[]{"MCP4725A2 4 chan DAC"});
        sensors.put(0x66,new String[]{"MCP4725A3 4 chan DAC"});
        sensors.put(0x67,new String[]{"MCP4725A3 4 chan DAC"});
        sensors.put(0x11,new String[]{"Si4713"});
        sensors.put(0x38,new String[]{"FT6206 touch controller"});
        sensors.put(0x41,new String[]{"STMPE610"});

    }

}
