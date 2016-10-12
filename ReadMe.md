# Android SwitchDateTime Picker

SwitchDateTime Picker is a library for select a *Date* object in dialog with a DatePicker (Calendar) and a TimePicker (Clock) in the same UI.

<img src="https://raw.githubusercontent.com/J-Jamet/Android-SwitchDateTimePicker/master/art/demo1.gif">

SwitchDateTime use *AppTheme*, for use another theme add in *Android.manifest*:
`tools:replace="android:theme"`

## Use
You can see
https://github.com/J-Jamet/Android-SwitchDateTimePicker/blob/master/sample/src/main/java/com/kunzisoft/switchdatetimesample/Sample.java
for complete sample.


```
// Initialize
SwitchDateTimeDialogFragment dateTimeDialogFragment = SwitchDateTimeDialogFragment.newInstance(
        getString(R.string.label_datetime_dialog),
        getString(R.string.positive_button_datetime_picker),
        getString(R.string.negative_button_datetime_picker)
);
// Assign values
dateTimeDialogFragment.setYear(2016);
dateTimeDialogFragment.setMonth(12);
dateTimeDialogFragment.setMonth(10);
dateTimeDialogFragment.setHour(1);
dateTimeDialogFragment.setMinute(20);
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