package org.fossasia.pslab.communication;

import android.util.Log;

public class CommandsProto {

    private static String TAG = "CommandsProto";

    public int ACKNOWLEDGE = 254;
    public int MAX_SAMPLES = 10000;
    public int DATA_SPLITTING = 200;

    public int FLASH = 1;
    public int READ_FLASH = 1;
    public int WRITE_FLASH = 2;
    public int WRITE_BULK_FLASH = 3;
    public int READ_BULK_FLASH = 4;

    public int ADC = 2;
    public int CAPTURE_ONE = 1;
    public int CAPTURE_TWO = 2;
    public int CAPTURE_DMASPEED = 3;
    public int CAPTURE_FOUR = 4;
    public int CONFIGURE_TRIGGER = 5;
    public int GET_CAPTURE_STATUS = 6;
    public int GET_CAPTURE_CHANNEL = 7;
    public int SET_PGA_GAIN = 8;
    public int GET_VOLTAGE = 9;
    public int GET_VOLTAGE_SUMMED = 10;
    public int START_ADC_STREAMING = 11;
    public int SELECT_PGA_CHANNEL = 12;
    public int CAPTURE_12BIT = 13;
    public int CAPTURE_MULTIPLE = 14;
    public int SET_HI_CAPTURE = 15;
    public int SET_LO_CAPTURE = 16;

    public int MULTIPOINT_CAPACITANCE = 20;
    public int SET_CAP = 21;
    public int PULSE_TRAIN = 22;

    public int SPI_HEADER = 3;
    public int START_SPI = 1;
    public int SEND_SPI8 = 2;
    public int SEND_SPI16 = 3;
    public int STOP_SPI = 4;
    public int SET_SPI_PARAMETERS = 5;
    public int SEND_SPI8_BURST = 6;
    public int SEND_SPI16_BURST = 7;

    public int I2C_HEADER = 4;
    public int I2C_START = 1;
    public int I2C_SEND = 2;
    public int I2C_STOP = 3;
    public int I2C_RESTART = 4;
    public int I2C_READ_END = 5;
    public int I2C_READ_MORE = 6;
    public int I2C_WAIT = 7;
    public int I2C_SEND_BURST = 8;
    public int I2C_CONFIG = 9;
    public int I2C_STATUS = 10;
    public int I2C_READ_BULK = 11;
    public int I2C_WRITE_BULK = 12;
    public int I2C_ENABLE_SMBUS = 13;
    public int I2C_INIT = 14;
    public int I2C_PULLDOWN_SCL = 15;
    public int I2C_DISABLE_SMBUS = 16;
    public int I2C_START_SCOPE = 17;


    public int UART_2 = 5;
    public int SEND_BYTE = 1;
    public int SEND_INT = 2;
    public int SEND_ADDRESS = 3;
    public int SET_BAUD = 4;
    public int SET_MODE = 5;
    public int READ_BYTE = 6;
    public int READ_INT = 7;
    public int READ_UART2_STATUS = 8;


    public int DAC = 6;
    public int SET_DAC = 1;
    public int SET_CALIBRATED_DAC = 2;


    public int WAVEGEN = 7;
    public int SET_WG = 1;
    public int SET_SQR1 = 3;
    public int SET_SQR2 = 4;
    public int SET_SQRS = 5;
    public int TUNE_SINE_OSCILLATOR = 6;
    public int SQR4 = 7;
    public int MAP_REFERENCE = 8;
    public int SET_BOTH_WG = 9;
    public int SET_WAVEFORM_TYPE = 10;
    public int SELECT_FREQ_REGISTER = 11;
    public int DELAY_GENERATOR = 12;
    public int SET_SINE1 = 13;
    public int SET_SINE2 = 14;

    public int LOAD_WAVEFORM1 = 15;
    public int LOAD_WAVEFORM2 = 16;
    public int SQR1_PATTERN = 17;


    public int DOUT = 8;
    public int SET_STATE = 1;


    public int DIN = 9;
    public int GET_STATE = 1;
    public int GET_STATES = 2;


    public int ID1 = 0;
    public int ID2 = 1;
    public int ID3 = 2;
    public int ID4 = 3;
    public int LMETER = 4;


    public int TIMING = 10;
    public int GET_TIMING = 1;
    public int GET_PULSE_TIME = 2;
    public int GET_DUTY_CYCLE = 3;
    public int START_ONE_CHAN_LA = 4;
    public int START_TWO_CHAN_LA = 5;
    public int START_FOUR_CHAN_LA = 6;
    public int FETCH_DMA_DATA = 7;
    public int FETCH_INT_DMA_DATA = 8;
    public int FETCH_LONG_DMA_DATA = 9;
    public int GET_LA_PROGRESS = 10;
    public int GET_INITIAL_DIGITAL_STATES = 11;

    public int TIMING_MEASUREMENTS = 12;
    public int INTERVAL_MEASUREMENTS = 13;
    public int CONFIGURE_COMPARATOR = 14;
    public int START_ALTERNATE_ONE_CHAN_LA = 15;
    public int START_THREE_CHAN_LA = 16;
    public int STOP_LA = 17;


