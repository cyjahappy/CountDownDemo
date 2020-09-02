package com.person.countdowndemo;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.person.countdowndemo.entity.CountInfo;
import com.person.countdowndemo.service.CountDownService;
import com.person.countdowndemo.status.CountDownState;
import com.person.countdowndemo.utils.SpUtils;
import com.weigan.loopview.LoopView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class MainActivity extends AppCompatActivity {

    private MainViewMode mMainViewMode;
    private TextView tvCountTime;
    private Button btnSetTime;
    private Button btnSetTime2;
    private Button btnStarTime;
    private ConstraintLayout rootView;
    private Intent intent ;
    private ConstraintSet mConstraintSet;
    private ConstraintSet mConstraintSet2;
    private LoopView mHourLoopView;
    private LoopView mMinLoopView;
    private LoopView mSecLoopView;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById();
        setListener();
        initData();
        EventBus.getDefault().register(this);

    }

    /**
     * 绑定视图id
     */
    private void findViewById(){
        rootView = findViewById(R.id.root_view);
        mConstraintSet = new ConstraintSet();
        mConstraintSet.clone(rootView);
        mConstraintSet2 = new ConstraintSet();
        mConstraintSet2.clone(MainActivity.this,R.layout.activity_main_2);
        tvCountTime = findViewById(R.id.tv_count_time);
        btnSetTime = findViewById(R.id.btn_set_time);
        btnSetTime2 = findViewById(R.id.btn_set_time2);
        btnStarTime = findViewById(R.id.btn_start_countdown);
        mHourLoopView = findViewById(R.id.loopView_hour);
        mMinLoopView = findViewById(R.id.loopView_min);
        mSecLoopView = findViewById(R.id.loopView_sec);



    }

    /**
     * 时间设置和倒计时布局切换
     */
    private void changeConstraint(ConstraintSet constraintSet,ConstraintLayout rootView) {
        Transition transition = new AutoTransition();
        transition.setDuration(200);
        TransitionManager.beginDelayedTransition(rootView, transition);
        constraintSet.applyTo(rootView);
    }

    /**
     * 设置view监听
     */
    private void setListener(){
        btnSetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTime();
            }
        });

        btnSetTime2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTime();
            }
        });

        btnStarTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( mMainViewMode.mStateData.getValue().equals(CountDownState.STOP)){
                    EventBus.getDefault().post(CountDownState.RESTART);
                }else if( mMainViewMode.mStateData.getValue().equals(CountDownState.START)
                        ||mMainViewMode.mStateData.getValue().equals(CountDownState.RESTART)){
                    EventBus.getDefault().post(CountDownState.STOP);
                }else{
                    Toast.makeText(MainActivity.this,"请设置倒计时时间",Toast.LENGTH_LONG).show();
                }
            }
        });
      /*  mHourLoopView.setListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                SpUtils.saveIntData(MainActivity.this,SpUtils.keyHourIndex,index);
            }
        });

        mMinLoopView.setListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                SpUtils.saveIntData(MainActivity.this,SpUtils.keyMinIndex,index);
            }
        });

        mSecLoopView.setListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                SpUtils.saveIntData(MainActivity.this,SpUtils.keySecIndex,index);
            }
        });*/
    }

    /**
     * 初始化数据
     */
    private void initData(){
        mMainViewMode = ViewModelProviders.of(this).get(MainViewMode.class);
        mMainViewMode.hourList.setValue(mMainViewMode.getHourPickerData()); // 初始化小时集合数据
        mMainViewMode.minList.setValue(mMainViewMode.getMinPickerData()); // 初始化分钟和秒的集合数据
        EventBus.getDefault().postSticky(CountDownState.INIT);// 初始化倒计时状态
        intent = new Intent();
        intent.setClass(this, CountDownService.class);
        //     startForeService(intent); // 启动服务
//        EventBus.getDefault().postSticky(new CountInfo(100)); // 设置并启动倒计时
        observer();
        restartCountDown();

    }


    private void observer(){
        mMainViewMode.countDownTime.observe(this, new Observer<Long>() {
            @Override
            public void onChanged(Long aLong) {
                // 保存当前倒计时时间
                SpUtils.saveLongData(getApplicationContext(),SpUtils.keyCurrentTime,aLong);
                //保存当前系统时间并把毫秒转化成秒
                SpUtils.saveLongData(getApplicationContext(),SpUtils.keySystemTime,(System.currentTimeMillis() / 1000));
                if(aLong == 0){
                    tvCountTime.setText("00:00:00");
                }else{
                    tvCountTime.setText(mMainViewMode.formLong2Time(aLong));
                }

            }
        });

        mMainViewMode.mStateData.observe(this, new Observer<CountDownState>() {
            @Override
            public void onChanged(CountDownState countDownState) {
                switch (countDownState){
                    case STOP:
                        btnStarTime.setText(R.string.start);
                        break;
                    case INIT:
                        btnStarTime.setText(R.string.start);
                        break;
                    case START:
                    case RESTART:
                        btnStarTime.setText(R.string.stop);
                        break;
                    case FINISH:
                        btnStarTime.setText(R.string.start);
                        stopService(intent);
                        break;
                    default:
                        break;
                }
            }
        });

/*        mMainViewMode.hourList.observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> strings) {
                // 初始化时间选择器
                mHourLoopView.setItems(strings);
                mHourLoopView.setItemsVisibleCount(7); // 设置可显示时间个数


            }
        });

        mMainViewMode.minList.observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> strings) {
                // 初始化时间选择器
                mMinLoopView.setItems(strings);
                mSecLoopView.setItems(strings);
                mMinLoopView.setItemsVisibleCount(7);
                mSecLoopView.setItemsVisibleCount(7);

            }
        });*/
    }

    private void setTime() {
        if (mMainViewMode.mStateData.getValue().equals(CountDownState.START)
                || mMainViewMode.mStateData.getValue().equals(CountDownState.RESTART)) {
            EventBus.getDefault().post(CountDownState.STOP);
        }
        showTimeSelect(SpUtils.getIntData(MainActivity.this, SpUtils.keyHourIndex)
                ,SpUtils.getIntData(MainActivity.this, SpUtils.keyMinIndex)
                ,SpUtils.getIntData(MainActivity.this, SpUtils.keySecIndex));
        /*if (tvCountTime.getVisibility() == View.VISIBLE) {
            if (mMainViewMode.mStateData.getValue().equals(CountDownState.START)
                    || mMainViewMode.mStateData.getValue().equals(CountDownState.RESTART)) {
                EventBus.getDefault().post(CountDownState.STOP);
            }
            // 初始化时间选择器
            mHourLoopView.setCurrentPosition(SpUtils.getIntData(MainActivity.this, SpUtils.keyHourIndex));
            mMinLoopView.setCurrentPosition(SpUtils.getIntData(MainActivity.this, SpUtils.keyMinIndex));
            mSecLoopView.setCurrentPosition(SpUtils.getIntData(MainActivity.this, SpUtils.keySecIndex));
            changeConstraint(mConstraintSet2, rootView);
        } else {
            //返回的分别是三个级别的选中位置
            String hour = mMainViewMode.hourList.getValue().get(SpUtils.getIntData(MainActivity.this, SpUtils.keyHourIndex));
            String min = mMainViewMode.minList.getValue().get(SpUtils.getIntData(MainActivity.this, SpUtils.keyMinIndex));
            String sec = mMainViewMode.minList.getValue().get(SpUtils.getIntData(MainActivity.this, SpUtils.keySecIndex));
            long time = Integer.valueOf(hour) * (60 * 60) + Integer.valueOf(min) * 60 + Integer.valueOf(sec);
            if (time != 0) {
                mMainViewMode.totalTime.setValue(time);
                if (mMainViewMode.mStateData.getValue() != null && mMainViewMode.mStateData.getValue().equals(CountDownState.START)) {
                    EventBus.getDefault().post(CountDownState.STOP);
                }
                EventBus.getDefault().post(new CountInfo(time));
                changeConstraint(mConstraintSet, rootView);
            } else {
                Toast.makeText(MainActivity.this, "请设置倒计时时间", Toast.LENGTH_LONG).show();
            }

        }*/

    }


    /**
     * app被强制关闭的情况下打开时通过和当前系统时间的比较恢复倒计时
     */
    private void restartCountDown(){
        long currentDownTime =  SpUtils.getLongData(getApplicationContext(),SpUtils.keyCurrentTime);
        // 主动暂停重新打开时不直接开始倒计时
        if(SpUtils.getBooleanData(MainActivity.this,SpUtils.keyStop)){
            mMainViewMode.countDownTime.setValue(currentDownTime);
            mMainViewMode.totalTime.setValue(currentDownTime);
            EventBus.getDefault().postSticky(new CountInfo(0,currentDownTime));
        }else{
            // 异常暂停时，重新打开app计算与系统时间的差值
            long lastSystemTime =  SpUtils.getLongData(getApplicationContext(),SpUtils.keySystemTime);
            // 倒计时时间大于0时判断系统时间已经过了多长时间
            if(currentDownTime > 0){
                long currentSystemTime = System.currentTimeMillis() /1000; // ms -> s
                long timeDistance = currentSystemTime - lastSystemTime;
                // 系统时间差值小于剩余时间是继续倒计时
                if(timeDistance < currentDownTime){
                    EventBus.getDefault().postSticky(new CountInfo(currentDownTime -timeDistance));
                }
                startForeService(intent);
            }

        }
    }

    /**
     * 接收倒计时当前数值
     * @param time
     */
    @Subscribe(threadMode  = ThreadMode.MAIN)
    public void onMessage(Long time){
        mMainViewMode.countDownTime.setValue(time);
    }

    /**
     * EventBus 接收计时器状态
     * @param countDownState
     */
    @Subscribe(threadMode  = ThreadMode.MAIN,sticky = true)
    public void onMessage(CountDownState countDownState){
        mMainViewMode.mStateData.setValue(countDownState);
    }

    /**
     * 启动服务
     * @param intent
     */
    private void startForeService(Intent intent){
        if(!isServiceRunning(MainActivity.this,CountDownService.class.getName())){
            // 大于26时后台启动服务需要启动前台服务
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            }else{
                startService(intent);
            }
        }


    }

    /**
     * 展示时间选择器对话框
     * @param posHour
     * @param posMin
     * @param posSec
     */
    private void showTimeSelect(int posHour,int posMin,int posSec){
//条件选择器
        OptionsPickerView pvOptions = new OptionsPickerBuilder(MainActivity.this, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3 ,View v) {
                //返回的分别是三个级别的选中位置
                SpUtils.saveIntData(MainActivity.this,SpUtils.keyHourIndex,options1);
                SpUtils.saveIntData(MainActivity.this,SpUtils.keyMinIndex,option2);
                SpUtils.saveIntData(MainActivity.this,SpUtils.keySecIndex,options3);
                long time = options1 * (60 * 60) + option2 * 60 + options3;
                if (time != 0) {
                    mMainViewMode.totalTime.setValue(time);
                    mMainViewMode.countDownTime.setValue(time);
                    if (mMainViewMode.mStateData.getValue() != null && mMainViewMode.mStateData.getValue().equals(CountDownState.START)) {
                        EventBus.getDefault().postSticky(CountDownState.STOP);
                    }
                    EventBus.getDefault().postSticky(new CountInfo(time));
                    startForeService(intent);
//                    changeConstraint(mConstraintSet, rootView);
                } else {
                    Toast.makeText(MainActivity.this, "请设置倒计时时间", Toast.LENGTH_LONG).show();
                }

            }
        }).setLabels(getString(R.string.hour),getString(R.string.min),getString(R.string.sec))
                .isDialog(false)
                .setSelectOptions(posHour, posMin, posSec)
                .setContentTextSize(30)
                .build();
        pvOptions.setNPicker(mMainViewMode.hourList.getValue(), mMainViewMode.minList.getValue(), mMainViewMode.minList.getValue());
        pvOptions.show();
    }


    /**
     * 判断服务是否开启
     *
     * @return
     */
    public  boolean isServiceRunning(Context context, String ServiceName) {
        if (TextUtils.isEmpty(ServiceName)) {
            return false;
        }
        ActivityManager myManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager.getRunningServices(1000);
        for (int i = 0; i < runningService.size(); i++) {
            Log.i("服务运行1：", "" + runningService.get(i).service.getClassName().toString());
            if (runningService.get(i).service.getClassName().toString().equals(ServiceName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        stopService(intent);


    }
}
