package com.jeje.weather_trial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jeje.weather_trial.model.weatherData;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    final String APP_ID = "143a73c31c2eb13fe752f75e38c10507";
    final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather"; // api for current weather
    final String WEATHER_URL2 = "https://api.openweathermap.org/data/2.5/onecall"; // api for forecast weather

    final long MIN_TIME = 5000;
    final float MIN_DISTANCE = 1000;
    final int REQUEST_CODE = 101;

    private static final String KEY_Date = "date_key";
    private static final String KEY_CITY = "city_key";

    String Location_Provider = LocationManager.GPS_PROVIDER;

    TextView NameofCity, weatherState, Temperature, Humid_Press, HumidPressTitle, DateText, TempForecast, dayForecast, Refresh, addTown;
    ImageView mweatherIcon, weatherforecastIcon;
    Spinner citySpinner;

    RelativeLayout Background;
    GridLayout dataWrap;

    LocationManager mLocationManager;
    LocationListener mLocationListner;

    int resourceID, resourceBackgroundId;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getApplicationContext().getSharedPreferences("mySavedCache", Context.MODE_PRIVATE);

        NameofCity      = findViewById(R.id.cityText);
        weatherState    = findViewById(R.id.weatherType);
        Temperature     = findViewById(R.id.temperText);
        Humid_Press     = findViewById(R.id.textHumidPress);
        DateText        = findViewById(R.id.dateText);
        HumidPressTitle = findViewById(R.id.humidityTitle);
        Refresh         = findViewById(R.id.refresh_Text);
        addTown         = findViewById(R.id.addtown_Text);

        mweatherIcon    = findViewById(R.id.icon_weather);
        Background      = findViewById(R.id.main_layout);

        dataWrap        = findViewById(R.id.dataWrap);

        citySpinner     = findViewById(R.id.citySpinner);

        /* listener click to textview on bottom */
        addTown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Sorry, this feature not yet done, but this app can select our coordinate automatically", Toast.LENGTH_SHORT).show();
            }
        });

        Refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
                Toast.makeText(MainActivity.this, "Cache Cleared!", Toast.LENGTH_SHORT).show();
            }
        });


        initializeSpinner();

        if (savedInstanceState != null) {
            dataWrap.removeAllViews();
            DateText.setText(KEY_Date);
            String savedCity = savedInstanceState.getString(KEY_CITY);
            getWeatherForNewCity(savedCity);
            for(int i = 0; i < citySpinner.getCount(); i++){
                if(citySpinner.getItemAtPosition(i).toString().equals(savedCity)){
                    citySpinner.setSelection(i);
                    break;
                }
            }
        }

        if (sharedPreferences != null){
            int iconDef = getResources().getIdentifier("ic_sunny","drawable", getPackageName());
            int backgroundDef = getResources().getIdentifier("background_sunny","drawable", getPackageName());

            String savedDate = sharedPreferences.getString("datetext", null);
            String savedCity = sharedPreferences.getString("city", null);
            String savedtemp = sharedPreferences.getString("temperature", null);
            String savedweather = sharedPreferences.getString("weatherstate", null);
            String savedHumid = sharedPreferences.getString("humid", null);
            int savedIcon = sharedPreferences.getInt("icon", iconDef);
            int savedBackg = sharedPreferences.getInt("background", backgroundDef);

            DateText.setText(savedDate);
            NameofCity.setText(savedCity);
            Temperature.setText(savedtemp);
            weatherState.setText(savedweather);
            Humid_Press.setText(savedHumid);

            mweatherIcon.setImageResource(savedIcon);
            Background.setBackgroundResource(savedBackg);

            String savedSpinner = sharedPreferences.getString("citySpinner", null);

            for(int i = 0; i < citySpinner.getCount(); i++){
                if(citySpinner.getItemAtPosition(i).toString().equals(savedSpinner)){
                    citySpinner.setSelection(i);
                    break;
                }
            }
        } else {
            getWeatherForCurrentLocation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mLocationManager != null){
            mLocationManager.removeUpdates(mLocationListner);
        } else {
            getWeatherForCurrentLocation();
        }
    }

    private void getWeatherForCurrentLocation() {

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListner = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                dataWrap.removeAllViews();
                String Latitude = String.valueOf(location.getLatitude());
                String Longitude = String.valueOf(location.getLongitude());

                RequestParams params = new RequestParams();
                params.put("lat" ,Latitude);
                params.put("lon",Longitude);
                params.put("appid",APP_ID);
                weatherRequest(params);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                //not able to get location
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
            return;
        }
        mLocationManager.requestLocationUpdates(Location_Provider, MIN_TIME, MIN_DISTANCE, mLocationListner);

    }

    private void getWeatherForNewCity(String city) {
        dataWrap.removeAllViews();
        RequestParams params=new RequestParams();
        params.put("q", city);
        params.put("appid", APP_ID);
        weatherRequest(params);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if(requestCode==REQUEST_CODE)
        {
            if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(MainActivity.this,"Locationget Succesffully",Toast.LENGTH_SHORT).show();
                getWeatherForCurrentLocation();
            }
            else
            {
                //user denied the permission
            }
        }


    }

    private void weatherRequest(RequestParams params) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(WEATHER_URL, params, new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                weatherData weatherD = weatherData.fromJson(response);
                updateUI(weatherD);

                RequestParams params = new RequestParams();
                params.put("lat", weatherD.getLat());
                params.put("lon", weatherD.getLon());
                params.put("exclude", "minutely,hourly");
                params.put("appid", APP_ID);

                weatherForecast(params);

                // super.onSuccess(statusCode, headers, response);
            }


            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                //super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    private void weatherForecast(RequestParams params) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(WEATHER_URL2, params, new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(MainActivity.this,"Data Available!",Toast.LENGTH_SHORT).show();
                try {
                    updateUIForeCast(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // super.onSuccess(statusCode, headers, response);
            }


            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    private  void updateUI(weatherData weather){
        /* set Value*/
        DateText.setText(weather.getmDay());
        Temperature.setText(weather.getmTemperature());
        NameofCity.setText(weather.getMcity());
        weatherState.setText(weather.getmWeatherType());
        Humid_Press.setText(weather.getmHumidity()+" %/"+weather.getmPressure()+" hPa");
        resourceID = getResources().getIdentifier(weather.getMicon(),"drawable", getPackageName());
        mweatherIcon.setImageResource(resourceID);
        resourceBackgroundId = getResources().getIdentifier(weather.getMbackground(),"drawable", getPackageName());
        Background.setBackgroundResource(resourceBackgroundId);

        /*
        * change color text
        * */
        NameofCity.setTextColor(Color.parseColor(weather.getMfontColor()));
        weatherState.setTextColor(Color.parseColor(weather.getMfontColor()));
        Temperature.setTextColor(Color.parseColor(weather.getMfontColor()));
        Humid_Press.setTextColor(Color.parseColor(weather.getMfontColor()));
        HumidPressTitle.setTextColor(Color.parseColor(weather.getMfontColor()));
        DateText.setTextColor(Color.parseColor(weather.getMfontColor()));

        /* save cache to sharedpreferences */
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("datetext", DateText.getText().toString());
        editor.putString("temperature", Temperature.getText().toString());
        editor.putString("city", NameofCity.getText().toString());
        editor.putString("weatherstate", weatherState.getText().toString());
        editor.putString("humid", Humid_Press.getText().toString());
        editor.putInt("icon", resourceID);
        editor.putInt("background", resourceBackgroundId);
        editor.putString("citySpinner", citySpinner.getSelectedItem().toString());
        editor.apply();
    }

    private void updateUIForeCast(JSONObject response) throws JSONException {
        dataWrap.setColumnCount(response.getJSONArray("daily").length());
        for (int i = 1; i < response.getJSONArray("daily").length(); i++) {
            View childView1;
            LayoutInflater inflater = (LayoutInflater)MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            childView1 = inflater.inflate(R.layout.forecast, null);

            weatherforecastIcon = childView1.findViewById(R.id.icon_weatherforecast1);
            dayForecast = childView1.findViewById(R.id.text_dayweatherforecast1);
            TempForecast = childView1.findViewById(R.id.text_weatherforecast1);

            int mCondition = response.getJSONArray("daily").getJSONObject(i).getJSONArray("weather").getJSONObject(0).getInt("id");
            String micon = updateWeatherIcon(mCondition);

            double tempResult = response.getJSONArray("daily").getJSONObject(i).getJSONObject("temp").getDouble("min") - 273.15;
            int roundedValue = (int)Math.rint(tempResult);
            String mTemperature = Integer.toString(roundedValue);

            Date date = new Date(response.getJSONArray("daily").getJSONObject(i).getInt("dt") * 1000L);
            SimpleDateFormat jdf = new SimpleDateFormat("EEE");
            String day = jdf.format(date);

            int resourceID = getResources().getIdentifier(micon,"drawable", getPackageName());
            weatherforecastIcon.setImageResource(resourceID);
            dayForecast.setText(day);
            TempForecast.setText(mTemperature+"°C");

            dataWrap.addView(childView1);
        }
    }

    private void initializeSpinner() {

        List<String> spinnerData = new ArrayList<>();

        spinnerData.add("Gdansk");
        spinnerData.add("Warszawa");
        spinnerData.add("Krakow");
        spinnerData.add("Wroclaw");
        spinnerData.add("Lodz");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, spinnerData);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        citySpinner.setAdapter(adapter);

        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e("Value", citySpinner.getSelectedItem().toString());
                String city = parent.getSelectedItem().toString();
                getWeatherForNewCity(city);
                mLocationManager.removeUpdates(mLocationListner);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private static String updateWeatherIcon(int condition) {
        if(condition>=0 && condition<=300)
        {
            return "ic_thunderstorm";
        }
        else if(condition>=300 && condition<=500)
        {
            return "ic_rain";
        }
        else if(condition>=500 && condition<=600)
        {
            return "ic_rain";
        }
        else  if(condition>=600 && condition<=700)
        {
            return "ic_snow2";
        }
        else if(condition>=701 && condition<=771)
        {
            return "ic_fog";
        }

        else if(condition>=772 && condition<=800)
        {
            return "ic_clouds";
        }
        else if(condition==800)
        {
            return "ic_sunny";
        }
        else if(condition>=801 && condition<=804)
        {
            return "ic_clouds";
        }

        return "dunno";
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mLocationManager != null )
        {
            mLocationManager.removeUpdates(mLocationListner);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(KEY_Date, DateText.getText().toString());
        savedInstanceState.putString(KEY_CITY, NameofCity.getText().toString());

        super.onSaveInstanceState(savedInstanceState);
    }
}