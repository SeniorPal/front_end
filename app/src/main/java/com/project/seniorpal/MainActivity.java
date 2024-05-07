package com.project.seniorpal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.ChatCompletionRequest;
import com.zhipu.oapi.service.v4.model.ChatCompletionRequestMixIn;
import com.zhipu.oapi.service.v4.model.ChatFunction;
import com.zhipu.oapi.service.v4.model.ChatFunctionCall;
import com.zhipu.oapi.service.v4.model.ChatFunctionCallMixIn;
import com.zhipu.oapi.service.v4.model.ChatFunctionMixIn;
import com.zhipu.oapi.service.v4.model.ChatMessage;
import com.zhipu.oapi.service.v4.model.ChatMessageAccumulator;
import com.zhipu.oapi.service.v4.model.ChatMessageRole;
import com.zhipu.oapi.service.v4.model.ModelApiResponse;
import com.zhipu.oapi.service.v4.model.ModelData;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Flowable;


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

    private static final String API_KEY = "d5dacc4004179f93decc2dc575684063.6SWx2S0VOptED02R";

    private ClientV4 client;

    private ObjectMapper mapper;

    private List<ChatMessage> messages;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();

        initializeViews();
        initZhipuAIClient();
        setupListeners();
        startForegroundService(this);  // 在适当的位置调用以启动前台服务
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

        View emptyView = findViewById(R.id.view_bg_empty);
        emptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    private void setupListeners() {
        sendButton.setOnClickListener(v -> {
            String userInputText = userInput.getText().toString();
            if (!userInputText.isEmpty()) {
                addUserMessage(userInputText);
                userInput.setText(""); // Clear input after sending
            }
            // Chat with LLM
            chatWithAssistant(userInputText);
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

    /**
     * Initialize the Zhipu AI client
     */
    private void initZhipuAIClient() {
        client = new ClientV4.Builder(API_KEY).build();
        mapper = defaultObjectMapper();
        messages = new ArrayList<>();
    }

    private void chatWithAssistant(String userInput) {
        TextView assMsg = addAssistantMessage("Please wait");
        ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), "你好");
        messages.add(chatMessage);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(false)
                .messages(messages)
                .invokeMethod(Constants.invokeMethod)
                .build();
        ModelApiResponse modelApiResponse = client.invokeModelApi(chatCompletionRequest);
//        if (modelApiResponse.isSuccess()) {
//            try {
//                assMsg.setText(mapper.writeValueAsString(modelApiResponse));
//            } catch (JsonProcessingException e) {
//                assMsg.setText(e.getMessage());
//                assMsg.setTextColor(Color.RED);
//
//            }
//        }
//        ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), userInput);
//        messages.add(chatMessage);
//        System.out.println(messages.size());
//        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
//                .model(Constants.ModelChatGLM4)
//                .stream(false)
//                .messages(messages)
//                .invokeMethod(Constants.invokeMethod)
//                .build();
//        TextView assMsg = addAssistantMessage("Please wait");
//        ModelApiResponse modelApiResponse = client.invokeModelApi(chatCompletionRequest);
//        if (modelApiResponse.isSuccess()) {
//            try {
//                assMsg.setText(mapper.writeValueAsString(modelApiResponse));
////                System.out.println("model output:" + mapper.writeValueAsString(modelApiResponse));
//            } catch (JsonProcessingException e) {
//                assMsg.setText(e.getMessage());
//                assMsg.setTextColor(Color.RED);
//            }
////            AtomicBoolean isFirst = new AtomicBoolean(true);
////            ChatMessageAccumulator chatMessageAccumulator = mapStreamToAccumulator(modelApiResponse.getFlowable())
////                    .doOnNext(accumulator -> {
////                        if (isFirst.getAndSet(false)) {
////                            assMsg.setText("");
////                        }
////                        if (accumulator.getDelta() != null && accumulator.getDelta().getTool_calls() != null) {
////                            String json = mapper.writeValueAsString(accumulator.getDelta().getTool_calls());
////                            System.out.println("Tool calls: " + json);
////                        }
////                        if (accumulator.getDelta() != null && accumulator.getDelta().getContent() != null) {
////                            assMsg.append(accumulator.getDelta().getContent());
////                        }
////                    })
////                    .lastElement()
////                    .blockingGet();
//////            Choice choice = new Choice(chatMessageAccumulator.getChoice().getFinishReason(), 0L, chatMessageAccumulator.getDelta());
//////            List<Choice> choices = new ArrayList<>();
//////            choices.add(choice);
//////            ModelData data = new ModelData();
//////            data.setChoices(choices);
//////            data.setUsage(chatMessageAccumulator.getUsage());
//////            data.setId(chatMessageAccumulator.getId());
////            modelApiResponse.setFlowable(null);
//////            modelApiResponse.setData(data);
//        }
//        else {
//            assMsg.setText("Error: " + modelApiResponse.getMsg());
//            assMsg.setTextColor(Color.RED);
//        }
    }

    /**
     * Default object mapper for JSON serialization and deserialization
     *
     * @return ObjectMapper
     */
    private static ObjectMapper defaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        mapper.addMixIn(ChatFunction.class, ChatFunctionMixIn.class);
        mapper.addMixIn(ChatCompletionRequest.class, ChatCompletionRequestMixIn.class);
        mapper.addMixIn(ChatFunctionCall.class, ChatFunctionCallMixIn.class);
        return mapper;
    }

    /**
     * Map the stream of ModelData objects to a stream of ChatMessageAccumulator objects
     *
     * @param flowable Flowable<ModelData>
     * @return Flowable<ChatMessageAccumulator>
     */
    private static Flowable<ChatMessageAccumulator> mapStreamToAccumulator(Flowable<ModelData> flowable) {
        return flowable.map(chunk -> {
            return new ChatMessageAccumulator(chunk.getChoices().get(0).getDelta(), null, chunk.getChoices().get(0), chunk.getUsage(), chunk.getCreated(), chunk.getId());
        });
    }

    private TextView addUserMessage(String message) {
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
        return userMessage;
    }

    private TextView addAssistantMessage(String message) {
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
        return assistantMessage;
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
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopForegroundService(this);  // 在适当的位置调用以停止前台服务
    }

    public void startForegroundService(Context context) {
        Intent serviceIntent = new Intent(context, ForegroundService.class);
        context.startForegroundService(serviceIntent);
    }

    public void stopForegroundService(Context context) {
        Intent serviceIntent = new Intent(context, ForegroundService.class);
        context.stopService(serviceIntent);
    }
}
