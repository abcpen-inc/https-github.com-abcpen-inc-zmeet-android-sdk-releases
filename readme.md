### ZMeetSDK

#### 初始化

  ```java
ZMeetSDK.initialize(this, new ZMeetCredentialProvider(){
 						@Override
            public String getFederationToken() {
                // todo 获取授权token
                return null;
            }
 
})
  ```



#### 关联上下文

```java
 onCreate
 ZMeetSDK.attachActivity(this);
 
 onDestroy
 ZMeetSDK.detachedActivity(this);
```



#### 连接会议

```java
//创建RtcEngine
 ZMeetSDK.createRtcEngine(zRtcEngineEventHandler);

//连接会议
rtcEngine.connect(String meetingId, String unmae, String uid)
  
// 加入会议
rtcEngine.joinMeet();
```

| ZRtcEngineEventHandler   | ---            | ---  |
| ------------------------ | -------------- | ---- |
| onConnectionSuccess      | 建立连接       |      |
| onConnectionStateChanged | 连接状态变更   |      |
| onConnectionFail         | 连接失败       |      |
| onDisconnected           | 连接断开       |      |
| onAuthFail               | 认证失败       |      |
| onJoinRoomSuccess        | 加入房间       |      |
| onLeaveRoom              | 离开房间       |      |
| onUserJoined             | 用户加入房间   |      |
| onUserOffline            | 用户离开房间   |      |
| onRemoteVideoJoin        | 远端视频加入   |      |
| onRemoteVideoLeave       | 远端视频关闭   |      |
| onRemoteAudioJoin        | 远端音频加入   |      |
| onRemoteAudioLeave       | 远端音频关闭   |      |
| onUserMuteAudio          | 取消静音/静音  |      |
| onFirstLocalVideoFrame   | 本地视频第一帧 |      |
| onFirstLocalAudioFrame   | 本地音频第一帧 |      |
| onCameraClosed           | 摄像头关闭     |      |
| onWarn                   | 警告code       |      |
| onError                  | 错误码         |      |



#### ZRtcEngine

| ZRtcEngine         | ---                                               | ---  |
| ------------------ | ------------------------------------------------- | ---- |
| create             | 创建实例                                          |      |
| connect            | 建立连接                                          |      |
| muteLocalAudio     | 静音、取消静音                                    |      |
| setupLocalVideo    | 开启摄像头&绑定画布  并且推流                     |      |
| disableLocalVideo  | 关闭摄像头                                        |      |
| createRendererView | 创建渲染surface                                   |      |
| switchCamera       | 切换镜头                                          |      |
| setupRemoteVideo   | 绑定显示远端视频                                  |      |
| joinMeet           | 加入会议                                          |      |
| openMic            | 开启麦克风                                        |      |
| destroy            | 销毁                                              |      |
| setSpeakerOn       | 切换音频输出通道 true扬声器、false 听筒or外接设备 |      |

