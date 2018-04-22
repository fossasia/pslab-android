package org.fossasia.pslab.communication.sensors;

import org.fossasia.pslab.communication.peripherals.I2C;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.abs;

/**
 * Created by akarshan on 4/20/17.
 * <p>
 * //  functions that are needed to be handled in ScienceLab.java
 * load("logo");
 * scroll("topright");
 * TimeUnit.MILLISECONDS.sleep(2800);
 * scroll("stop");
 * <p>
 * ScienceLab instance of i2c needs to be passed to SSD1306 class constructor
 */

public class SSD1306 {
    private int ADDRESS = 0x3C;
    private ArrayList<String> load = new ArrayList<>(Collections.singletonList("logo"));
    private ArrayList<String> scroll = new ArrayList<>(Arrays.asList("left", "right, ", "topright", "topleft", "bottomleft", "bottomright", "stop"));
    public int NUMPLOTS = 0;
    public String[] PLOTNAMES = {""};
    public String name = "OLED Display";

    private int width = 128;
    private int height = 64;

    private int rotation = 0;
    private int cursorY = 0;
    private int cursorX = 0;
    private int textSize = 1;
    private int textColor = 1;
    private int textbgColor = 0;
    private boolean wrap = true;

    private int SSD1306_128_64 = 1;
    private int SSD1306_128_32 = 2;
    private int SSD1306_96_16 = 3;

    private int DISPLAY_TYPE = SSD1306_96_16;

    private int SSD1306_LCDWIDTH = 128;
    private int SSD1306_LCDHEIGHT = 64;

    private int SSD1306_SETCONTRAST = 0x81;
    private int SSD1306_DISPLAYALLON_RESUME = 0xA4;
    private int SSD1306_DISPLAYALLON = 0xA5;
    private int SSD1306_NORMALDISPLAY = 0xA6;
    private int SSD1306_INVERTDISPLAY = 0xA7;
    private int SSD1306_DISPLAYOFF = 0xAE;
    private int SSD1306_DISPLAYON = 0xAF;

    private int SSD1306_SETDISPLAYOFFSET = 0xD3;
    private int SSD1306_SETCOMPINS = 0xDA;

    private int SSD1306_SETVCOMDETECT = 0xDB;

    private int SSD1306_SETDISPLAYCLOCKDIV = 0xD5;
    private int SSD1306_SETPRECHARGE = 0xD9;

    private int SSD1306_SETMULTIPLEX = 0xA8;

    private int SSD1306_SETLOWCOLUMN = 0x00;
    private int SSD1306_SETHIGHCOLUMN = 0x10;

    private int SSD1306_SETSTARTLINE = 0x40;

    private int SSD1306_MEMORYMODE = 0x20;

    private int SSD1306_COMSCANINC = 0xC0;
    private int SSD1306_COMSCANDEC = 0xC8;

    private int SSD1306_SEGREMAP = 0xA0;

    private int SSD1306_CHARGEPUMP = 0x8D;

    private int SSD1306_EXTERNALVCC = 0x1;
    private int SSD1306_SWITCHCAPVCC = 0x2;

