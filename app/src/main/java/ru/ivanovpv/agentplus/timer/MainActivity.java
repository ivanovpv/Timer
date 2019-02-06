package ru.ivanovpv.agentplus.timer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG=MainActivity.class.getSimpleName();
    private static final Logger logger= LoggerFactory.getLogger(MainActivity.class);
    private TimerService timerService;
    private boolean bound=false;
    private boolean paused=false;
    private int counter;
    @BindView(R.id.textCounter) TextView counterTextView;
    @BindView(R.id.pauseButton) Button pauseButton;


    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
            TimerService.ServiceBinder b = (TimerService.ServiceBinder) binder;
            timerService = b.getService();
            bound=true;
            logger.debug("Service connected="+ timerService);
        }

        public void onServiceDisconnected(ComponentName className) {
            timerService = null;
            bound=false;
            logger.debug("Service disconnected");
        }
    };

    public void doBindService() {
        Intent intent = new Intent(this, TimerService.class);
        this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                counter=intent.getIntExtra(TimerService.TIMER_COUNTER_KEY, -1);
                logger.info("Activity handled counter="+counter);
                postCounter(counter);
            }
        }

        private void postCounter(final int counter) {
            counterTextView.post(new Runnable() {
                public void run() {
                    String roman=RomanNumerals.toRoman(counter);
                    counterTextView.setText(roman);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        if (savedInstanceState != null){
            counter = savedInstanceState.getInt("counter");
            paused = savedInstanceState.getBoolean("paused");
        }
        counterTextView.setText(RomanNumerals.toRoman(counter));
        if(paused)
            pauseButton.setText(R.string.resume);
        else
            pauseButton.setText(R.string.pause);
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(receiver, new IntentFilter(TimerService.TIMER_ACTION));
        if(!TimerService.isStarted()) {
            Intent intent = new Intent(this, TimerService.class);
            this.startService(intent);
        }
        /*for(int i=1; i <= 100; i++) {
            logger.info("Roman for i="+i+" is equal to: "+RomanNumerals.toRoman(i));
        }*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        doBindService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (bound) {
            unbindService(serviceConnection);
            bound = false;
        }
    }

    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt("counter", counter);
        bundle.putBoolean("paused", paused);
    }



    @OnClick(R.id.pauseButton)
    public void performPause(Button button) {
        if(bound) {
            if(paused) {
                timerService.runTimer();
                button.setText(R.string.pause);
                paused=false;
            }
            else {
                timerService.pauseTimer();
                button.setText(R.string.resume);
                paused=true;
            }
        }

    }
}
