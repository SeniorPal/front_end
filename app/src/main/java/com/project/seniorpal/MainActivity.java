package com.project.seniorpal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.client.OpenAiApi;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import org.json.JSONException;
import org.json.JSONObject;
import org.reactivestreams.Subscription;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;
import kotlinx.coroutines.flow.Flow;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;

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

    private static final String OPENAI_BASE = "api.openai.com/v1";

    private static final String ZHIPU_AI_BASE = "open.bigmodel.cn/api/paas/v4";

    private static final String API_KEY = "d5dacc4004179f93decc2dc575684063.6SWx2S0VOptED02R";

    private OpenAiService openAiService;

    private List<ChatMessage> messages;

    private Handler handler;

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
//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);
        handler = new Handler();
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
        // Initialize the OpenAI Service
        // Ref: https://github.com/TheoKanning/openai-java?tab=readme-ov-file#customizing-openaiservice
        ObjectMapper mapper = OpenAiService.defaultObjectMapper();
        OkHttpClient client = OpenAiService.defaultClient(API_KEY, Duration.ofSeconds(30))
                .newBuilder()
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    if (request.url().url().toString().contains(OPENAI_BASE)) {
                        request = request.newBuilder()
                                .url(request.url().url().toString().replace(OPENAI_BASE, ZHIPU_AI_BASE))
                                .build();
                    }
                    return chain.proceed(request);
                }).build();
        Retrofit retrofit = OpenAiService.defaultRetrofit(client, mapper);
        OpenAiApi api = retrofit.create(OpenAiApi.class);
        openAiService = new OpenAiService(api);

        // Initialize the messages list
        messages = new ArrayList<>();
    }

    private void chatWithAssistant(String userInput) {
        TextView assMsg = addAssistantMessage("Please wait");
        new Single<ChatMessage>() {
            @Override
            protected void subscribeActual(SingleObserver<? super ChatMessage> observer) {
                ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), userInput);
                messages.add(chatMessage);
                ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                        .builder()
                        .model("glm-4")
                        .messages(messages)
                        .build();
                ChatMessage respMsg = openAiService.createChatCompletion(chatCompletionRequest)
                        .getChoices().get(0).getMessage();
                observer.onSuccess(respMsg);
            }
        }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doAfterSuccess(message -> {
            messages.add(message);
            assMsg.setText(message.getContent());
        })
        .doOnError(throwable -> {
            Log.e("Chat", "An error occurred: " + throwable.getMessage());
            assMsg.setText("An error occurred: " + throwable.getMessage());
            assMsg.setTextColor(Color.RED);
        }).subscribe();
    }

    @SuppressLint("CheckResult")
    private void chatWithAssistantStream(String userInput) {
        ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), userInput);
        messages.add(chatMessage);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("glm-4")
                .messages(messages)
                .build();
        TextView assMsg = addAssistantMessage("Please wait");
        AtomicBoolean isFirst = new AtomicBoolean(true);
        Flowable<ChatCompletionChunk> flowable = openAiService.streamChatCompletion(chatCompletionRequest)
                .subscribeOn(Schedulers.io());
        openAiService.mapStreamToAccumulator(flowable)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .doOnNext(accumulator -> {
                    if (accumulator.isFunctionCall()) {
                        if (isFirst.getAndSet(false)) {
                            System.out.println("Executing function " + accumulator.getAccumulatedChatFunctionCall().getName() + "...");
                        }
                    } else {
                        if (isFirst.getAndSet(false)) {
                            Log.i("Chat", "Cleared message hint");
                            handler.post(() -> assMsg.setText(""));
                        }
                        if (accumulator.getMessageChunk().getContent() != null) {
                            Log.i("Chat", "Received chunk: " + accumulator.getMessageChunk().getContent());
                            handler.post(() -> assMsg.append(accumulator.getMessageChunk().getContent()));
                        }
                    }
                })
                .doOnComplete(() -> {
                    Log.i("Chat", "Chat completed");
                })
                .lastElement()
                .subscribeOn(Schedulers.io())
                .doAfterSuccess(accumulator -> {
                    handler.post(() -> messages.add(accumulator.getAccumulatedMessage()));
                }).subscribe();
//        openAiService.streamChatCompletion(chatCompletionRequest)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .doOnSubscribe(subscription -> {
//                    Log.i("Chat", "Chat started");
//                })
//                .doOnNext(chunk -> {
//                    if (isFirst.getAndSet(false)) {
//                        assMsg.setText("");
//                    }
//                    Log.i("Chat", "Received chunk: " + chunk.getChoices().get(0).getMessage().getContent());
//                    assMsg.append(chunk.getChoices().get(0).getMessage().getContent());
//                })
//                .doOnError(throwable -> {
//                    Log.e("Chat", "An error occurred: " + throwable.getMessage());
//                    assMsg.setText("An error occurred: " + throwable.getMessage());
//                })
//                .doOnComplete(() -> {
//                    Log.i("Chat", "Chat completed");
//                }).subscribe();
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
