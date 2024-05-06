package com.project.seniorpal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.baidu.speech.EventListener;
import com.baidu.speech.asr.SpeechConstant;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements EventListener {

    private boolean isEnd = false;
    private boolean isClose = true;
    private RadioButton chinese, english;
    private RadioGroup language;

    private EditText userInput;
    private ScrollView scrollView;
    private LinearLayout chatList;
    private ImageButton sendButton;

    private int id;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        userInput = findViewById(R.id.et_user_input);
        chinese = findViewById(R.id.chinese);
        english = findViewById(R.id.english);
        language = findViewById(R.id.language);
        scrollView = findViewById(R.id.sv_chat_list);
        chatList = findViewById(R.id.ll_chat_list);
        sendButton = findViewById(R.id.bt_send);

        CardView settingsButton = findViewById(R.id.cv_settings);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void setupListeners() {
        sendButton.setOnClickListener(v -> {
            String userInputText = userInput.getText().toString();
            if (!userInputText.isEmpty()) {
                addUserMessage(userInputText);
                userInput.setText(""); // Clear input after sending
            }
        });

        language.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chinese) {
                id = 0;
            } else {
                id = 1;
            }
            VoiceUtil.wakeup(getApplicationContext(), id);
        });

        if (language.getCheckedRadioButtonId() == R.id.chinese) {
            id = 0;
        } else {
            id = 1;
        }
    }

    private void addUserMessage(String message) {
        TextView userMessage = new TextView(this);
        userMessage.setText(message);
        userMessage.setTextColor(getResources().getColor(R.color.black)); // Assuming you have a color resource for text
        userMessage.setBackgroundResource(R.drawable.user_message_background); // Assuming you have a drawable for message background
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.END;
        params.topMargin = dpToPx(8); // Add top margin
        userMessage.setLayoutParams(params);
        chatList.addView(userMessage);
        scrollToEnd();
    }

    private void addAssistantMessage(String message) {
        TextView assistantMessage = new TextView(this);
        assistantMessage.setText(message);
        assistantMessage.setTextColor(getResources().getColor(R.color.black));
        assistantMessage.setBackgroundResource(R.drawable.assistant_message_background);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.START;
        params.topMargin = dpToPx(8); // Add top margin
        assistantMessage.setLayoutParams(params);
        chatList.addView(assistantMessage);
        scrollToEnd();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }



    private void scrollToEnd() {
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    @Override
    public void onEvent(String name, String params, byte[] data, int offset, int length) {
        String logTxt = "name: " + name;
        if (!isEnd && name.equals("asr.end")) {
            isEnd = true;
        }
        if (isEnd && name.equals("asr.partial")) {
            isEnd = false;
            logTxt += " ;params :" + params;
            try {
                JSONObject jsonObject = new JSONObject(params);
                String results_recognition = jsonObject.getString("best_result");
//                textView.setText("唤醒后语音识别结果"+"\n"+results_recognition);
                userInput.setText(results_recognition);
                RecognizerUtil.stopAsr();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_EXIT)) {
            if (isClose==false){
                Log.e("message1", "CALLBACK_EVENT_ASR_EXIT");
            }

        }


        if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
            if (params != null && params.contains("\"nlu_result\"")) {
                if (length > 0 && data.length > 0) {
                    try {
                        JSONObject allJson = new JSONObject(params);
                        String reslut = allJson.optString("best_result");
//                        textView.setText(reslut);
                        userInput.setText(reslut);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    logTxt += ", 语义解析结果：" + new String(data, offset, length);
                }
            }
        } else if (data != null) {
            logTxt += " ;data length=" + data.length;
        }
        Log.e("message1chen", logTxt);
    }

    @SuppressLint("InlinedApi")
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.RECORD_AUDIO,
                            android.Manifest.permission.ACCESS_NETWORK_STATE,
                            android.Manifest.permission.INTERNET,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            android.Manifest.permission.ACCESS_WIFI_STATE,
                            android.Manifest.permission.CHANGE_NETWORK_STATE,
                            android.Manifest.permission.READ_PHONE_STATE,
                            android.Manifest.permission.READ_CONTACTS,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.WRITE_SETTINGS,
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.SEND_SMS,
                            android.Manifest.permission.RECEIVE_SMS},
                    6);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        VoiceUtil.initKedaXun(getApplicationContext());
        VoiceUtil.wakeup(getApplicationContext(), id);
        RecognizerUtil.initAsr(getApplicationContext(), this);
    }
}
