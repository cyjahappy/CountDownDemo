package com.person.countdowndemo;

import com.person.countdowndemo.status.CountDownState;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 *
 **/
public class MainViewMode extends ViewModel {
    public MutableLiveData<Long> countDownTime = new MutableLiveData<>(); // 当前显示倒计时数
    public MutableLiveData<Long> totalTime = new MutableLiveData<>(); // 总倒计时数
    public MutableLiveData<List<String>> hourList = new MutableLiveData<>();
    public MutableLiveData<List<String>> minList = new MutableLiveData<>();
    public MutableLiveData<CountDownState> mStateData = new MutableLiveData<>(); // 倒计时状态



    /**
     * 将long数据转换成时分秒"00:00:00"
     * @param time
     * @return
     */
    public String formLong2Time(long time){
        int hour = (int) (time / (60 * 60));
        int min = (int) (time %  (60 * 60) / 60);
        int sec = (int) (time % (60 * 60) % 60);
        String strHour = (hour < 10) ? ("0"+hour) : (hour+"");
        String strMin = (min < 10) ? ("0"+min) : (min+"");
        String strSec = (sec < 10) ? ("0"+sec) : (sec+"");
        return strHour +":" +strMin +":"+ strSec;
    }


    /**
     * 获取分钟和秒的数据集合
     * @return
     */
    public List<String> getMinPickerData(){
        List<String> dataList = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            if(i < 10){
                dataList.add("0"+i);
            }else{
                dataList.add(i+"");
            }
        }
        return dataList;
    }

    /**
     * 获取小时的数据集合
     * @return
     */
    public List<String> getHourPickerData(){
        List<String> dataList = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            if(i < 10){
                dataList.add("0"+i);
            }else{
                dataList.add(i+"");
            }
        }
        return dataList;
    }
}