    private int[] logobuff = {255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 127, 127, 63, 63, 159, 159, 223, 223, 207, 207, 207, 239, 239, 47, 47, 39, 39, 7, 7, 67, 67, 83, 131, 135, 7, 7, 15, 15, 31, 191, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 63, 31, 15, 199, 99, 17, 25, 12, 4, 2, 3, 7, 63, 255, 255, 255, 255, 255, 255, 255, 255, 254, 252, 240, 224, 224, 224, 192, 192, 128, 128, 128, 128, 129, 128, 0, 0, 0, 0, 0, 3, 3, 7, 31, 127, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 127, 15, 3, 192, 120, 134, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 254, 254, 254, 252, 252, 249, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 143, 0, 0, 124, 199, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 128, 240, 252, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 240, 128, 0, 7, 56, 96, 128, 0, 0, 0, 0, 0, 0, 0, 12, 63, 255, 127, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 31, 7, 227, 243, 249, 249, 249, 249, 249, 249, 243, 255, 255, 199, 131, 49, 57, 57, 57, 121, 115, 255, 255, 255, 255, 15, 15, 159, 207, 207, 207, 143, 31, 63, 255, 255, 159, 207, 207, 207, 143, 31, 63, 255, 255, 255, 15, 15, 159, 207, 207, 207, 255, 255, 0, 0, 255, 127, 63, 159, 207, 239, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 254, 248, 240, 224, 129, 2, 4, 8, 16, 32, 96, 64, 128, 128, 135, 30, 115, 207, 159, 255, 255, 255, 255, 127, 63, 31, 31, 31, 31, 31, 31, 31, 7, 7, 7, 127, 127, 127, 127, 127, 127, 255, 255, 255, 255, 252, 240, 227, 231, 207, 207, 207, 207, 207, 207, 231, 255, 255, 231, 207, 207, 207, 207, 207, 198, 224, 240, 255, 255, 255, 0, 0, 231, 207, 207, 207, 199, 224, 240, 255, 225, 193, 204, 204, 204, 228, 192, 192, 255, 255, 255, 192, 192, 255, 255, 255, 255, 255, 255, 192, 192, 252, 248, 243, 231, 207, 223, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 254, 252, 248, 248, 240, 240, 224, 225, 225, 193, 193, 195, 195, 195, 195, 195, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 62, 62, 62, 62, 62, 62, 255, 243, 3, 3, 51, 51, 51, 19, 135, 239, 255, 255, 63, 63, 159, 159, 159, 159, 63, 127, 255, 255, 255, 63, 31, 159, 159, 159, 31, 252, 252, 255, 63, 63, 159, 159, 159, 159, 63, 127, 255, 255, 255, 223, 159, 159, 159, 31, 127, 255, 255, 255, 255, 223, 31, 31, 191, 159, 159, 159, 255, 255, 127, 63, 159, 159, 159, 159, 31, 31, 255, 255, 247, 3, 7, 159, 159, 159, 31, 127, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 254, 252, 252, 252, 252, 252, 252, 252, 252, 224, 224, 224, 255, 255, 255, 255, 255, 255, 255, 243, 240, 240, 247, 255, 254, 252, 248, 243, 255, 255, 248, 248, 242, 242, 242, 242, 242, 250, 255, 255, 255, 241, 242, 242, 242, 242, 248, 253, 255, 255, 248, 248, 242, 242, 242, 242, 242, 250, 255, 255, 249, 240, 242, 242, 242, 240, 240, 255, 255, 255, 255, 243, 240, 240, 243, 243, 255, 255, 255, 255, 252, 248, 243, 243, 243, 243, 243, 255, 255, 255, 247, 240, 240, 247, 255, 247, 240, 240, 247, 255};
    private int[] font = {0x00, 0x00, 0x00, 0x00, 0x00, 0x3E, 0x5B, 0x4F, 0x5B, 0x3E, 0x3E, 0x6B, 0x4F, 0x6B, 0x3E,
            0x1C, 0x3E, 0x7C, 0x3E, 0x1C, 0x18, 0x3C, 0x7E, 0x3C, 0x18, 0x1C, 0x57, 0x7D, 0x57, 0x1C,
            0x1C, 0x5E, 0x7F, 0x5E, 0x1C, 0x00, 0x18, 0x3C, 0x18, 0x00, 0xFF, 0xE7, 0xC3, 0xE7, 0xFF,
            0x00, 0x18, 0x24, 0x18, 0x00, 0xFF, 0xE7, 0xDB, 0xE7, 0xFF, 0x30, 0x48, 0x3A, 0x06, 0x0E,
            0x26, 0x29, 0x79, 0x29, 0x26, 0x40, 0x7F, 0x05, 0x05, 0x07, 0x40, 0x7F, 0x05, 0x25, 0x3F,
            0x5A, 0x3C, 0xE7, 0x3C, 0x5A, 0x7F, 0x3E, 0x1C, 0x1C, 0x08, 0x08, 0x1C, 0x1C, 0x3E, 0x7F,
            0x14, 0x22, 0x7F, 0x22, 0x14, 0x5F, 0x5F, 0x00, 0x5F, 0x5F, 0x06, 0x09, 0x7F, 0x01, 0x7F,
            0x00, 0x66, 0x89, 0x95, 0x6A, 0x60, 0x60, 0x60, 0x60, 0x60, 0x94, 0xA2, 0xFF, 0xA2, 0x94,
            0x08, 0x04, 0x7E, 0x04, 0x08, 0x10, 0x20, 0x7E, 0x20, 0x10, 0x08, 0x08, 0x2A, 0x1C, 0x08,
            0x08, 0x1C, 0x2A, 0x08, 0x08, 0x1E, 0x10, 0x10, 0x10, 0x10, 0x0C, 0x1E, 0x0C, 0x1E, 0x0C,
            0x30, 0x38, 0x3E, 0x38, 0x30, 0x06, 0x0E, 0x3E, 0x0E, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x5F, 0x00, 0x00, 0x00, 0x07, 0x00, 0x07, 0x00, 0x14, 0x7F, 0x14, 0x7F, 0x14,
            0x24, 0x2A, 0x7F, 0x2A, 0x12, 0x23, 0x13, 0x08, 0x64, 0x62, 0x36, 0x49, 0x56, 0x20, 0x50,
            0x00, 0x08, 0x07, 0x03, 0x00, 0x00, 0x1C, 0x22, 0x41, 0x00, 0x00, 0x41, 0x22, 0x1C, 0x00,
            0x2A, 0x1C, 0x7F, 0x1C, 0x2A, 0x08, 0x08, 0x3E, 0x08, 0x08, 0x00, 0x80, 0x70, 0x30, 0x00,
            0x08, 0x08, 0x08, 0x08, 0x08, 0x00, 0x00, 0x60, 0x60, 0x00, 0x20, 0x10, 0x08, 0x04, 0x02,
            0x3E, 0x51, 0x49, 0x45, 0x3E, 0x00, 0x42, 0x7F, 0x40, 0x00, 0x72, 0x49, 0x49, 0x49, 0x46,
            0x21, 0x41, 0x49, 0x4D, 0x33, 0x18, 0x14, 0x12, 0x7F, 0x10, 0x27, 0x45, 0x45, 0x45, 0x39,
            0x3C, 0x4A, 0x49, 0x49, 0x31, 0x41, 0x21, 0x11, 0x09, 0x07, 0x36, 0x49, 0x49, 0x49, 0x36,
            0x46, 0x49, 0x49, 0x29, 0x1E, 0x00, 0x00, 0x14, 0x00, 0x00, 0x00, 0x40, 0x34, 0x00, 0x00,
            0x00, 0x08, 0x14, 0x22, 0x41, 0x14, 0x14, 0x14, 0x14, 0x14, 0x00, 0x41, 0x22, 0x14, 0x08,
            0x02, 0x01, 0x59, 0x09, 0x06, 0x3E, 0x41, 0x5D, 0x59, 0x4E, 0x7C, 0x12, 0x11, 0x12, 0x7C,
            0x7F, 0x49, 0x49, 0x49, 0x36, 0x3E, 0x41, 0x41, 0x41, 0x22, 0x7F, 0x41, 0x41, 0x41, 0x3E,
            0x7F, 0x49, 0x49, 0x49, 0x41, 0x7F, 0x09, 0x09, 0x09, 0x01, 0x3E, 0x41, 0x41, 0x51, 0x73,
            0x7F, 0x08, 0x08, 0x08, 0x7F, 0x00, 0x41, 0x7F, 0x41, 0x00, 0x20, 0x40, 0x41, 0x3F, 0x01,
            0x7F, 0x08, 0x14, 0x22, 0x41, 0x7F, 0x40, 0x40, 0x40, 0x40, 0x7F, 0x02, 0x1C, 0x02, 0x7F,
            0x7F, 0x04, 0x08, 0x10, 0x7F, 0x3E, 0x41, 0x41, 0x41, 0x3E, 0x7F, 0x09, 0x09, 0x09, 0x06,
            0x3E, 0x41, 0x51, 0x21, 0x5E, 0x7F, 0x09, 0x19, 0x29, 0x46, 0x26, 0x49, 0x49, 0x49, 0x32,
            0x03, 0x01, 0x7F, 0x01, 0x03, 0x3F, 0x40, 0x40, 0x40, 0x3F, 0x1F, 0x20, 0x40, 0x20, 0x1F,
            0x3F, 0x40, 0x38, 0x40, 0x3F, 0x63, 0x14, 0x08, 0x14, 0x63, 0x03, 0x04, 0x78, 0x04, 0x03,
            0x61, 0x59, 0x49, 0x4D, 0x43, 0x00, 0x7F, 0x41, 0x41, 0x41, 0x02, 0x04, 0x08, 0x10, 0x20,
            0x00, 0x41, 0x41, 0x41, 0x7F, 0x04, 0x02, 0x01, 0x02, 0x04, 0x40, 0x40, 0x40, 0x40, 0x40,
            0x00, 0x03, 0x07, 0x08, 0x00, 0x20, 0x54, 0x54, 0x78, 0x40, 0x7F, 0x28, 0x44, 0x44, 0x38,
            0x38, 0x44, 0x44, 0x44, 0x28, 0x38, 0x44, 0x44, 0x28, 0x7F, 0x38, 0x54, 0x54, 0x54, 0x18,
            0x00, 0x08, 0x7E, 0x09, 0x02, 0x18, 0xA4, 0xA4, 0x9C, 0x78, 0x7F, 0x08, 0x04, 0x04, 0x78,
            0x00, 0x44, 0x7D, 0x40, 0x00, 0x20, 0x40, 0x40, 0x3D, 0x00, 0x7F, 0x10, 0x28, 0x44, 0x00,
            0x00, 0x41, 0x7F, 0x40, 0x00, 0x7C, 0x04, 0x78, 0x04, 0x78, 0x7C, 0x08, 0x04, 0x04, 0x78,
            0x38, 0x44, 0x44, 0x44, 0x38, 0xFC, 0x18, 0x24, 0x24, 0x18, 0x18, 0x24, 0x24, 0x18, 0xFC,
            0x7C, 0x08, 0x04, 0x04, 0x08, 0x48, 0x54, 0x54, 0x54, 0x24, 0x04, 0x04, 0x3F, 0x44, 0x24,
            0x3C, 0x40, 0x40, 0x20, 0x7C, 0x1C, 0x20, 0x40, 0x20, 0x1C, 0x3C, 0x40, 0x30, 0x40, 0x3C,
            0x44, 0x28, 0x10, 0x28, 0x44, 0x4C, 0x90, 0x90, 0x90, 0x7C, 0x44, 0x64, 0x54, 0x4C, 0x44,
            0x00, 0x08, 0x36, 0x41, 0x00, 0x00, 0x00, 0x77, 0x00, 0x00, 0x00, 0x41, 0x36, 0x08, 0x00,
            0x02, 0x01, 0x02, 0x04, 0x02, 0x3C, 0x26, 0x23, 0x26, 0x3C, 0x1E, 0xA1, 0xA1, 0x61, 0x12,
            0x3A, 0x40, 0x40, 0x20, 0x7A, 0x38, 0x54, 0x54, 0x55, 0x59, 0x21, 0x55, 0x55, 0x79, 0x41,
            0x21, 0x54, 0x54, 0x78, 0x41, 0x21, 0x55, 0x54, 0x78, 0x40, 0x20, 0x54, 0x55, 0x79, 0x40,
            0x0C, 0x1E, 0x52, 0x72, 0x12, 0x39, 0x55, 0x55, 0x55, 0x59, 0x39, 0x54, 0x54, 0x54, 0x59,
            0x39, 0x55, 0x54, 0x54, 0x58, 0x00, 0x00, 0x45, 0x7C, 0x41, 0x00, 0x02, 0x45, 0x7D, 0x42,
            0x00, 0x01, 0x45, 0x7C, 0x40, 0xF0, 0x29, 0x24, 0x29, 0xF0, 0xF0, 0x28, 0x25, 0x28, 0xF0,
            0x7C, 0x54, 0x55, 0x45, 0x00, 0x20, 0x54, 0x54, 0x7C, 0x54, 0x7C, 0x0A, 0x09, 0x7F, 0x49,
            0x32, 0x49, 0x49, 0x49, 0x32, 0x32, 0x48, 0x48, 0x48, 0x32, 0x32, 0x4A, 0x48, 0x48, 0x30,
            0x3A, 0x41, 0x41, 0x21, 0x7A, 0x3A, 0x42, 0x40, 0x20, 0x78, 0x00, 0x9D, 0xA0, 0xA0, 0x7D,
            0x39, 0x44, 0x44, 0x44, 0x39, 0x3D, 0x40, 0x40, 0x40, 0x3D, 0x3C, 0x24, 0xFF, 0x24, 0x24,
            0x48, 0x7E, 0x49, 0x43, 0x66, 0x2B, 0x2F, 0xFC, 0x2F, 0x2B, 0xFF, 0x09, 0x29, 0xF6, 0x20,
            0xC0, 0x88, 0x7E, 0x09, 0x03, 0x20, 0x54, 0x54, 0x79, 0x41, 0x00, 0x00, 0x44, 0x7D, 0x41,
            0x30, 0x48, 0x48, 0x4A, 0x32, 0x38, 0x40, 0x40, 0x22, 0x7A, 0x00, 0x7A, 0x0A, 0x0A, 0x72,
            0x7D, 0x0D, 0x19, 0x31, 0x7D, 0x26, 0x29, 0x29, 0x2F, 0x28, 0x26, 0x29, 0x29, 0x29, 0x26,
            0x30, 0x48, 0x4D, 0x40, 0x20, 0x38, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x38,
            0x2F, 0x10, 0xC8, 0xAC, 0xBA, 0x2F, 0x10, 0x28, 0x34, 0xFA, 0x00, 0x00, 0x7B, 0x00, 0x00,
            0x08, 0x14, 0x2A, 0x14, 0x22, 0x22, 0x14, 0x2A, 0x14, 0x08, 0xAA, 0x00, 0x55, 0x00, 0xAA,
            0xAA, 0x55, 0xAA, 0x55, 0xAA, 0x00, 0x00, 0x00, 0xFF, 0x00, 0x10, 0x10, 0x10, 0xFF, 0x00,
            0x14, 0x14, 0x14, 0xFF, 0x00, 0x10, 0x10, 0xFF, 0x00, 0xFF, 0x10, 0x10, 0xF0, 0x10, 0xF0,
            0x14, 0x14, 0x14, 0xFC, 0x00, 0x14, 0x14, 0xF7, 0x00, 0xFF, 0x00, 0x00, 0xFF, 0x00, 0xFF,
            0x14, 0x14, 0xF4, 0x04, 0xFC, 0x14, 0x14, 0x17, 0x10, 0x1F, 0x10, 0x10, 0x1F, 0x10, 0x1F,
            0x14, 0x14, 0x14, 0x1F, 0x00, 0x10, 0x10, 0x10, 0xF0, 0x00, 0x00, 0x00, 0x00, 0x1F, 0x10,
            0x10, 0x10, 0x10, 0x1F, 0x10, 0x10, 0x10, 0x10, 0xF0, 0x10, 0x00, 0x00, 0x00, 0xFF, 0x10,
            0x10, 0x10, 0x10, 0x10, 0x10, 0x10, 0x10, 0x10, 0xFF, 0x10, 0x00, 0x00, 0x00, 0xFF, 0x14,
            0x00, 0x00, 0xFF, 0x00, 0xFF, 0x00, 0x00, 0x1F, 0x10, 0x17, 0x00, 0x00, 0xFC, 0x04, 0xF4,
            0x14, 0x14, 0x17, 0x10, 0x17, 0x14, 0x14, 0xF4, 0x04, 0xF4, 0x00, 0x00, 0xFF, 0x00, 0xF7,
            0x14, 0x14, 0x14, 0x14, 0x14, 0x14, 0x14, 0xF7, 0x00, 0xF7, 0x14, 0x14, 0x14, 0x17, 0x14,
            0x10, 0x10, 0x1F, 0x10, 0x1F, 0x14, 0x14, 0x14, 0xF4, 0x14, 0x10, 0x10, 0xF0, 0x10, 0xF0,
            0x00, 0x00, 0x1F, 0x10, 0x1F, 0x00, 0x00, 0x00, 0x1F, 0x14, 0x00, 0x00, 0x00, 0xFC, 0x14,
            0x00, 0x00, 0xF0, 0x10, 0xF0, 0x10, 0x10, 0xFF, 0x10, 0xFF, 0x14, 0x14, 0x14, 0xFF, 0x14,
            0x10, 0x10, 0x10, 0x1F, 0x00, 0x00, 0x00, 0x00, 0xF0, 0x10, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
            0xF0, 0xF0, 0xF0, 0xF0, 0xF0, 0xFF, 0xFF, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF,
            0x0F, 0x0F, 0x0F, 0x0F, 0x0F, 0x38, 0x44, 0x44, 0x38, 0x44, 0x7C, 0x2A, 0x2A, 0x3E, 0x14,
            0x7E, 0x02, 0x02, 0x06, 0x06, 0x02, 0x7E, 0x02, 0x7E, 0x02, 0x63, 0x55, 0x49, 0x41, 0x63,
            0x38, 0x44, 0x44, 0x3C, 0x04, 0x40, 0x7E, 0x20, 0x1E, 0x20, 0x06, 0x02, 0x7E, 0x02, 0x02,
            0x99, 0xA5, 0xE7, 0xA5, 0x99, 0x1C, 0x2A, 0x49, 0x2A, 0x1C, 0x4C, 0x72, 0x01, 0x72, 0x4C,
            0x30, 0x4A, 0x4D, 0x4D, 0x30, 0x30, 0x48, 0x78, 0x48, 0x30, 0xBC, 0x62, 0x5A, 0x46, 0x3D,
            0x3E, 0x49, 0x49, 0x49, 0x00, 0x7E, 0x01, 0x01, 0x01, 0x7E, 0x2A, 0x2A, 0x2A, 0x2A, 0x2A,
            0x44, 0x44, 0x5F, 0x44, 0x44, 0x40, 0x51, 0x4A, 0x44, 0x40, 0x40, 0x44, 0x4A, 0x51, 0x40,
            0x00, 0x00, 0xFF, 0x01, 0x03, 0xE0, 0x80, 0xFF, 0x00, 0x00, 0x08, 0x08, 0x6B, 0x6B, 0x08,
            0x36, 0x12, 0x36, 0x24, 0x36, 0x06, 0x0F, 0x09, 0x0F, 0x06, 0x00, 0x00, 0x18, 0x18, 0x00,
            0x00, 0x00, 0x10, 0x10, 0x00, 0x30, 0x40, 0xFF, 0x01, 0x01, 0x00, 0x1F, 0x01, 0x01, 0x1E,
            0x00, 0x19, 0x1D, 0x17, 0x12, 0x00, 0x3C, 0x3C, 0x3C, 0x3C, 0x00, 0x00, 0x00, 0x00, 0x00};  //ascii fonts

