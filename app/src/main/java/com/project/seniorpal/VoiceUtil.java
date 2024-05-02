package com.project.seniorpal;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class VoiceUtil {

    private static VoiceWakeuper mIvw;

    private static int curThresh = 1450;
    private static String resultString;

    private static Context context1;
    /**
     * 科大讯飞
     * 语音sdk
     * 初始化
     */
    public static void initKedaXun(Context context) {

        // 初始化参数构建
        StringBuffer param = new StringBuffer();
        //IflytekAPP_id为我们申请的Appid
        param.append("appid=6a094814");
        param.append(",");
        // 设置使用v5+
        param.append(SpeechConstant.ENGINE_MODE + "=" + SpeechConstant.MODE_MSC);
        SpeechUtility.createUtility(context, param.toString());
        Log.d("chen", "===在appacation中初始化=====");
    }


    public static boolean wakeup(Context context,int id){
        mIvw = null;
        mIvw = VoiceWakeuper.createWakeuper(context, null);
        mIvw = VoiceWakeuper.getWakeuper();
        context1 = context;
        if (mIvw != null) {
            // 清空参数
            mIvw.setParameter(SpeechConstant.PARAMS, null);
            // 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
            mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:" + curThresh);
            // 设置唤醒模式
            mIvw.setParameter(SpeechConstant.IVW_SST, "wakeup");
            // 设置持续进行唤醒
            mIvw.setParameter(SpeechConstant.KEEP_ALIVE, "1");
            // 设置闭环优化网络模式
            mIvw.setParameter(SpeechConstant.IVW_NET_MODE, "1");
            // 设置唤醒资源路径
            mIvw.setParameter(SpeechConstant.IVW_RES_PATH, getResource(context,id));
            // 设置唤醒录音保存路径，保存最近一分钟的音频
//            mIvw.setParameter(SpeechConstant.IVW_AUDIO_PATH,
//                    getExternalFilesDir("msc").getAbsolutePath() + "/ivw.wav");
            mIvw.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
            // 如有需要，设置 NOTIFY_RECORD_DATA 以实时通过 onEvent 返回录音音频流字节
            //mIvw.setParameter( SpeechConstant.NOTIFY_RECORD_DATA, "1" );
            // 启动唤醒
            /*	mIvw.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");*/

            mIvw.startListening(mWakeuperListener);
            return true;
        } else {
            Log.d("chen","唤醒未初始化");
            return false;
        }
    }


    private static WakeuperListener mWakeuperListener = new WakeuperListener() {

        @Override
        public void onResult(WakeuperResult result) {
            Log.d("chen", "onResult");
            RecognizerUtil.stopAsr();
//            RecognizerUtil.cancelAsr();
            try {
                String text = result.getResultString();
                JSONObject object;
                object = new JSONObject(text);
                StringBuffer buffer = new StringBuffer();
                buffer.append("【RAW】 " + text);
                buffer.append("\n");
                buffer.append("【操作类型】" + object.optString("sst"));
                buffer.append("\n");
                buffer.append("【唤醒词id】" + object.optString("id"));
                buffer.append("\n");
                buffer.append("【得分】" + object.optString("score"));
                buffer.append("\n");
                buffer.append("【前端点】" + object.optString("bos"));
                buffer.append("\n");
                buffer.append("【尾端点】" + object.optString("eos"));
                resultString = buffer.toString();
                Toast.makeText(context1,"唤醒成功",Toast.LENGTH_SHORT).show();
                RecognizerUtil.startAsr();
            } catch (JSONException e) {
                resultString = "结果解析出错";
                e.printStackTrace();
            }
            Log.e("chen",resultString);
        }

        @Override
        public void onError(SpeechError error) {
            Log.e("chen",error.toString());
        }

        @Override
        public void onBeginOfSpeech() {
        }

        @Override
        public void onEvent(int eventType, int isLast, int arg2, Bundle obj) {
            switch (eventType) {
                // EVENT_RECORD_DATA 事件仅在 NOTIFY_RECORD_DATA 参数值为 真 时返回
                case SpeechEvent.EVENT_RECORD_DATA:
                    final byte[] audio = obj.getByteArray(SpeechEvent.KEY_EVENT_RECORD_DATA);
                    Log.i("chen", "ivw audio length: " + audio.length);
                    break;
            }
        }

        @Override
        public void onVolumeChanged(int volume) {

        }
    };

    private static String getResource(Context context,int id) {
        String resPath = ResourceUtil.generateResourcePath(context, ResourceUtil.RESOURCE_TYPE.assets, "ivw/" + "6a094814" + ".jet");
        if (id==1){
            resPath =  ResourceUtil.generateResourcePath(context, ResourceUtil.RESOURCE_TYPE.assets, "ivw/" + "6a09481488" + ".jet");
        }
        Log.d("chen", "resPath: " + resPath);
        return resPath;
    }
}
