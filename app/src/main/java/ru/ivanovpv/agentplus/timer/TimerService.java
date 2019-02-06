package ru.ivanovpv.agentplus.timer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

public class TimerService extends Service {
    private static final Logger logger= LoggerFactory.getLogger(TimerService.class);
    private Timer timer;
    private volatile int counter=0;
    private int tick=1000; //1 second by default
    private LocalBroadcastManager broadcastManager;
    private IBinder binder=new ServiceBinder();
    private static boolean started = false;
    public static final String TIMER_ACTION=Timer.class.getName()+".key";
    public static final String TIMER_COUNTER_KEY=Timer.class.getName()+".counter";

    public TimerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        started=true;
        runTimer();
    }

    public void pauseTimer() {
        if(timer==null)
            return;
        timer.cancel();
        timer.purge();
        timer=null;
    }

    public void runTimer() {
        timer=new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendCounter();
            }
        }, 1000, 1000);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        started=false;
    }

    private synchronized void sendCounter() {
        counter=(++counter)%100;
        logger.info("Counter="+(counter+1));
        Intent intent = new Intent(TIMER_ACTION);
        intent.putExtra(TIMER_COUNTER_KEY, counter+1);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public int getTick() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    public class ServiceBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }

    public static boolean isStarted() {
        return started;
    }

}
