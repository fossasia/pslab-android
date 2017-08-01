# pslab-android
PSLab Android App

[![Build Status](https://travis-ci.org/fossasia/pslab-android.svg?branch=master)](https://travis-ci.org/fossasia/pslab-android)
[![Gitter](https://badges.gitter.im/fossasia/pslab.svg)](https://gitter.im/fossasia/pslab?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Preview app](https://img.shields.io/badge/Preview-Appetize.io-orange.svg)](https://appetize.io/app/4eqye6ea422e5np0gp2jfpemgm)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/dd728d91bb5743ff916c16c1251f8dd5)](https://www.codacy.com/app/praveenkumar103/pslab-android?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=fossasia/pslab-android&amp;utm_campaign=Badge_Grade)
[![Mailing List](https://img.shields.io/badge/Mailing%20List-FOSSASIA-blue.svg)](mailto:pslab-fossasia@googlegroups.com)

This repository holds the Android App for performing experiments with [PSLab](http://pslab.fossasia.org/). PSLab is a tiny pocket science lab that provides an array of equipment for doing science and engineering experiments. It can function like an oscilloscope, waveform generator, frequency counter, programmable voltage and current source and also as a data logger. Our website is at: http://pslab.fossasia.org

## Communication

Please join us on the following channels:
* [Pocket Science Channel](https://gitter.im/fossasia/pslab)
* [Mailing List](https://groups.google.com/forum/#!forum/pslab-fossasia)

## Roadmap
 - [x] First we need to get communication between Android App and PSLab working.
 - [ ] Implement Applications and expose PSLab Hardware functionality to user.
 - [ ] Implement Functionality to Perform Experiment using PSLab Hardware Device. 

## Screenshots

  <table>
    <tr>
     <td><img src="/docs/screenshots/screenshot_initialization.png"></td>
     <td><img src="/docs/screenshots/screenshot_1.png"></td>
     <td><img src="/docs/screenshots/screenshot_2.png"></td>
     <td><img src="/docs/screenshots/screenshot_control_main.png"></td>
    </tr>
  </table>
  <table>
    <tr>
     <td><img src="/docs/screenshots/screenshot_3.png"></td>
     <td><img src="/docs/screenshots/screenshot_logical_analyzer.png"></td>
    </tr>
  </table>
  <table>
    <tr>
     <td><img src="/docs/screenshots/screenshot_multilevel_experimentlist.png"></td>
     <td><img src="/docs/screenshots/screenshot_experiment_doc.png"></td>
     <td><img src="/docs/screenshots/screenshot_experiment_setup.png"></td>
    </tr>
  </table>
  <table>
    <tr>
     <td><img src="/docs/screenshots/screenshot_audio_jack.png"></td>
    </tr>
  </table>

## Features
**Feature**|**Description**|**Status**
-----|-----|-----
Home Screen|Show status and version of PSLab device|Established
Application|Exposes PSLab application like Oscilloscope,etc |Established
 |Oscilloscope |Shows variation of analog signals | Working
 |Control |Generate waveforms and PWM signals. Control voltage and current sources and read results from a variety of electric components | Working
 |Logical Analyzer |Captures and displays signals from digital system | Working
 |Data Sensor Logger |Captures and displays data from various sensors | Working
 |Wireless Sensor |Scans, captures and displays data from various wireless sensors |
 |Sensor Quick View |Scans all the sensors connected to PSLab Device and provides interface to Log data| Working
 |Settings |Enable Auto-Start |
 |Saved Experiments |Access Pre-defined and Designed Experiments | Working
 |Design Experiments |To Design our own experiments |

## How to set up the Android app in your development environment

### Development Setup

Before you begin, you should already have the Android Studio SDK downloaded and set up correctly. You can find a guide on how to do this here: [Setting up Android Studio](http://developer.android.com/sdk/installing/index.html?pkg=studio)

### Setting up the Android Project

1. Download the _pslab-android_ project source. You can do this either by forking and cloning the repository (recommended if you plan on pushing changes) or by downloading it as a ZIP file and extracting it.

2. Open Android Studio, you will see a **Welcome to Android** window. Under Quick Start, select _Import Project (Eclipse ADT, Gradle, etc.)_

3. Navigate to the directory where you saved the pslab-android project, select the "pslab-android" folder, and hit OK. Android Studio should now begin building the project with Gradle.

4. Once this process is complete and Android Studio opens, check the Console for any build errors.

  - _Note:_ If you receive a Gradle sync error titled, "failed to find ...", you should click on the link below the error message (if available) that says _Install missing platform(s) and sync project_ and allow Android studio to fetch you what is missing.

5. Once all build errors have been resolved, you should be all set to build the app and test it.

6. To Build the app, go to _Build>Make Project_ (or alternatively press the Make Project icon in the toolbar).

7. If the app was built successfully, you can test it by running it on either a real device or an emulated one by going to _Run>Run 'app'_ or presing the Run icon in the toolbar.
 
If you want build apk only, go to Build>Build apk and apk would be build and directory where apk is generated would be prompted by Android Studio.

You can't debug the usual way as PSLab device is connected to micro-USB port through OTG cable. So Android Device is not connected to PC through usb cable. 

To debug over Wi-Fi : http://stackoverflow.com/questions/4893953/run-install-debug-android-applications-over-wi-fi
 
Note : 
1. If you built your own hardware, change VendorID and/or ProductID in [CommunicationHandler.java](https://github.com/fossasia/pslab-android/blob/master/app/src/main/java/org/fossasia/pslab/communication/CommunicationHandler.java) 
 
## Setup to use PSLab with Android App
To use PSLab device with Android, you simply need an OTG cable, an Android Device with USB Host feature enabled ( most modern phones have OTG support ) and PSLab Android App. Connect PSLab device to Android Phone via OTG cable. Rest is handled by App itself.

## Code practices

Please help us follow the best practice to make it easy for the reviewer as well as the contributor. We want to focus on the code quality more than on managing pull request ethics. 

 * Single commit per pull request
 * Reference the issue numbers in the commit message. Follow the pattern ``` Fixes #<issue number> <commit message>```
 * Follow uniform design practices. The design language must be consistent throughout the app.
 * The pull request will not get merged until and unless the commits are squashed. In case there are multiple commits on the PR, the commit author needs to squash them and not the maintainers cherrypicking and merging squashes.
 * If the PR is related to any front end change, please attach relevant screenshots in the pull request description.

## Code style

Please try to follow the mentioned guidelines while writing and submitting your code as it makes easier for the reviewer and other developers to understand.

 * While naming the layout files, ensure that the convention followed is (activity/fragment) _ (name).xml like ```activity_oscilloscope.xml``` , ```fragment_control_main.xml``` .
 * Name the views and widgets defined in the layout files as (viewtype/widget) _ (fragment/activity name) _ (no. in the file) like ```spinner_channel_select_la1``` , ```button_activity_oscilloscope1``` .
 * The activity/fragment file name corresponding to the layout files should be named as                       (activity/fragment name)(activity/fragment).java like ```ChannelsParameterFragment.java``` corresponding to the layout file ```fragment_channels_parameter.xml``` .
 * The corresponding widgets for buttons, textboxes, checkboxes etc. in activity files should be named as (viewtype/widget)(fragment/activity name)(no. in the file) like ```spinnerChannelSelect1``` corresponding to ```spinner_channel_select1``` .
