package com.example.adammb.myjobscheduler;

import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

import cz.msebera.android.httpclient.Header;

public class GetCurrentWeatherJobService extends JobService {
    public static final String TAG = GetCurrentWeatherJobService.class.getSimpleName();
    final String APP_ID= "d274b12a257d175910a465321e47dd8d";
    private final String CITY="Jakarta";


    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob() Executed");
        Log.d("TEST", "Test Executed");
        Toast.makeText(getBaseContext(), "TEST", Toast.LENGTH_SHORT).show();
        getCurrentWeather(params);

        return true;
    }

    private void getCurrentWeather(final JobParameters job) {
        Log.d(TAG,"Running");
        AsyncHttpClient client=new AsyncHttpClient();
        String url="http://api.openweathermap.org/data/2.5/weather?q="+CITY+"&appid="+APP_ID;

        Log.e(TAG, "getCurrentWeather: "+url);
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result=new String(responseBody);
                Log.d(TAG, result);
                try {
                    JSONObject responseObject=new JSONObject(result);

                    String currentWeather=responseObject.getJSONArray("weather").getJSONObject(0).getString("main");
                    String description=responseObject.getJSONArray("weather").getJSONObject(0).getString("description");
                    double tempInKelvin=responseObject.getJSONObject("main").getDouble("temp");

                    double tempInCelcius=tempInKelvin-273;
                    String temperature=new DecimalFormat("##.##").format(tempInCelcius);
                    String title="Current Weather";
                    String message=currentWeather+", "+description+" with "+temperature+" celcius ";
                    int notifId=100;

                    showNotification(getApplicationContext(),title,message,notifId);
                    jobFinished(job,false);

                } catch (JSONException e) {
                    jobFinished(job,true);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                jobFinished(job,true);
            }
        });
    }

    private void showNotification(Context context, String title, String message, int notifId) {
        NotificationManager notificationManagerCompat=(NotificationManager)context.getSystemService((Context.NOTIFICATION_SERVICE));
        Uri alarmSound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(context,"")
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_access_alarm_black_24dp)
                .setContentTitle(message)
                .setColor(ContextCompat.getColor(context,android.R.color.black))
                .setVibrate(new long[]{1000,1000,1000,1000,1000})
                .setSound(alarmSound);

        notificationManagerCompat.notify(notifId,builder.build());
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
