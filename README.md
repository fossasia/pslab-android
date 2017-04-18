# pslab-android
PSLab Android App

[![Build Status](https://travis-ci.org/fossasia/pslab-android.svg?branch=master)](https://travis-ci.org/fossasia/pslab-android)
[![Gitter](https://badges.gitter.im/fossasia/pslab.svg)](https://gitter.im/fossasia/pslab?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/dd728d91bb5743ff916c16c1251f8dd5)](https://www.codacy.com/app/praveenkumar103/pslab-android?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=fossasia/pslab-android&amp;utm_campaign=Badge_Grade)

This repository holds the Android App for performing experiments with [PSLab](http://pslab.fossasia.org/). PSLab is a tiny pocket science lab that provides an array of equipment for doing science and engineering experiments. It can function like an oscilloscope, waveform generator, frequency counter, programmable voltage and current source and also as a data logger.

## Communication
Our chat channel is on gitter here: https://gitter.im/fossasia/pslab

## Roadmap
 - First we need to get communication between Android App and PSLab working.

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
1. Don't upgrade the gradle version, when Android Studio prompts to upgrade gradle version.
2. If you built your own hardware, change VendorID and/or ProductID in [CommunicationHandler.java](https://github.com/fossasia/pslab-android/blob/master/app/src/main/java/org/fossasia/pslab/communication/CommunicationHandler.java) 
 
## Setup to use PSLab with Android App
To use PSLab device with Android, you simply need an OTG cable, an Android Device with USB Host feature enabled ( most modern phones have OTG support ) and PSLab Android App. Connect PSLab device to Android Phone via OTG cable. Rest is handled by App itself.

## Code practices

Please help us follow the best practice to make it easy for the reviewer as well as the contributor. We want to focus on the code quality more than on managing pull request ethics. 

 * Single commit per pull request
 * Reference the issue numbers in the commit message. Follow the pattern ``` Fixes #<issue number> <commit message>```
 * Follow uniform design practices. The design language must be consistent throughout the app.
 * The pull request will not get merged until and unless the commits are squashed. In case there are multiple commits on the PR, the commit author needs to squash them and not the maintainers cherrypicking and merging squashes.
 * If the PR is related to any front end change, please attach relevant screenshots in the pull request description.