    private int[] buff;
    private I2C i2c;

    public SSD1306(I2C i2c) throws IOException, InterruptedException {

        this.i2c = i2c;

        buff = new int[1024];
        Arrays.fill(buff, 0);
        SSD1306_command(SSD1306_DISPLAYOFF);                    //0xAE
        SSD1306_command(SSD1306_SETDISPLAYCLOCKDIV);            // 0xD5
        SSD1306_command(0x80);                                  // the suggested ratio 0x80
        SSD1306_command(SSD1306_SETMULTIPLEX);                  // 0xA8
        SSD1306_command(0x3F);
        SSD1306_command(SSD1306_SETDISPLAYOFFSET);              // 0xD3
        SSD1306_command(0x0);                                   // no offset
        SSD1306_command(SSD1306_SETSTARTLINE | 0x0);            // line //0
        SSD1306_command(SSD1306_CHARGEPUMP);                    // 0x8D
        SSD1306_command(0x14);                                    //vccstate = SSD1306_SWITCHCAPVCC;
        SSD1306_command(SSD1306_MEMORYMODE);                    // 0x20
        SSD1306_command(0x00);                                  // 0x0 act like ks0108
        SSD1306_command(SSD1306_SEGREMAP | 0x1);
        SSD1306_command(SSD1306_COMSCANDEC);
        SSD1306_command(SSD1306_SETCOMPINS);                    // 0xDA
        SSD1306_command(0x12);
        SSD1306_command(SSD1306_SETCONTRAST);                   // 0x81
        SSD1306_command(0xFF);                                    //	vccstate = SSD1306_SWITCHCAPVCC;
        SSD1306_command(SSD1306_SETPRECHARGE);                  // 0xd9
        SSD1306_command(0xF1);                                    //	vccstate = SSD1306_SWITCHCAPVCC;
        SSD1306_command(SSD1306_SETVCOMDETECT);                 // 0xDB
        SSD1306_command(0x40);
        SSD1306_command(SSD1306_DISPLAYALLON_RESUME);           // 0xA4
        SSD1306_command(SSD1306_NORMALDISPLAY);                 // 0xA6
        SSD1306_command(SSD1306_DISPLAYON);                     //--turn on oled panel

    }

