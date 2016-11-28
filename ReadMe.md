[![](https://jitpack.io/v/Kunzisoft/Android-SwitchDateTimePicker.svg)](https://jitpack.io/#Kunzisoft/Android-SwitchDateTimePicker) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-SwitchDateTimePicker-blue.svg?style=flat)](http://android-arsenal.com/details/1/4513)

## Donation

Donations will be used to create free and open source applications.

[![Alt attribute for your image](https://lh3.googleusercontent.com/d1aTMwN6NMJmcMdsz24h_J4JmH5aZ9lhbJdZWQ3VFne3VZxiUVPrYZ41qm1Zig2ha4lU4Wg_BSAE_w=w1920-h1200-no "")](https://youtube.streamlabs.com/UC_U4icXPFfgKo4IDSTSzBEQ "Kunzisoft Donation")

# Android SwitchDateTime Picker

SwitchDateTime Picker is a library for select a *Date* object in dialog with a DatePicker (Calendar) and a TimePicker (Clock) in the same UI.

<img src="https://raw.githubusercontent.com/J-Jamet/Android-SwitchDateTimePicker/master/art/demo1.gif">

<img src="https://raw.githubusercontent.com/J-Jamet/Android-SwitchDateTimePicker/master/art/demo2.gif">

## Installation
Add the JitPack repository in your build.gradle at the end of repositories:
```
	allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
	}
```
And add the dependency
```
	dependencies {
	        compile 'com.github.Kunzisoft:Android-SwitchDateTimePicker:1.0'
	}
```

## Usage

### SimpleDateFormat for Day and Month
You can specify a particular [*SimpleDateFormat*](https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html) for value of Day and Month with **setSimpleDateFormat(SimpleDateFormat format)**
Warning, the format must satisfy the regular expression : **(M|w|W|D|d|F|E|u|\s)***
, for example *dd MMMM*

### 24 Hours mode
By default, time is in 12 hours mode, If you want a 24-hour display, use:
`dateTimeFragment.set24HoursMode(true);`
before the "show"

### Style
You can customize the style to change color, bold, etc... of each element.
In your *styles.xml*, for example
```
<resources>
    <!-- Base application theme. -->
    <style name="SwitchDateTimeThemeCustom" parent="AlertDialog.AppCompat.Light">
        <!-- Customize your theme here. -->
        <item name="colorPrimary">@color/colorPrimary</item>
        <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
        <item name="colorAccent">@color/colorAccent</item>
    </style>

    <!-- Custom SwitchDateTime style, each element is optional-->
		<!-- Theme for SwitchDateTime -->
		    <style name="Theme.SwitchDateTime" parent="Theme.AppCompat.Light.DarkActionBar">
		        <!-- DateTime -->
		        <item name="dateTimeColorBackground">#c94545</item>
		        <item name="dateTimeColorLabelBackground">#c94545</item>
		        <item name="dateTimeColorLabel">#cacaca</item>
		        <item name="dateTimeColorValue">#fff</item>
		        <item name="dateTimeColorIcon">#fff</item>

		        <!-- Time -->
		        <item name="timeLabelColorBackground">#c94545</item>
		        <item name="timeLabelColorAccent">#fff</item>
		        <item name="timeLabelColor">#fff</item>

		        <item name="timeColorBackground">#fff</item>
		        <item name="timeColorCircle">#e1e1e1</item>
		        <item name="timeColorCenter">#000</item>
		        <item name="timeColorNumbers">#494949</item>
		        <item name="timeColorSelector">#c94545</item>

		        <item name="timeAmPmColorBackground">#c94545</item>
		        <item name="timeAmPmColorSelectBackground">#c94545</item> <!-- Alpha is apply-->
		        <item name="timeAmPmColorText">#fff</item>

		        <!-- Date -->
		        <item name="dateLabelColorBackground">#c94545</item>
		        <item name="dateSelectDayColor">#c94545</item>

		        <!-- Year -->
		        <item name="dateSelectYearBackgroundColor">#c94545</item>
		        <item name="dateSelectYearTextColor">#fff</item>
		    </style>

		    <style name="Theme.SwitchDateTime.TitleDateTimeLabelText">
		        <item name="android:textSize">18sp</item>
		        <item name="android:textColor">#fff</item>
		        <item name="android:textStyle">bold</item>
		    </style>

		    <!-- Time precise -->
		    <style name="Theme.SwitchDateTime.TimeLabelText">
		        <item name="android:textSize">26sp</item>
		        <item name="android:textColor">#fff</item>
		    </style>

		    <style name="Theme.SwitchDateTime.TimeLabelAmPm">
		        <item name="android:textSize">12sp</item>
		        <item name="android:textColor">#fff</item>
		        <item name="android:textStyle">bold</item>
		    </style>

		    <!-- Date Precise -->
		    <style name="Theme.SwitchDateTime.DateLabelMonthAndDay">
		        <item name="android:textSize">26sp</item>
		        <item name="android:textColor">#fff</item>
		    </style>

		    <!-- Year Precise -->
		    <style name="Theme.SwitchDateTime.DateLabelYear">
		        <item name="android:textSize">16sp</item>
		        <item name="android:textColor">#fff</item>
		        <item name="android:textStyle">bold</item>
		    </style>
</resources>

```

### Sample
You can see
https://github.com/J-Jamet/Android-SwitchDateTimePicker/blob/master/sample/src/main/java/com/kunzisoft/switchdatetimesample/Sample.java
for complete sample.
```
// Initialize
SwitchDateTimeDialogFragment dateTimeDialogFragment = SwitchDateTimeDialogFragment.newInstance(
        "Title example",
        "OK",
        "Cancel"
);

// Assign values
dateTimeFragment.set24HoursMode(true);
dateTimeFragment.setDefaultHourOfDay(15);
dateTimeFragment.setDefaultMinute(20);
dateTimeFragment.setDefaultDay(4);
dateTimeFragment.setDefaultMonth(Calendar.DECEMBER);
dateTimeFragment.setDefaultYear(2018);

// Define new day and month format
try {
    dateTimeFragment.setSimpleDateMonthAndDayFormat(new SimpleDateFormat("dd MMMM", Locale.getDefault()));
} catch (SwitchDateTimeDialogFragment.SimpleDateMonthAndDayFormatException e) {
    Log.e(TAG, e.getMessage());
}

// Set listener
dateTimeDialogFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonClickListener() {
    @Override
    public void onPositiveButtonClick(Date date) {
        // Date is get on positive button click
        // Do something
    }

    @Override
    public void onNegativeButtonClick(Date date) {
        // Date is get on negative button click
    }
});

// Show
dateTimeDialogFragment.show(getSupportFragmentManager(), "dialog_time");
```
## Bonus
You can follow the project live on https://www.livecoding.tv/kunzisoft/

## License

Copyright (c) 2016 JAMET Jeremy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
