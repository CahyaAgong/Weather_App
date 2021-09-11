package com.jeje.weather_trial.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class weatherData {

    private String mTemperature, micon, mcity, mWeatherType, mPressure, mHumidity, mbackground, mfontColor, mDay;
    private double lat, lon;
    private int mCondition;

    public static weatherData fromJson(JSONObject jsonObject)
    {
        try
        {
            weatherData weatherD=new weatherData();
            weatherD.mcity = jsonObject.getString("name");
            weatherD.mCondition = jsonObject.getJSONArray("weather").getJSONObject(0).getInt("id");
            weatherD.mWeatherType = jsonObject.getJSONArray("weather").getJSONObject(0).getString("main");
            weatherD.micon = updateWeatherIcon(weatherD.mCondition);
            weatherD.mbackground = updateWeatherBackground(weatherD.mCondition);
            weatherD.mfontColor = updateColorText(weatherD.mCondition);
            double tempResult = jsonObject.getJSONObject("main").getDouble("temp")-273.15;
            int roundedValue = (int)Math.rint(tempResult);
            weatherD.mTemperature = Integer.toString(roundedValue);
            weatherD.mPressure = Integer.toString(jsonObject.getJSONObject("main").getInt("pressure"));
            weatherD.mHumidity = Integer.toString(jsonObject.getJSONObject("main").getInt("humidity"));
            weatherD.lat = jsonObject.getJSONObject("coord").getDouble("lat");
            weatherD.lon = jsonObject.getJSONObject("coord").getDouble("lon");

            Date date = new Date(jsonObject.getInt("dt") * 1000L);
            SimpleDateFormat jdf = new SimpleDateFormat("EEEE, dd MMM");
            weatherD.mDay = jdf.format(date);

            return weatherD;
        }
         catch (JSONException e) {
            e.printStackTrace();
            return null;
        }


    }


    private static String updateWeatherIcon(int condition)
    {
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

    private static String updateWeatherBackground(int condition)
    {
        if(condition>=0 && condition<=300)
        {
            return "background_thunder";
        }
        else if(condition>=300 && condition<=500)
        {
            return "background_rainy";
        }
        else if(condition>=500 && condition<=600)
        {
            return "background_rainy";
        }
        else  if(condition>=600 && condition<=700)
        {
            return "background_snowy";
        }
        else if(condition>=701 && condition<=771)
        {
            return "background_cloudy";
        }

        else if(condition>=772 && condition<=800)
        {
            return "background_cloudy";
        }
        else if(condition==800)
        {
            return "background_sunny";
        }
        else if(condition>=801 && condition<=804)
        {
            return "background_cloudy";
        }

        return "dunno";
    }

    private static String updateColorText(int condition)
    {

        if(condition>=0 && condition<=300)
        {
            return "#ffffff";
        }
        else if(condition>=300 && condition<=500)
        {
            return "#ffffff";
        }
        else if(condition>=500 && condition<=600)
        {
            return "#ffffff";
        }
        else  if(condition>=600 && condition<=700)
        {
            return "#61625C";
        }
        else if(condition>=701 && condition<=771)
        {
            return "#ffffff";
        }

        else if(condition>=772 && condition<=800)
        {
            return "#ffffff";
        }
        else if(condition==800)
        {
            return "#ffffff";
        }
        else if(condition>=801 && condition<=804)
        {
            return "#ffffff";
        }

        return "dunno";
    }

    public String getmTemperature() {
        return mTemperature+"°C";
    }

    public String getMicon() {
        return micon;
    }

    public String getMcity() {
        return mcity;
    }

    public String getmWeatherType() {
        return mWeatherType;
    }

    public String getmPressure() {
        return mPressure;
    }

    public String getmHumidity() {
        return mHumidity;
    }

    public String getMbackground() {
        return mbackground;
    }

    public String getMfontColor() {
        return mfontColor;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getmDay() {
        return mDay;
    }
}
