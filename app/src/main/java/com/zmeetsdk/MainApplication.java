package com.zmeetsdk;

import com.abcpen.meet.ZMeetSDK;
import com.abcpen.meet.utils.ZMD5Util;
import com.blankj.utilcode.util.LogUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import androidx.multidex.MultiDexApplication;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainApplication extends MultiDexApplication {
    private static final String TAG = "MainApplication";
    private Gson gson = new Gson();

    @Override
    public void onCreate() {
        super.onCreate();
        ZMeetSDK.initialize(this, this::getFederationToken);
    }


    /**
     * 获取token
     *
     * @return
     * @throws IOException
     */
    public String getFederationToken() {
        try {
            String urlPath = "https://meetingtest.abcpen.com/api/meeting/getAccessToken?userId=" + MeetDebugCons.UID;
            String appid = appid;
            String appsecr = appsecr;
            String nonceStr = "123456";
            String singPub = "appId=" + appid + "&expireTime=-1" + "&nonceStr=" + nonceStr;
            String s = (ZMD5Util.getMD5String(ZMD5Util.getMD5String(singPub) + "&appSecret=" + appsecr)).toUpperCase();
            HashMap<String, String> paramMap = new HashMap<>();
            paramMap.put("appId", appid);
            paramMap.put("sign", s);
            paramMap.put("nonceStr", nonceStr);
            paramMap.put("timestamp", System.currentTimeMillis() + "");

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(45, TimeUnit.SECONDS)
                    .writeTimeout(55, TimeUnit.SECONDS)
                    .build();

            Request.Builder builder = new Request.Builder();
            paramMap.forEach((s1, v) -> builder.addHeader(s1, v));
            Request build = builder.url(urlPath).build();

            Response execute = okHttpClient.newCall(build).execute();
            String string = execute.body().string();
            Gson gson = new Gson();
            UserTokenResp userTokenResp = gson.fromJson(string, UserTokenResp.class);
            LogUtils.dTag(TAG, "getFederationToken: ", string);
            return userTokenResp.data;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
