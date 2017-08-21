package com.example.salsabeel.client;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "MyPrefsFile1";
    public CheckBox dontShowAgain;

    Button connectBtn,disconnectBtn;
    EditText ipEt;
    String ip = "";

    Intent controlActivity1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*  // To test dialog design, uncomment this

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("skipMessage", "NOT checked");
        editor.commit();

        */

        // hide keypad on starting the activity
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        connectBtn =  (Button) findViewById(R.id.connectBtn);
        ipEt = (EditText) findViewById(R.id.ipET);

        connectBtn.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ip = ipEt.getText().toString();
                /*

                new sendMessageTask().execute();
                for (int i = 0 ; i < 10000000 ; i++);
                if (!isConnected)
                {
                    Toast.makeText(getApplicationContext(),"Make sure you are Connected to the Quadcopter HOTSPOT",Toast.LENGTH_LONG).show();
                    isConnected = false;
                }
                */
                controlActivity1 = new Intent(MainActivity.this,controlActivity.class);
                controlActivity1.putExtra("ServerIP",ip);
                MainActivity.this.startActivity(controlActivity1);

                }
        });

    }

    @Override
    protected void onResume() {


        // 1. Instantiate an AlertDialog.Builder with its constructor
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        //builder.setView(inflater.inflate(R.layout.dialog_layout, null));

        View eulaLayout = inflater.inflate(R.layout.checkbox, null);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String skipMessage = settings.getString("skipMessage", "NOT checked");

        dontShowAgain = (CheckBox) eulaLayout.findViewById(R.id.skip);
        builder.setView(eulaLayout);

        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String checkBoxResult = "NOT checked";

                if (dontShowAgain.isChecked()) {
                    checkBoxResult = "checked";
                }

                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();

                editor.putString("skipMessage", checkBoxResult);
                editor.commit();

                // Do what you want to do on "OK" action

                return;
            }
        });


        if (!skipMessage.equals("checked")) {
            builder.show();
        }

        super.onResume();
    }


}
