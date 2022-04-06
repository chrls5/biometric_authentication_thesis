package com.example.thesis_final.clientSide;


import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.thesis_final.CurrentState;
import com.example.thesis_final.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoggedInView extends AppCompatActivity implements View.OnLongClickListener {

    TextView pubKeyText, privKeyText, signedDataText;
    ClipboardManager myClipboard;
    final ClipData[] myClip = new ClipData[1];
    String new_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_homepage);

        TextView linkTextView = findViewById(R.id.loggedin_message);
        linkTextView.setMovementMethod(LinkMovementMethod.getInstance());

        pubKeyText = (TextView) findViewById(R.id.pubKeyView);
        privKeyText = (TextView) findViewById(R.id.privKeyView);
        signedDataText = (TextView) findViewById(R.id.signedDataSent);

        pubKeyText.setText(CurrentState.getPubKeyLatest());
        privKeyText.setText(CurrentState.getPrivKeyLatest());
        signedDataText.setText(CurrentState.getSignedDataLatest());

        //inside oncreate
        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        pubKeyText.setOnLongClickListener(this);
        privKeyText.setOnLongClickListener(this);
        signedDataText.setOnLongClickListener(this);


        ((Button) findViewById(R.id.btnAssociateBiom)).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                            //TODO pop alert to enter password if user wants to change the pubkey it
                            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setTitle("Enter password: ");

                        final EditText input = new EditText(v.getContext());
                        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD );
                        builder.setView(input);

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new_password = input.getText().toString();
                                if (CurrentState.getUsername() != null&& new_password!=null && new_password != "" )
                                    Register.alterUserWithBiometrics(CurrentState.getUsername(), CurrentState.getPubKeyLatest(), v);
                                else
                                    Toast.makeText(v.getContext(), "Something went wrong. Try logging in again!", Toast.LENGTH_SHORT);
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        builder.show();

                    }
                }
        );

    }

    @Override
    public boolean onLongClick(View v) {
        String text;

        switch (v.getId()) {
            case R.id.pubKeyView:
                text = pubKeyText.getText().toString();
                break;
            case R.id.privKeyView:
                text = privKeyText.getText().toString();
                break;
            case R.id.signedDataSent:
                text = signedDataText.getText().toString();
                break;
            default:
                text = null;
        }

        if (text == null) {
            return false;
        } else {
            myClip[0] = ClipData.newPlainText("text", text);
            myClipboard.setPrimaryClip(myClip[0]);

            Toast.makeText(getApplicationContext(), "Text Copied",
                    Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);
        builder.setTitle("Log out?");
        builder.setMessage("By going back you are also logging out. Continue?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                CurrentState.clearState();
                finish();
            }
        });
        builder.setNegativeButton("No", null);
        builder.setCancelable(false);
        builder.show();


    }
}