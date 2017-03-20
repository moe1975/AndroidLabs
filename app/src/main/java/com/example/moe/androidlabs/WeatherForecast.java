package com.example.moe.androidlabs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;



public class WeatherForecast extends AppCompatActivity {

    private ForecastQuery forecast;

    private ProgressBar weatherProgress;

    private TextView currentTempText;
    private TextView minTempText;
    private TextView maxTempText;

    private ImageView weatherStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_forecast);

        weatherProgress = (ProgressBar) findViewById(R.id.progressBar);

        currentTempText = (TextView) findViewById(R.id.currentTempText);
        minTempText = (TextView) findViewById(R.id.minTempText);
        maxTempText = (TextView) findViewById(R.id.maxTempText);

        weatherStatus = (ImageView) findViewById(R.id.weatherImage);

        forecast = new ForecastQuery();
        forecast.execute();
    }

    private class ForecastQuery extends AsyncTask<String, Integer, String> {

        private String currentTemp;
        private String minTemp;
        private String maxTemp;
        private String iconName;
        private String iconFileName ="null";
        private Bitmap weatherImage;

        private static final String OttawaWeatherURL = "http://api.openweathermap.org/data/2.5/weather?q=ottawa,ca&APPID=d99666875e0e51521f0040a3d97d0f6a&mode=xml&units=metric";

        private boolean fileExists(String fileName){
            File file = getBaseContext().getFileStreamPath(fileName);
            return file.exists();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            weatherStatus.setImageBitmap(weatherImage);

            String cText = currentTemp + "\u2103";
            String minText = minTemp + "\u2103";
            String maxText = maxTemp + "\u2103";

            currentTempText.setText(cText);
            minTempText.setText(minText);
            maxTempText.setText(maxText);

            weatherProgress.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            weatherProgress.setVisibility(View.VISIBLE);
            weatherProgress.setProgress(values[0]);


        }

        @Override
        protected String doInBackground(String... params) {
            URL url;
            XmlPullParser parser;
            int progress = 0;

            try {
                url = new URL(OttawaWeatherURL);
                HttpURLConnection conn;
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();

                parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(conn.getInputStream(), null);
                parser.nextTag();

                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }
                    String name = parser.getName();
                    if (name.equals("temperature")) {
                        currentTemp = parser.getAttributeValue(null, "value");
                        minTemp = parser.getAttributeValue(null, "min");
                        maxTemp = parser.getAttributeValue(null, "max");
                    } else if(name.equals("weather")) {
                        iconFileName = parser.getAttributeValue(null, "icon");

                        FileInputStream fis;
                        Log.i("Info", iconFileName);
                        if(fileExists(iconFileName)) {
                            Log.i("Info", iconFileName + " was found");
                            try {
                                File f = getFileStreamPath(iconFileName);
                                fis = new FileInputStream(f);
                                weatherImage = BitmapFactory.decodeStream(fis);
                            }
                            catch (FileNotFoundException e) {
                                Log.e("Error", "Could not find image " + iconFileName);
                            }
                        }else
                        {
                            String imageUrl = "http://openweathermap.org/img/w/" + iconFileName + ".png";
                            iconFileName = "image" + iconFileName + ".PNG";

                            Bitmap image = HTTPUtils.getImage(imageUrl);
                            weatherImage = image;
                            FileOutputStream outputStream;
                            try {
                                outputStream = openFileOutput( iconFileName, Context.MODE_PRIVATE);
                                if(image != null)
                                    image.compress(Bitmap.CompressFormat.PNG, 80, outputStream);

                                outputStream.flush();
                                outputStream.close();
                            } catch (NullPointerException | IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }

                    progress += 1;
                    publishProgress(progress);
                }

            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }

            publishProgress(100);



            return null;
        }
    }
}