    public void load(String arg) throws IOException {
        scroll("stop");
        if (arg.equals("logo")) {
            clearDisplay();
            System.arraycopy(logobuff, 0, buff, 0, 1024);
            displayOLED();
        }
    }

    private void SSD1306_command(int cmd) throws IOException {
        i2c.writeBulk(ADDRESS, new int[]{0x00, cmd});
    }

    public void SSD1306_data(int data) throws IOException {
        i2c.writeBulk(ADDRESS, new int[]{0x40, data});
    }

    private void clearDisplay() {
        setCursor(0, 0);
        for (int a = 0; a < SSD1306_LCDWIDTH * SSD1306_LCDHEIGHT / 8; a++)
            buff[a] = 0;
    }

    private void displayOLED() throws IOException {
        SSD1306_command(SSD1306_SETLOWCOLUMN | 0x00);
        SSD1306_command(SSD1306_SETHIGHCOLUMN | 0x00);
        SSD1306_command(SSD1306_SETSTARTLINE | 0x00);
        int a = 0;
        while (a < SSD1306_LCDWIDTH * SSD1306_LCDHEIGHT / 8) {
            i2c.writeBulk(ADDRESS, merge(new int[]{0x40}, Arrays.copyOfRange(buff, a, a + 16)));
            a += 16;
        }
    }

