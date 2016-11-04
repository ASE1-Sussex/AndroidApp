package com.sussex.ase1.gpstry3;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.google.android.gms.location.LocationRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    Messenger myService = null;
    boolean isBound;
    RemoteService remoteService;

    private LocationManager locationManager;
    private LocationRequest mLocationRequest;
    private android.location.LocationListener listener;
    private static final int REQUEST_LOCATION_CODE = 2;

    private double lat;
    private double lon;

    private TextView latTxt;
    private TextView lonTxt;
private TextView amazonRecords;
    private TextView localRecords;

    private String android_id;

    // Cloud AWS variables
    private DynamoDBMapper mapper;
    private AmazonDynamoDBClient dynamoClient;

    private DBHandler db;

    private int countSavingCloud;
    private final int ROWS_PER_TRANSACTION = 10;

    private int amazonCount;

    @Override
    protected void onStart()
    {
        super.onStart();
        enableLocation();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final GlobalVariables globalVariable = (GlobalVariables) getApplicationContext();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        amazonRecords = (TextView) findViewById(R.id.recordsOnAmazonTxt);
        localRecords = (TextView) findViewById(R.id.localRecordsTxt);


        android_id = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);


        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-west-2:f7e2ae4e-296a-4c7e-b1cb-c6fb371db3d8", // Identity Pool ID - UsersPoolWinter - GJJP
                Regions.US_WEST_2 // Region
        );
        dynamoClient = new AmazonDynamoDBClient(credentialsProvider);
        dynamoClient.setRegion(Region.getRegion(Regions.US_WEST_2));
        mapper = new DynamoDBMapper(dynamoClient);



        db = new DBHandler(this, null, null, 1);


        listener = new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                lat = location.getLatitude();
                lon = location.getLongitude();

                Log.w("hi",""+lat);


//insert the coordinates in the cloud DB
//                Location oLocation = new Location(lat, lon);
//                oLocation.setId_Location(UUID.randomUUID().toString());
//                oLocation.setId_user(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));

                Location latLon = new Location(lat,lon);

//                arrayLocationsCloud.add(oLocation);
                sendMessage(latLon);
                countSavingCloud++;
// removes firsts location to liberate memory
//                if(arrayLocationsCloud.size() == ROWS_MAX_LOCAL)
//                {
//                    for(int i = 0; i < ROWS_MAX_LOCAL - ROWS_PER_TRANSACTION; i++)
//                        arrayLocationsCloud.remove(i);
//                }
// executes the cloud saving
                if(countSavingCloud >= ROWS_PER_TRANSACTION && CheckInternetAccessStatus())
                {
                    new DynamoDBManagerTask().execute(returnJSONArray());
                    countSavingCloud = 0;
                }



            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET},10);
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };



        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

//        Intent serviceIntent = new Intent(getApplicationContext(), RemoteService.class);
//        bindService(serviceIntent, myConnection, Context.BIND_AUTO_CREATE);
     //   remoteService = new RemoteService(MainActivity.this);
        checkPermission();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Log.i("SETTINGSSJD", "Starting Settings Intent");
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            Log.i("SETTINGSSJD", "After Settings Intent Created");
            startActivity(settingsIntent);
            Log.i("SETTINGSSJD", "After Starting Settings Intent");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.author) {
            // Handle the camera action
        } else if (id == R.id.info) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.location) {
            Intent mapIntent = new Intent(this, LocationActivity.class);
            startActivity(mapIntent);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private ServiceConnection myConnection =
            new ServiceConnection()
            {
                @Override
                public void onServiceConnected(ComponentName className, IBinder service) {
                    myService = new Messenger(service);

                    isBound = true;
                }

                @Override
                public void onServiceDisconnected(ComponentName className) {
                    myService = null;
                    isBound = false;
                }
            };

    public void sendMessage(Location location)
    {

        db.addLocation(location, android_id);
        int numLocalRecords = db.getLocalCount();
        Log.w(""+numLocalRecords, "number of local records");
        localRecords.setText("Number of Local Records: " + numLocalRecords);
        updateNumOfAmazonRecordsText();

    }


    public List<Location> returnJSONArray()
    {

        DBHandler db = new DBHandler(this, null, null, 1);
        //JSONArray json = db.getJSONArray();
        List<Location> json = db.getListArray();
        return json;
        //Log.w("JSON","gf" + json);
    }

    public void checkPermission()
    {
        int permission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION ))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Permission to access the Location is required for this app to run.").setTitle("Permission required");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        makeRequest();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
               makeRequest();
            }
        }
    }

    protected void makeRequest()
    {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
    }
    // Set-up the location request properties
    public synchronized void setupLocationRequest()
    {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000); //milliseconds
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //is more likely to use GPS than WiFi
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(mConnectionCallbacks)
//                .addOnConnectionFailedListener(mOnConnectionFailedListener)
//                .addApi(LocationServices.API)
//                .build();
//        mGoogleApiClient.connect();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode) {
            case REQUEST_LOCATION_CODE: {
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(this, "Application will close. Permission not granted. ", Toast.LENGTH_LONG).show();

                }
                else
                {
                    setupLocationRequest();
                }
                return;
            }
        }
    }

    public void enableLocation() {

//        while (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
//        {
//
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
//                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET},10);
//
//
//            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            startActivity(i);
//
//        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //noinspection MissingPermission
            locationManager.requestLocationUpdates("gps", 1000, 0, listener);
    }
    }

    // inner class to execute the database update
    private class DynamoDBManagerTask extends AsyncTask<List<Location>, Void, String>
    {
        @Override
        protected String doInBackground(List<Location>... params)
        {
//            if (params.length > 0) {
//                if (!(params[0].get(0).getId_location() == null))
//                        mapper.batchSave(params[0]);
//                    //updateNumOfLocalRecordsText();       //resets localRecorsd TextView to 0
////                mapper.save(new Location(7877.756,56756.23,"34wGERRYf","fgJUnmp345", 5657));
//                Log.w("GERA", "OK");
//            }
//
//            ScanRequest scanr = new ScanRequest().withTableName("Location");
//            ScanResult result = dynamoClient.scan(scanr);
//            amazonCount = result.getCount();
//            Log.w("number amazon records", "" + amazonCount);
//                return null;
            return null;
        }
    }


    private void updateNumOfLocalRecordsText() {
        localRecords.setText("Number of Local Records: 0");
    }

    private void updateNumOfAmazonRecordsText() {
        amazonRecords.setText("Number of Records stored on Amazon: " + amazonCount);
    }

    // Checks if internet access is enabled
    private boolean CheckInternetAccessStatus()
    {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if(netInfo != null && netInfo.isConnectedOrConnecting())
        {
            return true;
        }
        else
        {
            Toast.makeText(this, "Internet access is not enabled in your device, go to settings and enable it first.", Toast.LENGTH_LONG).show();
            return false;
        }
    }
}

