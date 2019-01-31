# PSLab Android App

<a href="https://play.google.com/store/apps/details?id=io.pslab"><img alt="Get it on Google Play" height="80" src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png"></a>
<a href="https://f-droid.org/app/io.pslab"><img alt="Get it on F-Droid" height="80" src="https://f-droid.org/badge/get-it-on.png"></a>

[![Build Status](https://travis-ci.org/fossasia/pslab-android.svg?branch=development)](https://travis-ci.org/fossasia/pslab-android)
[![Gitter](https://badges.gitter.im/fossasia/pslab.svg)](https://gitter.im/fossasia/pslab?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/dd728d91bb5743ff916c16c1251f8dd5)](https://www.codacy.com/app/praveenkumar103/pslab-android?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=fossasia/pslab-android&amp;utm_campaign=Badge_Grade)
[![Mailing List](https://img.shields.io/badge/Mailing%20List-FOSSASIA-blue.svg)](mailto:pslab-fossasia@googlegroups.com)
[![Twitter Follow](https://img.shields.io/twitter/follow/pslabio.svg?style=social&label=Follow&maxAge=2592000?style=flat-square)](https://twitter.com/pslabio)

This repository holds the Android App for performing experiments with [PSLab](https://pslab.io/). PSLab is a tiny pocket science lab that provides an array of equipment for doing science and engineering experiments. It can function like an oscilloscope, waveform generator, frequency counter, programmable voltage and current source and also as a data logger. Our website is at https://pslab.io

## Communication

Please join us on the following channels:
* [Pocket Science Channel](https://gitter.im/fossasia/pslab)
* [Mailing List](https://groups.google.com/forum/#!forum/pslab-fossasia)

## Roadmap
 - [x] First we need to get communication between Android App and PSLab working.
 - [ ] Implement Applications and expose PSLab Hardware functionality to the user.
 - [ ] Implement Functionality to Perform Experiment using PSLab Hardware Device.

## Screenshots

  <table>
      <tr>
       <td><img src="/docs/images/view_device_not_found.png"></td>
       <td><img src="/docs/images/view_initialization.png"></td>
       <td><img src="/docs/images/view_home_screen.png"></td>
       <td><img src="/docs/images/view_instrument_panel.png"></td>
      </tr>
  </table>
  <table>
      <tr>
       <td><img src="/docs/images/instrument_oscilloscope_guide.png"></td>
       <td><img src="/docs/images/instrument_logic_analyzer_guide.png"></td>
      </tr>
    </table>
  <table>
    <tr>
     <td><img src="/docs/images/view_about_us.png"></td>
     <td><img src="/docs/images/view_navigation_drawer.png"></td>
     <td><img src="/docs/images/view_pin_layout_front.png"></td>
     <td><img src="/docs/images/view_pin_layout_back.png"></td>
    </tr>
  </table>
  <table>
    <tr>
     <td><img src="/docs/images/instrument_oscilloscope_channelparam.png"></td>
     <td><img src="/docs/images/instrument_oscilloscope_audiojack.png"></td>
    </tr>
  </table>
  <table>
    <tr>
     <td><img src="/docs/images/instrument_wavegenerator_view.png"></td>
     <td><img src="/docs/images/instrument_logic_analyzer_view.png"></td>
    </tr>
  </table>
  <table>
    <tr>
     <td><img src="/docs/images/instrument_power_source_view.png"></td>
     <td><img src="/docs/images/instrument_luxmeter_guide.png"></td>
     <td><img src="/docs/images/instrument_multimeter_view.png"></td>
    </tr>
  </table>
  <table>
    <tr>
     <td><img src="/docs/images/instrument_barometer_view.png"></td>
     <td><img src="/docs/images/view_log_map_location.png"></td>
     <td><img src="/docs/images/view_data_logger.png"></td>
    </tr>
  </table>

## Video Demo
- [PSLab Android App Overview](https://www.youtube.com/watch?v=JJfsF0b8M8k)
- [Observing Sound Waveforms Using PSLab Device](https://www.youtube.com/watch?v=5bxDd1PiOMQ)
- [Real-time Sensor Data Logging Using Pocket Science Lab](https://www.youtube.com/watch?v=_A8h6o-UcNo)
- [Generating and Observing Waveforms Using Pocket Science Lab](https://www.youtube.com/watch?v=Ua9_OCR4p8Y)

## Features
|   **Feature**    |                    **Description**                    |   **Status**    |
|------------------|-------------------------------------------------------|-----------------|
|   Home Screen    | Show status and version of PSLab device               |  		 ✓       |
|   Instruments    | Exposes PSLab instruments like Oscilloscope, etc      | 		 ✓       |
|  Oscilloscope    | Shows variation of analog signals                     | 		 ✓       |
|   Multimeter     | Measures voltage, current, resistance and capacitance | 		 ✓       |
| Logical Analyzer | Captures and displays signals from digital system     | 		 ✓       |
|  Wave Generator  | Generates arbitrary analog and digital waveforms      | 		 ✓       |
|   Power Source   | Generates programmable voltage and currents	       | 		 ✓       |
|    Lux Meter     | Measures the ambient light intensity                  | 		 ✓       |


## How to set up the Android app in your development environment

  Minimum Android version 4.1 (API Level 16)
  
  Maximum Android version 8.1 (API Level 27)

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

7. If the app was built successfully, you can test it by running it on either a real device or an emulated one by going to _Run>Run 'app'_ or pressing the Run icon in the toolbar.

If you want build apk only, go to Build>Build apk and apk would be build and directory where apk is generated would be prompted by Android Studio.

You can't debug the usual way as PSLab device is connected to micro-USB port through OTG cable. So Android Device is not connected to PC through USB cable.

To debug over Wi-Fi: http://blog.fossasia.org/android-app-debugging-over-wifi-for-pslab/

Note :
1. If you built your own hardware, change VendorID and/or ProductID in [CommunicationHandler.java](https://github.com/fossasia/pslab-android/blob/master/app/src/main/java/org/fossasia/pslab/communication/CommunicationHandler.java)

### Permissions Required

1. Record_Audio : It is required for oscilloscope to accept inputs from the phone inbuilt microphone. You can find its implementation in [AudioJack.java](https://github.com/fossasia/pslab-android/blob/development/app/src/main/java/io/pslab/others/AudioJack.java).
2. Access_Fine_Location and Internet : It is required for use in lux meter and compass to get the coordinates for tagging the data on the map. You can find its implementation in [GPSLogger.java](https://github.com/fossasia/pslab-android/blob/development/app/src/main/java/io/pslab/others/GPSLogger.java).
3. Write_External_Storage : It is required for storing log files from instruments that can be transferred out for future analysis.
4. Read_External_Storage : While writing logs in the storage, [CSVLogger.java](https://github.com/fossasia/pslab-android/blob/development/app/src/main/java/io/pslab/others/CSVLogger.java) first checks whether there is any CSVLogger directory exist or not and that require this read permission.  

## Setup to use PSLab with Android App
To use PSLab device with Android, you simply need an OTG cable, an Android Device with USB Host feature enabled ( most modern phones have OTG support ) and PSLab Android App. Connect PSLab device to Android Phone via OTG cable. Rest is handled by App itself.

## Contributions Best Practices

### Code practices

Please help us follow the best practice to make it easy for the reviewer as well as the contributor. We want to focus on the code quality more than on managing pull request ethics.

 * Single commit per pull request
 * Reference the issue numbers in the commit message. Follow the pattern ``` Fixes #<issue number> <commit message>```
 * Follow uniform design practices. The design language must be consistent throughout the app.
 * The pull request will not get merged until and unless the commits are squashed. In case there are multiple commits on the PR, the commit author needs to squash them and not the maintainers cherrypicking and merging squashes.
 * If the PR is related to any front end change, please attach relevant screenshots in the pull request description.

#### How to `git squash`?

As a tip for new developers those who struggle with squashing commits into one, multiple commits may appear in your pull request mostly due to following reasons.

 * Intentionally adding multiple commit messages after each change without just `git add`ing.
 * Updating the current branch with the remote so a merge commit takes place.

Despite any reason, follow the steps given below to squash all commits into one adhering to our best practices.

 * Setup remote to upstream branch if not set before;

`$ git remote add upstream https://github.com/fossasia/pslab-android.git`

 * Check into the branch related to the pull request

`$ git checkout <branch-name>`

 * Perform a soft reset to retain the changes while removing all the commit details

`$ git reset --soft upstream/development`

 * Add files to the staging area

`$ git add <file paths or "." to add everything>`

 * Create a new commit with a proper message following commit message guidelines

`$ git commit -m "tag: commit message"`

 * If you have already made a pull request,

`$ git push -f origin <branch-name>`

### Branch Policy

We have the following branches
 * **development**
	 All development goes on in this branch. If you're making a contribution,
	 you are supposed to make a pull request to _development_.
	 Make sure it passes a build check on Travis.

 * **master**
   This contains the stable code. After significant features/bugfixes are accumulated on development, we move it to master.

 * **apk**
   This branch contains automatically generated apk file for testing.


### Code style

Please try to follow the mentioned guidelines while writing and submitting your code as it makes easier for the reviewer and other developers to understand.

 * While naming the layout files, ensure that the convention followed is (activity/fragment) _ (name).xml like ```activity_oscilloscope.xml``` , ```fragment_control_main.xml``` .
 * Name the views and widgets defined in the layout files as (viewtype/widget) _ (fragment/activity name) _ (no. in the file) like ```spinner_channel_select_la1``` , ```button_activity_oscilloscope1``` .
 * The activity/fragment file name corresponding to the layout files should be named as                       (activity/fragment name)(activity/fragment).java like ```ChannelsParameterFragment.java``` corresponding to the layout file ```fragment_channels_parameter.xml``` .
 * The corresponding widgets for buttons, textboxes, checkboxes etc. in activity files should be named as (viewtype/widget)(fragment/activity name)(no. in the file) like ```spinnerChannelSelect1``` corresponding to ```spinner_channel_select1``` .

## License

This project is currently licensed under the Apache License 2.0. A copy of [LICENSE](LICENSE) is to be present along with the source code. To obtain the software under a different license, please contact FOSSASIA.

## Maintainers
The project is maintained by
- Padmal ([@CloudyPadmal](https://github.com/CloudyPadmal))
- Mario Behling ([@mariobehling](http://github.com/mariobehling))
- Lorenz Gerber ([@lorenzgerber](https://github.com/lorenzgerber))
- Wei Tat ([@cweitat](https://github.com/cweitat))
- Wai Gie ([@woshikie](https://github.com/woshikie))

## Alumni
- Praveen Patil ([@wavicles](https://github.com/wavicles))
- Jithin ([@jithinbp](https://github.com/jithinbp))
- Akarshan Gandotra ([@akarshan96](https://github.com/akarshan96))
- Asitava Sarkar ([@asitava1998](https://github.com/asitava1998))
- Vivek Singh Bhadauria ([@viveksb007](https://github.com/viveksb007))
- Avjeet ([@Avjeet](https://github.com/Avjeet))
- Abhinav ([@abhinavraj23](https://github.com/abhinavraj23))
- Harsh ([@harsh-2711](https://github.com/harsh-2711))
- Yatri ([@yatri1609](https://github.com/yatri1609))