    private int[] merge(int[] arr1, int[] arr2) {
        int[] mergedIntegerArray = new int[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, mergedIntegerArray, 0, arr1.length);
        System.arraycopy(arr2, 0, mergedIntegerArray, arr1.length, arr2.length);
        return mergedIntegerArray;
    }

    public void setContrast(int contrast) throws IOException {
        SSD1306_command(SSD1306_SETCONTRAST);
        SSD1306_command(contrast);
    }

    private void drawPixel(int x, int y, int color) {
        if (color == 1)
            buff[(x + (y / 8) * SSD1306_LCDWIDTH)] |= (1 << (y % 8));
        else
            buff[x + (y / 8) * SSD1306_LCDWIDTH] &= ~(1 << (y % 8));
    }

    public void drawCircle(int x0, int y0, int r, int color) {
        int f = 1 - r;
        int ddF_x = 1;
        int ddF_y = -2 * r;
        int x = 0;
        int y = r;
        drawPixel(x0, y0 + r, color);
        drawPixel(x0, y0 - r, color);
        drawPixel(x0 + r, y0, color);
        drawPixel(x0 - r, y0, color);
        while (x < y) {
            if (f >= 0) {
                y -= 1;
                ddF_y += 2;
                f += ddF_y;
            }
            x += 1;
            ddF_x += 2;
            f += ddF_x;
            drawPixel(x0 + x, y0 + y, color);
            drawPixel(x0 - x, y0 + y, color);
            drawPixel(x0 + x, y0 - y, color);
            drawPixel(x0 - x, y0 - y, color);
            drawPixel(x0 + y, y0 + x, color);
            drawPixel(x0 - y, y0 + x, color);
            drawPixel(x0 + y, y0 - x, color);
            drawPixel(x0 - y, y0 - x, color);
        }
    }

