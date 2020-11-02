package com.zmeetsdk;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.abcpen.meet.ZMeetSDK;
import com.abcpen.meet.listener.ZRtcEngineEventHandler;
import com.abcpen.meet.rtc.ZRtcEngine;
import com.abcpen.meet.view.ZMeetRendererView;
import com.abcpen.meet.view.ZMeetVideoCanvas;
import com.blankj.utilcode.util.LogUtils;
import com.jakewharton.rxbinding4.view.RxView;
import com.tbruyelle.rxpermissions3.RxPermissions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import kotlin.Unit;

public class ZMeetSimpleActivity extends AppCompatActivity implements VideoAdapter.OnSelectListener {

    public static final String MEETING_ID = "MEETING_ID";
    private static final String TAG = "MainActivity";
    private FrameLayout frameLayout;
    private ZRtcEngine rtcEngine;
    private ZMeetVideoCanvas zMeetVideoCanvas;
    private boolean isMirror = false;
    private RecyclerView recyclerView;
    private VideoAdapter videoAdapter;
    private List<VideoAdapter.UserItem> userItemList = new ArrayList<>();
    private BtnViewHolder viewHolder;
    private String meetingId;


    public static void startMeeting(Context context, String meetingId) {
        Intent intent = new Intent(context, ZMeetSimpleActivity.class);
        intent.putExtra(MEETING_ID, meetingId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_meet);
        ZMeetSDK.attachActivity(this);
        meetingId = getIntent().getStringExtra(MEETING_ID);
        frameLayout = findViewById(R.id.fm_video);
        rtcEngine = ZMeetSDK.createRtcEngine(zRtcEngineEventHandler);
        recyclerView = findViewById(R.id.recyclerView);
        initView();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        rtcEngine.destroy();
        ZMeetSDK.detachedActivity(this);
    }


    public class BtnViewHolder {
        private boolean isConnect = false;
        private boolean isOpenCamera = false;
        private boolean isMuteAudio = false;

        private Button btnCamera;
        private Button btnConnect;
        private Button btnMuteAudio;
        private Button btnSpeakerOn;

