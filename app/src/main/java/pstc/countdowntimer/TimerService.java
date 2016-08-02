package pstc.countdowntimer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Chandrakant on 24-07-2016.
 */

public class TimerService extends Service {

    private static TimerService serviceInstance;

    public static String COUNTDOWN = "timer_countdown";
    public static String TIME_LEFT = "timer_timeleft";
    public static String NOTIFICATION_INTENT = "notification_intent";
    public static int FOREGROUND_NOTIFICATION_ID = 1027;
    public static String SECONDS_TIMER = "seconds_timer";
    public static String SERVICE_RUNNING_FLAG = "service_running_flag";


    Intent broadcaseIntent;
    CountDownTimer countDownTimer;
    private long millisLeft;
    private boolean foregroundActive;
    private NotificationManager mNotificationManager;
    private Notification notif;

    private SharedPreferences sharedPreferences;


    @Override
    public void onCreate(){
        super.onCreate();
        broadcaseIntent = new Intent(COUNTDOWN);
        Log.v("tag1","onCreate service reached");
        serviceInstance = this;
        foregroundActive = false;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
    }


    @Override
    public int onStartCommand(Intent i, int flags, int id) {
        Log.v("tag1", "onStartCommand reached");

        sharedPreferences.edit().putBoolean(SERVICE_RUNNING_FLAG,true).apply();
        if(i.getAction().equals(MainActivity.action_start)){
            String secs_str = i.getStringExtra(SECONDS_TIMER);
            long secs_long = Long.parseLong(secs_str);
            Log.v("tag1",Long.toString(secs_long));

            startTimer(secs_long);

        }else if(i.getAction().equals(MainActivity.action_stop)){
            stopSelf();
            stopForeground(true);
            sharedPreferences.edit().putBoolean(SERVICE_RUNNING_FLAG,false).apply();

        }

        return 0;
    }

    private void startTimer(long sec){
        sec = sec*1000;
        countDownTimer = new CountDownTimer(sec,500) {
            @Override
            public void onTick(long millisUntilFinished) {
                //Log.v("tag1","onTick reached");
                broadcaseIntent.putExtra(TIME_LEFT, millisUntilFinished);
                sendBroadcast(broadcaseIntent);
                millisLeft = millisUntilFinished;
                updateNotification(millisUntilFinished);

            }

            @Override
            public void onFinish() {
                Log.v("tag1","onFinish timer called");
                long l = 0;
                broadcaseIntent.putExtra(TIME_LEFT, l);
                sendBroadcast(broadcaseIntent);
                updateNotification(0);
                serviceInstance.stopSelf();
                sharedPreferences.edit().putBoolean(SERVICE_RUNNING_FLAG,false).apply();

                cancel();
            }
        }.start();
    }

    private void updateNotification(long millisLeft){

        if(foregroundActive){
            mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            Bitmap icon = BitmapFactory.decodeResource(serviceInstance.getResources(),
                    R.mipmap.ic_launcher);

            Intent notiIntent = new Intent(serviceInstance,MainActivity.class);
            notiIntent.setAction(NOTIFICATION_INTENT);
            notiIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            if(serviceInstance==null){
                Log.v("tag1","kat gaya");
            }
            PendingIntent pendingIntent = PendingIntent.getActivity(serviceInstance, 0, notiIntent, 0);


            notif = new NotificationCompat.Builder(serviceInstance)
                    .setContentTitle("CountDown App")
                    .setContentText(convertmilliSec(millisLeft))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true).build();

            mNotificationManager.notify(
                    FOREGROUND_NOTIFICATION_ID,
                    notif);
        }
    }

    public static TimerService getInstance(){
        return serviceInstance;
    }

    public static void stopService(){
        serviceInstance.stopSelf();
        serviceInstance = null;

    }


    public void onPauseCalled(){

        foregroundActive = true;

        Intent notiIntent = new Intent(serviceInstance,MainActivity.class);
        notiIntent.setAction(NOTIFICATION_INTENT);
        notiIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if(serviceInstance==null){
            Log.v("tag1","kat gaya");
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(serviceInstance, 0, notiIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(serviceInstance.getResources(),
                R.mipmap.ic_launcher);


        notif = new NotificationCompat.Builder(serviceInstance)
                .setContentTitle("CountDown App")
                .setContentText(convertmilliSec(millisLeft))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true).build();
        serviceInstance.startForeground(FOREGROUND_NOTIFICATION_ID,notif);

    }

    private String convertmilliSec(long millis){
        String secs_left;

        if(millis==0){
            secs_left = "Seconds Left: " + 0;
        }else {
            secs_left = "Seconds Left: " + (millis / 1000 + 1);
        }
        return secs_left;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


