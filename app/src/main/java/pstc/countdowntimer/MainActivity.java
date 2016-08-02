package pstc.countdowntimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Time;
import java.util.Timer;


public class MainActivity extends AppCompatActivity {

    public static String action_start = "start_timer";
    public static String action_stop = "stop_timer";
    private TextView textClock;
    private TimerService timerService;
    private boolean ServiceRunning;
    private EditText secondsEdit;
    private Button button1;

    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ServiceRunning = false;
        textClock = (TextView) findViewById(R.id.textClockView);
        secondsEdit = (EditText) findViewById(R.id.seconds_edit);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        button1 = (Button) findViewById(R.id.button_start);
        if(sharedPreferences.getBoolean(TimerService.SERVICE_RUNNING_FLAG,false)==true){
            button1.setText("Stop Timer");
        }
    }

    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTimeLeft(intent);
            if(intent.getLongExtra(TimerService.TIME_LEFT,-1) == 0){

                Toast.makeText(getBaseContext(),"Count down completed!",Toast.LENGTH_SHORT).show();
                stopService(new Intent(getApplicationContext(), TimerService.class));
                button1.setText("Start Timer");
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(br, new IntentFilter(TimerService.COUNTDOWN));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(br);
        if(sharedPreferences.getBoolean(TimerService.SERVICE_RUNNING_FLAG,false)==true) {
            timerService = TimerService.getInstance();
            timerService.onPauseCalled();
        }
    }


    private void updateTimeLeft(Intent i){
        long millis_left = i.getLongExtra(TimerService.TIME_LEFT,0);

        if(millis_left==0){
            textClock.setText("Seconds Left: " + 0);
        }else {
            textClock.setText("Seconds Left: " + (millis_left / 1000 + 1));
        }
        Log.v("tag1",textClock.getText().toString());

    }

    public void startTimer(View v){
        if(sharedPreferences.getBoolean(TimerService.SERVICE_RUNNING_FLAG,false)==false){
            String secs_edit = secondsEdit.getText().toString();
            if(secs_edit.equals("")){
                Toast.makeText(this,"Enter some value",Toast.LENGTH_SHORT).show();
                return;
            }
            sharedPreferences.edit().putBoolean(TimerService.SERVICE_RUNNING_FLAG,true).apply();
            Intent i = new Intent(this,TimerService.class);
            i.setAction(action_start);
            i.putExtra(TimerService.SECONDS_TIMER, secs_edit);
            startService(i);
            ServiceRunning = true;
            secondsEdit.clearFocus();
            secondsEdit.setText("");
            button1.setText("Stop Timer");
            Log.v("tag1", "Button Pressed");
        }else{
            //TimerService.stopService();
            sharedPreferences.edit().putBoolean(TimerService.SERVICE_RUNNING_FLAG,false).apply();

            textClock.setText("-");
            button1.setText("Start Timer");
            timerService = TimerService.getInstance();
            timerService.stopSelf();
            timerService.countDownTimer.cancel();
            timerService=null;
        }

    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        if(timerService!=null){
            timerService=null;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
