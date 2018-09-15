package com.bmacode17.androideatit.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bmacode17.androideatit.R;
import com.bmacode17.androideatit.common.Common;
import com.bmacode17.androideatit.databases.Database;
import com.bmacode17.androideatit.models.User;
import com.facebook.FacebookSdk;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    Button button_signIn , button_signUp;
    TextView textView_eatIt;
    ProgressBar myProgressBar;
    AlertDialog alertDialog;
    FirebaseDatabase database;
    DatabaseReference table_user;

    // Press Ctrl + O

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        This method is deprecated so simply you can delete this line of code in your class. because according to the latest Facebook we now don't need to initialize the SDK manually, it gets initialize by itself.
         */
        FacebookSdk.sdkInitialize(getApplicationContext());

        // Add this code before setContentView method
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/cambria.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_main);

        printKeyHash();

        button_signIn = (Button) findViewById(R.id.button_signIn);
        button_signUp = (Button) findViewById(R.id.button_signUp);
        textView_eatIt = (TextView) findViewById(R.id.textView_eatIt);

        // Init Paper
        // Paper is a fast NoSQL-like storage for Java/Kotlin objects on Android ,  It's more a Key-value blob store.
        Paper.init(this);

        database = FirebaseDatabase.getInstance();
        table_user = database.getReference("user");

        Typeface face = Typeface.createFromAsset(getAssets(),"fonts/myFont.ttf");
        textView_eatIt.setTypeface(face);
        button_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this , SignIn.class);
                startActivity(intent);
            }
        });

        button_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this , SignUp.class);
                startActivity(intent);
            }
        });

        // Check for remember
        String user = Paper.book().read(Common.USER_KEY);
        String password = Paper.book().read(Common.PASSWORD_KEY);
        if(user != null && password !=null)
            if(!user.isEmpty() && !password.isEmpty())
                login(user,password);
    }

    private void printKeyHash() {

        try{

            PackageInfo info = getPackageManager().getPackageInfo("com.bmacode17.androideatit",PackageManager.GET_SIGNATURES);
            for(Signature signature:info.signatures){

                MessageDigest messageDigest = MessageDigest.getInstance("SHA");
                messageDigest.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(messageDigest.digest(),Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void login(final String phone, final String password) {

        if (Common.isConnectedToInternet(getBaseContext())) {

            openAlertDialog();
            table_user.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // check if the user isn't exists in the database
                    if (dataSnapshot.child(phone).exists()) {

                        // Get user information
                        alertDialog.dismiss();
                        User user = dataSnapshot.child(phone).getValue(User.class);
                        user.setPhone(phone);
                        if (user.getPassword().equals(password)) {

                            Intent homeIntent = new Intent(MainActivity.this, Home.class);
                            Common.currentUser = user;
                            startActivity(homeIntent);
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        alertDialog.dismiss();
                        Toast.makeText(MainActivity.this, "User isn't exist in the database", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            Toast.makeText(MainActivity.this, "Check your Internet connection !", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public void openAlertDialog() {

        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.myprogressdialog, null);
        myAlertDialog.setView(dialogView);
        myAlertDialog.setCancelable(true);
        myProgressBar = (ProgressBar) dialogView.findViewById(R.id.progressBar);
        alertDialog = myAlertDialog.create();
        alertDialog.show();

        try {
            Thread.sleep(3000);
        } catch (Exception e){}
    }
}
