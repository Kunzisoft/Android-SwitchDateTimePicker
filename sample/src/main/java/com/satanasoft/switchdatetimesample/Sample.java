package com.satanasoft.switchdatetimesample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.satanasoft.switchdatetime.SwitchDateTimeDialogFragment;

import java.util.Date;

public class Sample extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        Button buttonView = (Button) findViewById(R.id.button);
        buttonView.setOnClickListener(new View.OnClickListener() {
            TextView textView = (TextView) findViewById(R.id.textView);
            @Override
            public void onClick(View view) {
                SwitchDateTimeDialogFragment dateTimeDialogFragment = SwitchDateTimeDialogFragment.newInstance(
                        getString(R.string.label_datetime_dialog),
                        getString(R.string.positive_button_datetime_picker),
                        getString(R.string.negative_button_datetime_picker)
                );
                dateTimeDialogFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonClickListener() {
                    @Override
                    public void onPositiveButtonClick(Date date) {
                        textView.setText(date.toString());
                    }

                    @Override
                    public void onNegativeButtonClick(Date date) {
                        textView.setText("");
                    }
                });
                dateTimeDialogFragment.show(getSupportFragmentManager(), "dialog_time");
            }
        });
    }
}