    private void drawLine(int x0, int y0, int x1, int y1, int color) {
        boolean steep = abs(y1 - y0) > abs(x1 - x0);
        int tmp, ystep, dx, dy, err;
        if (steep) {
            tmp = y0;
            y0 = x0;
            x0 = tmp;
            tmp = y1;
            y1 = x1;
            x1 = tmp;
        }
        if (x0 > x1) {
            tmp = x1;
            x1 = x0;
            x0 = tmp;
            tmp = y1;
            y1 = y0;
            y0 = tmp;
        }
        dx = x1 - x0;
        dy = abs(y1 - y0);
        err = dx / 2;

        if (y0 < y1)
            ystep = 1;
        else
            ystep = -1;

        while (x0 <= x1) {
            if (steep)
                drawPixel(y0, x0, color);
            else
                drawPixel(x0, y0, color);
            err -= dy;
            if (err < 0) {
                y0 += ystep;
                err += dx;
            }
            x0 += 1;
        }
    }

    public void drawRect(int x, int y, int w, int h, int color) {
        drawFastHLine(x, y, w, color);
        drawFastHLine(x, y + h - 1, w, color);
        drawFastVLine(x, y, h, color);
        drawFastVLine(x + w + 1, y, h, color);
    }

    private void drawFastVLine(int x, int y, int h, int color) {
        drawLine(x, y, x, y + h - 1, color);
    }

