package com.person.countdowndemo.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.person.countdowndemo.R;
import com.person.countdowndemo.entity.CountInfo;
import com.person.countdowndemo.status.CountDownState;
import com.person.countdowndemo.utils.MediaManager;
import com.person.countdowndemo.utils.NotificationUtil;
import com.person.countdowndemo.utils.SpUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 *
 **/
public class CountDownService extends Service {

    public MutableLiveData<Long> countDownTime = new MutableLiveData<>(); // 当前倒计时时间
    public MutableLiveData<Long> countDownTotalTime = new MutableLiveData<>();
    private CountDownBinder mCountDownBinder;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private  Disposable mDisposable = null;

    private MediaManager mMediaManager;
    @Override
    public void onCreate() {
        super.onCreate();
        initData();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mCountDownBinder;
    }

    public class CountDownBinder extends Binder {
        public CountDownService getService(){
            return CountDownService.this;
        }

    }

    private void initData(){
        EventBus.getDefault().register(this);
        mCountDownBinder = new CountDownBinder();
        mMediaManager = new MediaManager(getApplicationContext());
        mMediaManager.updatePrefsLoop(R.raw.ding);
        // android 8.0起前台服务必须开启通知
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            showNotify();
        }

    }

    /**
     * EventBus 接收计时器状态
     * @param countDownState
     */
    @Subscribe(threadMode  = ThreadMode.MAIN,sticky = true)
    public void onMessage(CountDownState countDownState){
        switch (countDownState){
            case FINISH:
                mMediaManager.playBeepSound();
                SpUtils.saveBooleanData(getApplicationContext(),SpUtils.keyStop,false);
                break;
            case INIT:

                break;
            case START:
                SpUtils.saveBooleanData(getApplicationContext(),SpUtils.keyStop,false);
                break;
            case RESTART:
                SpUtils.saveBooleanData(getApplicationContext(),SpUtils.keyStop,false);
                if(countDownTime.getValue()!=null&&countDownTime.getValue()> 0){
                    startCountDownTime(countDownTime.getValue());
                }
                break;
            case STOP:
                SpUtils.saveBooleanData(getApplicationContext(),SpUtils.keyStop,true);
                stopCountDownTime();
                break;
        }
    }

    /**
     * EventBus接收倒计时总时间
     * @param countInfo
     */
    @Subscribe(threadMode  = ThreadMode.MAIN,sticky = true)
    public void onMessage(CountInfo countInfo){
        if(countInfo != null && countInfo.getTotalTime()!= 0){
            // 总时间不为0时设置当前倒计时并开始倒计时
            startCountDownTime(countInfo.getTotalTime());
            EventBus.getDefault().post(CountDownState.START);
        }else{
            // 当前时间不为0时，设置当前倒计时时间状态改为暂停
            countDownTime.setValue(countInfo.getCurrentTime());
            EventBus.getDefault().post(CountDownState.STOP);
        }

    }



    //显示通知栏
    @RequiresApi(Build.VERSION_CODES.O)
    public void showNotify() {
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(NotificationUtil.channelId,"倒计时",NotificationManager.IMPORTANCE_HIGH);
        channel.setShowBadge(true);//显示logo
        channel.setDescription(getString(R.string.app_name));//设置描述
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC); //设置锁屏可见 VISIBILITY_PUBLIC=可见
        manager.createNotificationChannel(channel);

        Notification notification = new Notification.Builder(this,NotificationUtil.channelId)
                .setChannelId(NotificationUtil.channelId)
                .setContentTitle(getString(R.string.app_name))//标题
                .setContentText(getString(R.string.count_down))//内容
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)//小图标一定需要设置,否则会报错(如果不设置它启动服务前台化不会报错,但是你会发现这个通知不会启动),如果是普通通知,不设置必然报错
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .build();
        startForeground(1,notification);//服务前台化只能使用startForeground()方法,不能使用 notificationManager.notify(1,notification); 这个只是启动通知使用的,使用这个方法你只需要等待几秒就会发现报错了
    }


    /**
     * 开始倒计时
     * @param time 倒计时总时间
     */
    public void startCountDownTime(final long time) {
        countDownTotalTime.setValue(time);
        SpUtils.saveLongData(this,SpUtils.keyCountTotalTime,(System.currentTimeMillis()/1000));
        if(mDisposable == null || mDisposable.isDisposed()){
            mDisposable = Observable.interval(1,TimeUnit.SECONDS).map(new Function<Long, Long>() {
                @Override
                public Long apply(Long aLong) throws Exception {
                    long systemTime = System.currentTimeMillis() / 1000;
                    long setTime = SpUtils.getLongData(CountDownService.this,SpUtils.keyCountTotalTime);
                    long countDown = systemTime - setTime ;
                    long currentTime = time - countDown;
                    return currentTime;
                }
            }).subscribeOn(Schedulers.io())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            if(aLong == 0){
                                EventBus.getDefault().post(CountDownState.FINISH);
                                mDisposable.dispose();
                            }
                            EventBus.getDefault().post(aLong);
                            countDownTime.postValue(aLong);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Log.d("error",throwable.toString());
                        }
                    });
            mCompositeDisposable.add(mDisposable);
        }




    }

    /**
     * 暂停倒计时
     */
    private void stopCountDownTime(){
        if(mCompositeDisposable != null){
            mCompositeDisposable.clear();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopCountDownTime();
        EventBus.getDefault().unregister(this);
    }
}
