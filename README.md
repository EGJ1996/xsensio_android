# NFCSensorComm
This is an Android application allowing to communicate, using NFC, with a NFC tag and a micro-controller (MCU) 
in Extended mode (see Datasheet AMS AS3955).


## Description of the main parts of the code
The Java source code of the app (ignoring the unit tests) has its root at
NFCSensorComm/app/src/main/java/com/xsensio/nfcsensorcomm/

From there, the code can be seen as being split between the following parts:
1. mainactivity: this folder contains the code related to the main "screen" (Activity)
of the app, that is, the one that the user falls in when the app starts. 
The architecture is the following: MainActivity is an Activity containing 4 differents tabs
which all have a different purpose, explained below.
  * sensorcomm: allows the user to send the command to the MCU to read and return samples from the sensors. This is the main feature of the app.
  * tagconfiguration: allows the user to read and modify the configuration of a NFC tag. 
  * phonetagcomm: allows the user to read/write the EEPROM of an NFC tag.
  * phonemcucomm: allows the user to perform read/write operations in Extended mode with the MCU. This part is only intended to help in the development of the app.
2. calibration: allows the user to manage (create, update, delete) the calibration profiles used to convert the readouts 
received from the sensors into so-called "outputs", for instance concentration or pH. 
5. sensorresult: presents the results (i.e. the outputs VS time) of one pair (sensor, readout case)
3. model: contains the Java classes modeling different aspects of the application such as calibration profiles,
the configuration of an NFC tag, etc.
4. nfc: contains the code allowing the app to communicate using the NFC.
5. settings: contains the user-defined and editable settings of the app.


## General architecture:

### Activities and Fragments
In Android, the user can "move" between different "screens" each with its own 
features and appearance. These screens are called "activities". The content of an activity
is usually defined in a layout file (one per activity), stored in NFCSensorComm/app/src/main/res/layout/
. An activity can also contain "fragments" which define a part of a "screen", that is,
several fragments can be displayed at the same time in an activity. One of the benefit
of fragments is that piece of the GUI canbe re-used in different places of the app without
repeating the code. Since the interface of this app is quite simple, fragments are only
used where really necessary. For instance, MainActivity is an activity that uses tabs and each tab is a fragment: 
PhoneMcuCommFragment, PhoneTagCommFragment, SensorCommFragment and NfcTagConfigurationfragment.
CalibrationActivity is an activity that has no fragment, that is, what is displayed on the screen
is only defined in the layout file activity_calibration.xml.

### Code organization of each Activity/Fragment
In order to be easily maintainable and testable, the architecture pattern 
Model-View-Presenter (https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93presenter) has bveen used.
It consists in separating the code in 3 parts (as described by Wikipedia):
1. The Model is an interface defining the data to be displayed or otherwise acted upon in the user interface.
2. The View: The view is a passive interface that displays data (the model) and routes user commands 
(events) to the presenter to act upon that data.
3. The Presenter: The presenter acts upon the model and the view. It retrieves data from repositories 
(the model), and formats it for display in the view.

In this app, the pieces of code related to the model are stored in 
NFCSensorComm/app/src/main/java/com/xsensio/nfcsensorcomm/model/ since they are common to the whole app.
Then, for each activity/fragment, there exists one presenter and one view. The activity/fragment is actually the view
since it is the piece of code defining the GUI. For each activity, there exists a Java interface (name suffixed by the word
"Contract") which defines the functions that the view and the presenter must implement.