    private void drawFastHLine(int x, int y, int w, int color) {
        drawLine(x, y, x + w - 1, y, color);
    }

    private void fillRect(int x, int y, int w, int h, int color) {
        for (int i = x; i < x + w; i++)
            drawFastVLine(i, y, h, color);
    }

    public void writeString(String string) {
        for (int i = 0; i < string.length(); i++) {
            writeChar((int) string.charAt(i));
        }
    }

    private void writeChar(int c) {
        if (c == '\n') {
            cursorY += textSize * 8;
            cursorX = 0;
        } else if (c == '\r') {
        } else {
            drawChar(cursorX, cursorY, c, textColor, textbgColor, textSize);
            cursorX += textSize * 6;
            if (wrap & (cursorX > (width - textSize * 6))) {
                cursorY += textSize * 8;
                cursorX = 0;
            }
        }
    }

    private void drawChar(int x, int y, int c, int color, int bg, int size) {
        int line;
        if ((x >= width) | (y >= height) | ((x + 5 * size - 1) < 0) | ((y + 8 * size - 1) < 0))
            return;
        for (int i = 0; i < 6; i++) {
            if (i == 5)
                line = 0x0;
            else
                line = font[c * 5 + i];
            for (int j = 0; j < 8; j++) {
                if ((line & 0x1) > 0) {
                    if (size == 1)
                        drawPixel(x + i, y + j, color);
                    else
                        fillRect(x + (i * size), y + (j * size), size, size, color);
                } else if (bg != color) {
                    if (size == 1)
                        drawPixel(x + i, y + j, bg);
                    else
                        fillRect(x + i * size, y + j * size, size, size, bg);
                }
                line >>= 1;
            }
        }
    }