    public int COMMON = 11;

    public int GET_CTMU_VOLTAGE = 1;
    public int GET_CAPACITANCE = 2;
    public int GET_FREQUENCY = 3;
    public int GET_INDUCTANCE = 4;

    public int GET_VERSION = 5;

    public int RETRIEVE_BUFFER = 8;
    public int GET_HIGH_FREQUENCY = 9;
    public int CLEAR_BUFFER = 10;
    public int SET_RGB1 = 11;
    public int READ_PROGRAM_ADDRESS = 12;
    public int WRITE_PROGRAM_ADDRESS = 13;
    public int READ_DATA_ADDRESS = 14;
    public int WRITE_DATA_ADDRESS = 15;

    public int GET_CAP_RANGE = 16;
    public int SET_RGB2 = 17;
    public int READ_LOG = 18;
    public int RESTORE_STANDALONE = 19;
    public int GET_ALTERNATE_HIGH_FREQUENCY = 20;
    public int SET_RGB3 = 22;

    public int START_CTMU = 23;
    public int STOP_CTMU = 24;

    public int START_COUNTING = 25;
    public int FETCH_COUNT = 26;
    public int FILL_BUFFER = 27;


    public int SETBAUD = 12;
    public int BAUD9600 = 1;
    public int BAUD14400 = 2;
    public int BAUD19200 = 3;
    public int BAUD28800 = 4;
    public int BAUD38400 = 5;
    public int BAUD57600 = 6;
    public int BAUD115200 = 7;
    public int BAUD230400 = 8;
    public int BAUD1000000 = 9;


    public int NRFL01 = 13;
    public int NRF_SETUP = 1;
    public int NRF_RXMODE = 2;
    public int NRF_TXMODE = 3;
    public int NRF_POWER_DOWN = 4;
    public int NRF_RXCHAR = 5;
    public int NRF_TXCHAR = 6;
    public int NRF_HASDATA = 7;
    public int NRF_FLUSH = 8;
    public int NRF_WRITEREG = 9;
    public int NRF_READREG = 10;
    public int NRF_GETSTATUS = 11;
    public int NRF_WRITECOMMAND = 12;
    public int NRF_WRITEPAYLOAD = 13;
    public int NRF_READPAYLOAD = 14;
    public int NRF_WRITEADDRESS = 15;
    public int NRF_TRANSACTION = 16;
    public int NRF_START_TOKEN_MANAGER = 17;
    public int NRF_STOP_TOKEN_MANAGER = 18;
    public int NRF_TOTAL_TOKENS = 19;
    public int NRF_REPORTS = 20;
    public int NRF_WRITE_REPORT = 21;
    public int NRF_DELETE_REPORT_ROW = 22;

    public int NRF_WRITEADDRESSES = 23;


    public int NONSTANDARD_IO = 14;
    public int HX711_HEADER = 1;
    public int HCSR04_HEADER = 2;
    public int AM2302_HEADER = 3;
    public int TCD1304_HEADER = 4;
    public int STEPPER_MOTOR = 5;


    public int PASSTHROUGHS = 15;
    public int PASS_UART = 1;

    public int STOP_STREAMING = 253;

    public int EVERY_SIXTEENTH_RISING_EDGE = 0b101;
    public int EVERY_FOURTH_RISING_EDGE = 0b100;
    public int EVERY_RISING_EDGE = 0b011;
    public int EVERY_FALLING_EDGE = 0b010;
    public int EVERY_EDGE = 0b001;
    public int DISABLED = 0b000;

    public int CSA1 = 1;
    public int CSA2 = 2;
    public int CSA3 = 3;
    public int CSA4 = 4;
    public int CSA5 = 5;
    public int CS1 = 6;
    public int CS2 = 7;

    public int TEN_BIT = 10;
    public int TWELVE_BIT = 12;

    public String applySIPrefix(double value, String unit, int precision) {
        boolean negative = false;
        if (value < 0) {
            negative = true;
            value *= -1;
        } else if (value == 0)
            return "0 " + unit;
        int exponent = (int) Math.log10(value);
        if (exponent > 0) {
            exponent = (exponent / 3) * 3;
        } else {
            exponent = ((-1 * exponent + 3) / 3) * (-3);
        }
        value *= (Math.pow(10, -exponent));
        if (value >= 1000.0) {
            value /= 1000.0;
            exponent += 3;
        }
        if (negative) value *= -1;
        String PREFIXES = "yzafpnum KMGTPEZY";
        int prefixLevel = (PREFIXES.length() - 1) / 2;
        int siLevel = exponent / 3;
        if (Math.abs(siLevel) > prefixLevel) {
            Log.e(TAG, "Value Error : Exponent out range of available prefixes.");
            return "";
        } else {
            String format = "%." + precision + "f %s%s";
            return String.format(format, precision, value, PREFIXES.charAt(siLevel + prefixLevel), unit);
        }
    }

}
