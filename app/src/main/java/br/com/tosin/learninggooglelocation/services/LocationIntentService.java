package br.com.tosin.learninggooglelocation.services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Message;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.com.tosin.learninggooglelocation.MainActivity;

/**
 * Created by Roger on 10/10/2015.
 */
public class LocationIntentService extends IntentService {


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * name Used to name the worker thread, important only for debugging.
     */
    public LocationIntentService () {
        super("worker_thread");
    }

    @Override
    protected void onHandleIntent (Intent intent) {
        Location location = intent.getParcelableExtra(MainActivity.LOCATION);

        List<Address> addresses = new ArrayList<>();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        String error = "";
        String result = "";
        List<String> endereco = new ArrayList<>();

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

        } catch (IOException e) {
            e.printStackTrace();
            error = "Network problem";
        } catch (IllegalArgumentException e) {
            error = "Illegal Arguments";
        }

        if (addresses != null && !addresses.isEmpty()) {
            Address a = addresses.get(0);

                for (int i = 0, tam = a.getMaxAddressLineIndex(); i < tam; i++){
                    String temp = a.getAddressLine(i);
                    endereco.add(temp);
                }
        }
        else {
            result = error;
        }

        Message msg = new Message();
        msg.obj = result == null || result == "" ? endereco : result;
        MainActivity.handler.sendMessage(msg);
        Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
    }
}
