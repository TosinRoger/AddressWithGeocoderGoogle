package br.com.tosin.learninggooglelocation;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import br.com.tosin.learninggooglelocation.services.LocationIntentService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static EditText editTextStreet;
    private static EditText editTextNumber;
    private static EditText editTextComplement;
    private static EditText editTextPostalCode;
    private static EditText editTextCity;
    private static EditText editTextState;

    private Button buttonFetchLocation;
    private Button buttonFetchAddress;

    private static TextView textViewLatLong;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    public final static String LOCATION = "Location";

    private static final String TAG = "LOG";//MainActivity.class.getSimpleName();

    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        this.initialise();

        if (checkPlayServices()) {
            this.buildGoogleApiClient();
        }
    }

   private void initialise(){
       editTextStreet       = (EditText) findViewById(R.id.editText_street);
       editTextNumber       = (EditText) findViewById(R.id.editText_number);
       editTextComplement   = (EditText) findViewById(R.id.editText_complement);
       editTextPostalCode   = (EditText) findViewById(R.id.editText_postalCode);
       editTextCity         = (EditText) findViewById(R.id.editText_city);
       editTextState        = (EditText) findViewById(R.id.editText_state);

       textViewLatLong = (TextView) findViewById(R.id.textView_lat_long);

       buttonFetchLocation  = (Button) findViewById(R.id.button_fetchLocation);
       buttonFetchAddress   = (Button) findViewById(R.id.button_fetchAddress);

       buttonFetchLocation.setOnClickListener(this);
       buttonFetchAddress.setOnClickListener(this);
   }

    @Override
    public void onClick (View v) {
        if (v == buttonFetchLocation) {
            displayLocation();
        }
        if (v == buttonFetchAddress) {
            displayLocation();
            if(mLastLocation != null)
                callIntentService();
        }
    }


    public void displayLocation(){
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            String text = mLastLocation.getLatitude() + "," + mLastLocation.getLongitude();

            Log.i(TAG, "Funfou: " + text);

            textViewLatLong.setText("Latitude: " + mLastLocation.getLatitude() + " , Longitude: " + mLastLocation.getLongitude());

        }
}

    @Override
    public void onConnected(Bundle connectionHint) {
        displayLocation();
    }

    @Override
    public void onConnectionSuspended (int i) {
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnectionFailed (ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            else {
                Toast.makeText(getApplicationContext(), "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onStart () {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    protected void onResume () {
        super.onResume();
        checkPlayServices();
    }

    public void callIntentService() {
        Intent intent = new Intent(this, LocationIntentService.class);
        intent.putExtra(LOCATION, mLastLocation);
        startService(intent);
    }

    public static Handler handler = new Handler(){
        @Override
        public void handleMessage (Message msg) {
            super.handleMessage(msg);
            Object object = msg.obj;
            if (object instanceof List){
                List<String> address = (List<String>) msg.obj;
                int position = 0;

                if (!address.isEmpty()) {
                    String[] temp = address.get(position).split(",");
                    editTextStreet.setText(temp[0].trim());

                    temp = temp[1].split(" - ");

                    editTextNumber.setText(temp[0].trim());

                    position++;
                }
                if (position < address.size()){
                    String[] temp = address.get(position).split("-");
                    editTextCity.setText(temp[0].trim());
                    editTextState.setText(temp[1].trim());
                    position++;
                }
                if (position < address.size()){
                    editTextPostalCode.setText(address.get(position));
                    position++;
                }

            }



        }
    };
}
