package com.example.wp.botsdktest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.duer.botsdk.BotIdentity;
import com.baidu.duer.botsdk.BotIntent;
import com.baidu.duer.botsdk.BotSdk;
import com.baidu.duer.botsdk.IBotMessageListener;
import com.baidu.duer.botsdk.UiContextPayload;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;

public class MainActivity extends Activity implements View.OnClickListener {
    public static final String TAG = "BotSdk";

    private TextView mStatusView;
    private ImageView mLight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BotSdk.getInstance().init(this.getApplication());

        setContentView(R.layout.activity_main);

        mStatusView = findViewById(R.id.status);
        mLight = findViewById(R.id.light);
        findViewById(R.id.bind).setOnClickListener(this);
        findViewById(R.id.test).setOnClickListener(this);
        findViewById(R.id.listen).setOnClickListener(this);
        findViewById(R.id.sendClientContext).setOnClickListener(this);
    }


    public String sign(String rand){
        String key = "abcdefghijk12345";
        return getMD5(rand+key);
    }

    private static String bytesToHex1(byte[] md5Array) {
        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < md5Array.length; i++) {
            int temp = 0xff & md5Array[i];//TODO:此处为什么添加 0xff & ？
            String hexString = Integer.toHexString(temp);
            if (hexString.length() == 1) {//如果是十六进制的0f，默认只显示f，此时要补上0
                strBuilder.append("0").append(hexString);
            } else {
                strBuilder.append(hexString);
            }
        }
        return strBuilder.toString();
    }
    private static String getMD5(String info) {
        try {
            //获取 MessageDigest 对象，参数为 MD5 字符串，表示这是一个 MD5 算法（其他还有 SHA1 算法等）：
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            //update(byte[])方法，输入原数据
            //类似StringBuilder对象的append()方法，追加模式，属于一个累计更改的过程
            md5.update(info.getBytes("UTF-8"));
            //digest()被调用后,MessageDigest对象就被重置，即不能连续再次调用该方法计算原数据的MD5值。可以手动调用reset()方法重置输入源。
            //digest()返回值16位长度的哈希值，由byte[]承接
            byte[] md5Array = md5.digest();
            //byte[]通常我们会转化为十六进制的32位长度的字符串来使用,本文会介绍三种常用的转换方法
            return bytesToHex1(md5Array);
        } catch (NoSuchAlgorithmException e) {
            return "";
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bind:
                if (!BotSdk.getInstance().isRegister()) {
                    String rand1="hongyang"+Math.random();
                    String rand2= "yanghong"+Math.random();
                    String botId="3fcc17e3-7e97-b9ec-84cd-5211f6271394";
                    BotSdk.getInstance().register(messageListener,
                            botId,
//                            "f5b129dc-94ac-92b0-3d79-5469d1facf7f",
                            rand1,
                            sign(rand1),
                            rand2,
                            sign(rand2));
                } else {
                    mStatusView.setText("注册成功，可以开始语音交互");
                }
                break;
            case R.id.test:
                BotSdk.getInstance().speak("你点击了试一试按钮", false);
                break;
            case R.id.listen:
                BotSdk.getInstance().listen();
                break;
            case R.id.sendClientContext:
                updateUiContext();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        findViewById(R.id.bind).performClick();
        mLight.setImageDrawable(mLight.getDrawable());
    }

    @Override
    protected void onPause() {
        super.onPause();
        BotSdk.getInstance().updateUiContext(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BotSdk.getInstance().unregister();
    }

    private void updateUiContext() {
        UiContextPayload payload = new UiContextPayload();
        //String[] words = {"试一试", "点击试一试"};
        HashMap<String, String> params;
        params = new HashMap<>();
        params.put("name", "地址");
        params.put("type", "city");
        payload.addHyperUtterance("sdkdemo://input",
                null,
                "input",
                params);

        params = new HashMap<>();
        params.put("name", "试一试");
        payload.addHyperUtterance("sdkdemo://clicktest",
                null, "link",
                params);

        BotSdk.getInstance().updateUiContext(payload);
    }

    private IBotMessageListener messageListener = new IBotMessageListener() {
        @Override
        public void onHandleIntent(String token, BotIdentity identity, BotIntent intent,
                                   String customData) {
            Log.d("BotSdk", "onHandleIntent:" + token + "|" + identity
                    .accessToken + "|" + intent.name + "|" + intent.slots + "|" +
                    customData);

            if ("open_light".equals(intent.name )) {
                if (intent.slots!=null  && intent.slots.size() > 0 && intent.slots.get(0).value.equals("黄灯")) {
                    mLight.setImageDrawable(getResources().getDrawable(R.drawable.light2));
                } else {
                    mLight.setImageDrawable(getResources().getDrawable(R.drawable.light3));
                }
            } else if ("close_light".equals(intent.name)) {
                mLight.setImageDrawable(getResources().getDrawable(R.drawable.light));
            }

        }

        @Override
        public void onClickLink(String url, HashMap<String, String> paramMap) {
            Log.d(TAG, "onClickLink: " + url + " , " + paramMap);
            if("sdkdemo://clicktest".equals(url)){
                findViewById(R.id.test).performClick();
            }

            if("sdkdemo://input".equals(url)){
                ((TextView)findViewById(R.id.input)).setText(paramMap.get("content"));
            }

            Toast.makeText(MainActivity.this, "url = " + url, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onHandleScreenNavigatorEvent(int event) {

            String msg = "";

            switch (event) {
                case IBotMessageListener.NAV_SCROLL_LEFT:
                    msg = "NAV_SCROLL_LEFT";
                    break;
                case IBotMessageListener.NAV_SCROLL_RIGHT:
                    msg = "NAV_SCROLL_RIGHT";
                    break;
                case IBotMessageListener.NAV_SCROLL_UP:
                    msg = "NAV_SCROLL_UP";
                    break;
                case IBotMessageListener.NAV_SCROLL_DOWN:
                    msg = "NAV_SCROLL_DOWN";
                    break;
                case IBotMessageListener.NAV_NEXT_PAGE:
                    msg = "NAV_NEXT_PAGE";
                    break;
                case IBotMessageListener.NAV_PREVIOUS_PAGE:
                    msg = "NAV_PREVIOUS_PAGE";
                    break;
                case IBotMessageListener.NAV_GO_HOMEPAGE:
                    msg = "NAV_GO_HOMEPAGE";
                    break;
                case IBotMessageListener.NAV_GO_BACK:
                    msg = "NAV_GO_BACK";
                    break;
                default:
                    break;
            }
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCloseRequested() {
            Log.d(TAG, "onCloseRequested: ");
            // System.exit(0);
        }

        @Override
        public void onConnect() {
            Log.d(TAG, "onConnect");
            mStatusView.setText("已连接");
        }

        @Override
        public void onDisconnect() {
            Log.d(TAG, "onDisconnect");
            mStatusView.setText("已断开连接");
        }

        @Override
        public void onRegisterFailed(int status) {
            mStatusView.setText("注册失败");
            Log.d(TAG, "onRegisterFailed");
        }

        @Override
        public void onRegisterSucceed() {
            Log.d(TAG, "onRegisterSucceed");
            mStatusView.setText("注册成功，可以开始语音交互");
        }
    };
}
