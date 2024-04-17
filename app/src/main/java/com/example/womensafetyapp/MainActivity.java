package com.example.womensafetyapp;
import com.google.android.gms.location.LocationRequest;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
//import android.location.LocationRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;
/////////////////////////
//////////////////////
public class MainActivity extends AppCompatActivity implements SensorEventListener {
    FirebaseAuth auth;

        RecyclerView mainUserRecyclerView; //1no
        UserAdpter adpter;      //2
        FirebaseDatabase database;    //3
        ArrayList<Users> usersArrayList; //9
        ImageView imglogout;

////////////////////////vibrate section////////////////////////////////


        private SensorManager sensorManager;
        private Sensor accelerometerSensor;
        private boolean isAcclerometersensoravailable, notfirsttime=false;
        private float cx,cy,cz;
        private float  lx,ly,lz;
        private float xDfference,yd,zd;
        private float shakevib=13f;
        private Vibrator vibrator;
///////////////////////////////////////////////
    ////////////// for location part//////////////
    private LocationRequest locationRequest;
    private double longitude1;
    private double latitude1;
    ///////////////////////////////////

    @SuppressLint({"MissingInflatEdId", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();


        ///////////////////for vibration///////////////
        vibrator= (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!=null){
            accelerometerSensor=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            isAcclerometersensoravailable=true;
        }else{
            isAcclerometersensoravailable=false;
        }
        ////////////////////////////////

        ////////////// for location ///////////////

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        //////////////////////////////////////

        database=FirebaseDatabase.getInstance();//7

        auth = FirebaseAuth.getInstance();
        View cumbut = findViewById(R.id.camBut);
        View setbut = findViewById(R.id.settingBut);
        View sosbut = findViewById(R.id.sosBut);
        DatabaseReference reference = database.getReference().child("user"); //8



        usersArrayList= new ArrayList<>(); //10

        mainUserRecyclerView = findViewById(R.id.mainUserRecyclerView);
        mainUserRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adpter = new UserAdpter(MainActivity.this,usersArrayList);
        mainUserRecyclerView.setAdapter(adpter);

        //11 starts here
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Users users=dataSnapshot.getValue(Users.class);
                    usersArrayList.add(users);
                }
                 adpter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
           });
        imglogout = findViewById(R.id.logoutimg);
        imglogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(MainActivity.this,R.style.dialog);
                dialog.setContentView(R.layout.dialog_layout);
                Button no,yes;
                yes=dialog.findViewById(R.id.yesbnt);
                no=dialog.findViewById(R.id.nobnt);
                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent=new Intent(MainActivity.this,Login.class);
                        startActivity(intent);
                        finish();
                    }
                });
                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        //11 ends here
        mainUserRecyclerView=findViewById(R.id.mainUserRecyclerView);     //4
        mainUserRecyclerView.setLayoutManager(new LinearLayoutManager(this)); //5

        adpter = new UserAdpter(MainActivity.this,usersArrayList);//12
        mainUserRecyclerView.setAdapter(adpter); //6




        /////////////////
        setbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, setting.class);
                startActivity(intent);
            }
        });

        cumbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,10);
            }
        });

        sosbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, sos_emergency.class);
                startActivity(intent);
            }
        });

        if(auth.getCurrentUser() == null){
            Intent intent = new Intent(MainActivity.this,Login.class);
            startActivity(intent);
        }
    }

    ///////////////////////sending message using shaking////////////////////////////////////
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        cx=sensorEvent.values[0];
        cy=sensorEvent.values[1];
        cz=sensorEvent.values[2];
        if(notfirsttime){
            xDfference=Math.abs(lx-cx);
            yd=Math.abs(ly-cy);
            zd=Math.abs(lz-cz);
            if((xDfference>shakevib && yd>shakevib)||(xDfference>shakevib && zd>shakevib)|| (yd>shakevib && zd>shakevib)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500,VibrationEffect.DEFAULT_AMPLITUDE));


                    if(checkSelfPermission(Manifest.permission.SEND_SMS)== PackageManager.PERMISSION_GRANTED){
                        getCurrentLocation();
                       // sendSMS();
                    }else{
                        requestPermissions(new String[]{Manifest.permission.SEND_SMS},1);
                    }


                }else{
                    vibrator.vibrate(500);
                    sendSMS();
                    getCurrentLocation();
                }
            }

        }
        lx=cx;
        ly=cy;
        lz=cz;
        notfirsttime=true;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    @Override
    protected void onPause() {
        super.onPause();
        if(isAcclerometersensoravailable){
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(isAcclerometersensoravailable){
            sensorManager.registerListener(this,accelerometerSensor,SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void sendSMS(){
//        String phoneNo=number.getText().toString().trim();

        String phoneNo[] = {"8101414010","7908537735","9333038520","6296461324","8101552885"};
        String SMS="Please Help i'm in danger!"+"current location ---> Latitude : "+latitude1+"  Longitude : "+longitude1;

        int i=0;
        for(i=0;i<phoneNo.length;i++) {

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo[i], null, SMS, null, null);
            Toast.makeText(this, "Message is sent", Toast.LENGTH_SHORT).show();

        }
    }

    //////////////////// location part///////////////////
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){

                if (isGPSEnabled()) {

                    getCurrentLocation();

                }else {

                    turnOnGPS();
                }
            }
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {

                getCurrentLocation();
            }
        }
    }

    private void getCurrentLocation() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                if (isGPSEnabled()) {

                    LocationServices.getFusedLocationProviderClient(MainActivity.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);

                                    LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                            .removeLocationUpdates(this);

                                    if (locationResult != null && locationResult.getLocations().size() >0){

                                        int index = locationResult.getLocations().size() - 1;
                                        double latitude = locationResult.getLocations().get(index).getLatitude();
                                        double longitude = locationResult.getLocations().get(index).getLongitude();
                                        latitude1=latitude;
                                        longitude1=longitude;
                                        sendSMS();

                                     //  AddressText.setText("Latitude: "+ latitude1 + "\n" + "Longitude: "+ longitude1);

                                    }
                                }
                            }, Looper.getMainLooper());

                } else {
                    turnOnGPS();
                }

            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    private void turnOnGPS() {



        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(MainActivity.this, "GPS is already tured on", Toast.LENGTH_SHORT).show();

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(MainActivity.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });

    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = null;
        boolean isEnabled = false;

        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;

    }

    ////////////////////////////////////////
}