        public BtnViewHolder() {
            btnSpeakerOn = findViewById(R.id.btn_audio_mode);
            btnCamera = findViewById(R.id.btn_openCamera);
            btnConnect = findViewById(R.id.btn_connection);
            btnMuteAudio = findViewById(R.id.btn_mute_audio);
            RxPermissions rxPermissions = new RxPermissions(ZMeetSimpleActivity.this);
            RxView.clicks(btnCamera)
                    .throttleFirst(1, TimeUnit.SECONDS)
                    .compose(rxPermissions.ensure(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                    .subscribe(this::onOpenCameraClick);
            RxView.clicks(btnMuteAudio)
                    .throttleFirst(300, TimeUnit.MILLISECONDS)
                    .subscribe(this::onMuteAudioClick);
            RxView.clicks(btnConnect)
                    .throttleFirst(1, TimeUnit.SECONDS)
                    .subscribe(this::onConnectionClick);
            RxView.clicks(btnSpeakerOn)
                    .throttleFirst(1, TimeUnit.SECONDS)
                    .subscribe(this::onSpeakerOnClick);
            setSpeakerOnText();
        }

        private void setSpeakerOnText() {
            btnSpeakerOn.setText(rtcEngine.isSpeakerOn() ? "听筒" : "扬声器");
        }

        private void onSpeakerOnClick(Unit unit) {
            rtcEngine.setSpeakerOn(!rtcEngine.isSpeakerOn());
            setSpeakerOnText();
        }

        private void onConnectionClick(Unit unit) {
            if (isConnect) {
                rtcEngine.disConnect();
            } else {
                rtcEngine.connect(
                        meetingId,
                        MeetDebugCons.NAME, MeetDebugCons.UID);
            }

        }


        private void onMuteAudioClick(Unit unit) {
            isMuteAudio = !isMuteAudio;
            rtcEngine.muteLocalAudio(isMuteAudio);
            if (isMuteAudio) {
                btnMuteAudio.setText("取消静音");
            } else {
                btnMuteAudio.setText("静音");
            }
        }

        private void onOpenCameraClick(Boolean aBoolean) {
            if (aBoolean) {
                if (isOpenCamera) {
                    rtcEngine.disableLocalVideo();
                } else {
                    rtcEngine.setupLocalVideo(zMeetVideoCanvas);
                    unSelectLast();
                }
            }
        }

        public void setCameraOpen(boolean b) {
            isOpenCamera = b;
            if (isOpenCamera) {
                btnCamera.setText("关闭摄像头");
            } else {
                btnCamera.setText("开启摄像头");
            }
        }

        public void onConnected() {
            isConnect = true;
            btnConnect.setText("断开连接");
        }

        public void onDisConnected() {
            isConnect = false;
            btnConnect.setText("连接");
        }
    }

    private void initView() {
        viewHolder = new BtnViewHolder();
        userItemList.add(new VideoAdapter.UserItem(MeetDebugCons.UID, MeetDebugCons.NAME, false));
        videoAdapter = new VideoAdapter(userItemList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        recyclerView.setAdapter(videoAdapter);

        ZMeetRendererView rendererView = rtcEngine.createRendererView(this);
        zMeetVideoCanvas = new ZMeetVideoCanvas(rendererView, ZMeetVideoCanvas.SCALE_ASPECT_FIT);
        frameLayout.addView(zMeetVideoCanvas);
    }


    public void onSwitchClick(View view) {
        LogUtils.dTag(TAG, "onSwitchClick: ");
        rtcEngine.switchCamera();
    }

    public void onMirrorClick(View view) {
        if (zMeetVideoCanvas != null) {
            isMirror = !isMirror;
            zMeetVideoCanvas.setMirror(isMirror);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void unSelectLast() {
        userItemList.stream().filter(VideoAdapter.UserItem::isSelect)
                .findFirst().ifPresent(userItem -> {
            int i = userItemList.indexOf(userItem);
            userItem.setSelect(false);
            videoAdapter.notifyItemChanged(i);
            zMeetVideoCanvas.setStreamURL("");
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onItemSelect(int index) {
        VideoAdapter.UserItem userItem = userItemList.get(index);
        if (userItem.isHasVideo()) {
            unSelectLast();
            userItem.setSelect(true);
            videoAdapter.notifyItemChanged(index);
            if (userItem.isLocal()) {
                rtcEngine.setupLocalVideo(zMeetVideoCanvas);
            } else {
                rtcEngine.setupRemoteVideo(zMeetVideoCanvas, userItem.getUid());
            }
        }
    }


    public ZRtcEngineEventHandler zRtcEngineEventHandler = new ZRtcEngineEventHandler() {
        @Override
        public void onWarn(int warn) {
            LogUtils.dTag(TAG, "onWarn: ", "warn = [" + warn + "]");
        }

        @Override
        public void onConnectionSuccess() {
            LogUtils.dTag(TAG, "onConnectionSuccess: ");
            viewHolder.onConnected();
            rtcEngine.joinMeet();
        }

        @Override
        public void onError(int error) {
            LogUtils.dTag(TAG, "onError: ", "error = [" + error + "]");
        }

        @Override
        public void onJoinRoomSuccess(String rid, String uid) {
            LogUtils.dTag(TAG, "onJoinRoomSuccess: ", "rid = [" + rid + "], uid = [" + uid + "]");
            rtcEngine.openMic();
        }

        @Override
        public void onLeaveRoom(String rid, String uid) {
            LogUtils.dTag(TAG, "onLeaveRoom: ", "rid = [" + rid + "], uid = [" + uid + "]");
        }

        @Override
        public void onUserJoined(String uid, String name) {
            LogUtils.dTag(TAG, "onUserJoined: ", "uid = [" + uid + "], name = [" + name + "]");
            VideoAdapter.UserItem userItem = new VideoAdapter.UserItem(uid, name, false);
            Optional<VideoAdapter.UserItem> first = userItemList.stream().filter(userItem1 -> TextUtils.equals(userItem1.getUid(), uid)).findFirst();
            if (first.isPresent()) {
                int i = userItemList.indexOf(first.get());
                LogUtils.dTag(TAG, "OnUserUpdate: ", "uid = [" + uid + "], name = [" + name + "]", i);
                userItemList.remove(i);
                userItemList.add(i, userItem);
                videoAdapter.notifyItemChanged(i);
            } else {
                userItemList.add(userItem);
                videoAdapter.notifyItemChanged(userItemList.size() - 1);
            }


        }


        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onUserOffline(String uid) {
            LogUtils.dTag(TAG, "onUserOffline: ", "uid = [" + uid + "]");
            userItemList.stream()
                    .filter(userItem -> TextUtils.equals(uid, userItem.getUid()))
                    .findFirst()
                    .ifPresent(userItem -> {
                        int i = userItemList.indexOf(userItem);
                        userItemList.remove(userItem);
                        videoAdapter.notifyItemRemoved(i);
                    });
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onRemoteVideoJoin(String uid) {
            LogUtils.dTag(TAG, "onRemoteVideoJoin: ", "uid = [" + uid + "]");
            userItemList.stream().filter(userItem -> TextUtils.equals(uid, userItem.getUid()))
                    .findFirst().ifPresent(userItem -> {
                userItem.setHasVideo(true);
                int i = userItemList.indexOf(userItem);
                videoAdapter.notifyItemChanged(i);
            });
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onRemoteVideoLeave(String uid) {
            LogUtils.dTag(TAG, "onRemoteVideoLeave: ", "uid = [" + uid + "]");
            userItemList.stream().filter(userItem -> TextUtils.equals(uid, userItem.getUid()))
                    .findFirst().ifPresent(userItem -> {
                userItem.setHasVideo(false);
                int i = userItemList.indexOf(userItem);
                onItemSelect(i);
                videoAdapter.notifyItemChanged(i);
            });
        }

        @Override
        public void onConnectionStateChanged(int state, int reason) {
            LogUtils.dTag(TAG, "onConnectionStateChanged: ", "state = [" + state + "], reason = [" + reason + "]");
        }

        @Override
        public void onAuthFail() {
            LogUtils.dTag(TAG, "onAuthFail: ");
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onFirstLocalVideoFrame() {
            LogUtils.dTag(TAG, "onFirstLocalVideoFrame: ");
            viewHolder.setCameraOpen(true);
            userItemList.stream().filter(userItem -> TextUtils.equals(MeetDebugCons.UID, userItem.getUid()))
                    .findFirst().ifPresent(userItem -> {
                userItem.setHasVideo(true);
                int i = userItemList.indexOf(userItem);
                if (!hasSelect()) {
                    onItemSelect(i);
                }
                videoAdapter.notifyItemChanged(i);
            });
        }

        @Override
        public void onFirstLocalAudioFrame() {
            LogUtils.dTag(TAG, "onFirstLocalAudioFrame: ");
        }

        @Override
        public void onUserMuteVideo(String uid, boolean muted) {
            LogUtils.dTag(TAG, "onUserMuteVideo: ", "uid = [" + uid + "], muted = [" + muted + "]");
            userItemList.stream().filter(userItem -> TextUtils.equals(uid, userItem.getUid()))
                    .findFirst().ifPresent(userItem -> {
                userItem.setHasVideo(!muted);
                int i = userItemList.indexOf(userItem);
                videoAdapter.notifyItemChanged(i);
            });
        }

        @Override
        public void onDisconnected() {
            LogUtils.dTag(TAG, "onDisconnected: ");
            viewHolder.onDisConnected();
            VideoAdapter.UserItem current = userItemList.get(0);
            userItemList.clear();
            userItemList.add(current);
            videoAdapter.notifyDataSetChanged();
        }

        @Override
        public void onConnectionFail() {
            viewHolder.onDisConnected();
            LogUtils.dTag(TAG, "onConnectionFail: ");
            VideoAdapter.UserItem current = userItemList.get(0);
            userItemList.clear();
            userItemList.add(current);
            videoAdapter.notifyDataSetChanged();
        }

        @Override
        public void onRemoteAudioJoin(String uid) {
            LogUtils.dTag(TAG, "onRemoteAudioJoin: ", "uid = [" + uid + "]");
        }

        @Override
        public void onRemoteAudioLeave(String uid) {
            LogUtils.dTag(TAG, "onRemoteAudioLeave: ", "uid = [" + uid + "]");
        }

        @Override
        public void onCameraClosed() {
            viewHolder.setCameraOpen(false);
            userItemList.stream().filter(userItem -> TextUtils.equals(MeetDebugCons.UID, userItem.getUid()))
                    .findFirst().ifPresent(userItem -> {
                userItem.setHasVideo(false);
                if (userItem.isSelect()) {
                    unSelectLast();
                }
                int i = userItemList.indexOf(userItem);
                videoAdapter.notifyItemChanged(i);
            });
        }

        @Override
        public void onUserMuteAudio(String uid, boolean muted) {
            LogUtils.dTag(TAG, "onUserMuteAudio: ", "uid = [" + uid + "], muted = [" + muted + "]");
        }
    };

    private boolean hasSelect() {
        for (VideoAdapter.UserItem userItem : userItemList) {
            if (userItem.isSelect()) {
                return true;
            }
        }
        return false;
    }


}