    private void setCursor(int x, int y) {
        cursorX = x;
        cursorY = y;
    }

    public void setTextSize(int size) {
        if (size > 0)
            textSize = size;
        else
            textSize = 1;
    }

    public void setTextColor(int color, int backgroundcolor) {
        textColor = color;
        textbgColor = backgroundcolor;
    }

    public void setTextWrap(boolean w) {
        wrap = w;
    }

    public void scroll(String arg) throws IOException {
        if (arg.equals("left"))
            SSD1306_command(0x27);          //up-0x29 ,2A left-0x27 right0x26
        if (arg.equals("right"))
            SSD1306_command(0x26);          //up-0x29 ,2A left-0x27 right0x26
        if (arg.equals("topright") | arg.equals("bottomright"))
            SSD1306_command(0x29);          //up-0x29 ,2A left-0x27 right0x26
        if (arg.equals("topleft") | arg.equals("bottomleft"))
            SSD1306_command(0x2A);          //up-0x29 ,2A left-0x27 right0x26
        if (new ArrayList<String>(Arrays.asList("left", "right", "topright", "topleft", "bottomleft", "bottomright")).contains(arg)) {
            SSD1306_command(0x00);          //dummy
            SSD1306_command(0x0);           //start page
            SSD1306_command(0x7);           //time interval 0b100 - 3 frames
            SSD1306_command(0xf);           //end page
            if (arg.equals("topleft") | arg.equals("topright"))
                SSD1306_command(0x02);          //dummy 00 . xx for horizontal scroll (speed)
            else if (arg.equals("bottomleft") | arg.equals("bottomright"))
                SSD1306_command(0xfe);          //dummy 00 . xx for horizontal scroll (speed)
            else if (arg.equals("left") | arg.equals("right")) {
                SSD1306_command(0x02);          //dummy 00 . xx for horizontal scroll (speed)
                SSD1306_command(0xff);
            }
        }
        SSD1306_command(0x2F);
        if (arg.equals("stop"))
            SSD1306_command(0x2E);
    }

    public void pulseIt() throws InterruptedException, IOException {
        for (int a = 0; a < 2; a++) {
            SSD1306_command(0xD6);
            SSD1306_command(0x01);
            TimeUnit.MILLISECONDS.sleep(100);
            SSD1306_command(0xD6);
            SSD1306_command(0x00);
            TimeUnit.MILLISECONDS.sleep(100);
        }
    }
}
