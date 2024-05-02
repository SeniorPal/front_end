package com.project.seniorpal;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.baidu.aip.asrwakeup3.core.mini.AutoCheck;
import com.baidu.aip.asrwakeup3.core.recog.MyRecognizer;
import com.baidu.aip.asrwakeup3.core.util.AuthUtil;
import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;

import org.json.JSONObject;

import java.util.Map;

public class RecognizerUtil {

    protected static MyRecognizer myRecognizer;

    protected static EventManager asr;
    protected static String asrJson = "";

    public static void initAsr(Context mContext, EventListener listener) {
        asr = EventManagerFactory.create(mContext, "asr");
        // 基于sdk集成1.3 注册自己的输出事件类
        asr.registerListener(listener); //  EventListener 中 onEvent方法
        Map<String, Object> params = AuthUtil.getParam();
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        (new AutoCheck(mContext, new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainErrorMessage(); // autoCheck.obtainAllMessage();
                        ; // 可以用下面一行替代，在logcat中查看代码
                        // Log.w("AutoCheckMessage", message);
                    }
                }
            }
        }, false)).checkAsr(params);
         // 可以替换成自己的json
        asrJson = new JSONObject(params).toString(); // 这里可以替换成你需要测试的json
//        asr.send(event, json, null, 0, 0);
    }

    public static void startAsr(){
        asr.send(SpeechConstant.ASR_START, asrJson, null, 0, 0);
    }

    public static void stopAsr(){
        asr.send(SpeechConstant.ASR_STOP, null, null, 0, 0);
    }

    public static void cancelAsr(){
        asr.send(SpeechConstant.ASR_CANCEL, null, null, 0, 0);
    }
    public static void cancelAsr(EventListener listener){
        asr.unregisterListener(listener);
    }



}
