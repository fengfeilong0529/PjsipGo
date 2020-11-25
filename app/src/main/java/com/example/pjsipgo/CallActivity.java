package com.example.pjsipgo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.gotev.sipservice.BroadcastEventReceiver;
import net.gotev.sipservice.CodecPriority;
import net.gotev.sipservice.RtpStreamStats;
import net.gotev.sipservice.SipServiceCommand;

import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_status_code;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CallActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static final String TAG = "CallActivity";

    @BindView(R.id.textViewPeer)
    TextView mTextViewPeer;
    @BindView(R.id.textViewCallState)
    TextView mTextViewCallState;
    @BindView(R.id.buttonAccept)
    Button mButtonAccept;
    @BindView(R.id.buttonHangup)
    Button mButtonHangup;
    @BindView(R.id.layoutIncomingCall)
    LinearLayout mLayoutIncomingCall;
    @BindView(R.id.tvOutCallInfo)
    TextView mTvOutCallInfo;
    @BindView(R.id.btnCancel)
    Button mBtnCancel;
    @BindView(R.id.layoutOutCall)
    LinearLayout mLayoutOutCall;
    @BindView(R.id.svRemote)
    SurfaceView mSvRemote;
    @BindView(R.id.svLocal)
    SurfaceView mSvLocal;
    @BindView(R.id.btnMuteMic)
    ImageButton mBtnMuteMic;
    @BindView(R.id.btnHangUp)
    ImageButton mBtnHangUp;
    @BindView(R.id.btnSwitchCamera)
    ImageButton mBtnSpeaker;
    @BindView(R.id.layoutConnected)
    RelativeLayout mLayoutConnected;
    @BindView(R.id.parent)
    LinearLayout mParent;

    private String mAccountID;
    private String mDisplayName;
    private String mRemoteUri;
    private int mCallID;
    private boolean mIsVideo;
    private int mType;
    private String mNumber;
    private boolean mIsVideoConference;
    private boolean micMute;

    public static final int TYPE_INCOMING_CALL = 646;
    public static final int TYPE_OUT_CALL = 647;
    public static final int TYPE_CALL_CONNECTED = 648;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        ButterKnife.bind(this);
        registReceiver();
        initData();
    }

    private void registReceiver() {
        mReceiver.register(this);
    }

    private void initData() {
        mAccountID = getIntent().getStringExtra("accountID");
        mCallID = getIntent().getIntExtra("callID", -1);
        mType = getIntent().getIntExtra("type", -1);
        mDisplayName = getIntent().getStringExtra("displayName");
        mRemoteUri = getIntent().getStringExtra("remoteUri");
        mNumber = getIntent().getStringExtra("number");
        mIsVideo = getIntent().getBooleanExtra("isVideo", false);
        mIsVideoConference = getIntent().getBooleanExtra("isVideoConference", false);

        showLayout(mType);
        mTextViewPeer.setText(String.format("%s\n%s", mRemoteUri, mDisplayName));
        mTvOutCallInfo.setText(String.format("您正在呼叫 %s", mNumber));

        SurfaceHolder holder = mSvLocal.getHolder();
        holder.addCallback(this);

        mSvRemote.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                SipServiceCommand.setupIncomingVideoFeed(CallActivity.this, mAccountID, mCallID, surfaceHolder.getSurface());
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                SipServiceCommand.setupIncomingVideoFeed(CallActivity.this, mAccountID, mCallID, null);
            }
        });
    }

    public static void startActivityIn(Context context, String accountID, int callID, String displayName, String remoteUri, boolean isVideo) {
        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra("accountID", accountID);
        intent.putExtra("callID", callID);
        intent.putExtra("displayName", displayName);
        intent.putExtra("remoteUri", remoteUri);
        intent.putExtra("isVideo", isVideo);
        intent.putExtra("type", TYPE_INCOMING_CALL);
        context.startActivity(intent);
    }

    public static void startActivityOut(Context context, String accountID, int callID, String number, boolean isVideo, boolean isVideoConference) {
        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra("accountID", accountID);
        intent.putExtra("callID", callID);
        intent.putExtra("number", number);
        intent.putExtra("isVideo", isVideo);
        intent.putExtra("isVideoConference", isVideoConference);
        intent.putExtra("type", TYPE_OUT_CALL);
        context.startActivity(intent);
    }

    @OnClick({R.id.buttonAccept, R.id.buttonHangup, R.id.btnCancel, R.id.btnMuteMic, R.id.btnHangUp, R.id.btnSwitchCamera})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.buttonAccept:
                //接听
                SipServiceCommand.acceptIncomingCall(this, mAccountID, mCallID, mIsVideo);
                break;
            case R.id.buttonHangup:
                //拒绝
                SipServiceCommand.declineIncomingCall(this, mAccountID, mCallID);
                finish();
                break;
            case R.id.btnCancel:
                //取消呼叫
                SipServiceCommand.hangUpActiveCalls(this, mAccountID);
                finish();
                break;
            case R.id.btnMuteMic:
                //麦克风静音
                micMute = !micMute;
                SipServiceCommand.setCallMute(this, mAccountID, mCallID, micMute);
                mBtnMuteMic.setSelected(micMute);
                break;
            case R.id.btnHangUp:
                //挂断
                SipServiceCommand.hangUpCall(this, mAccountID, mCallID);
                finish();
                break;
            case R.id.btnSwitchCamera:
                //切换摄像头
                SipServiceCommand.switchVideoCaptureDevice(this,mAccountID,mCallID);
                break;
        }
    }

    private void showLayout(int type) {
        if (type == TYPE_INCOMING_CALL) {
            mLayoutIncomingCall.setVisibility(View.VISIBLE);
            mLayoutOutCall.setVisibility(View.GONE);
            mLayoutConnected.setVisibility(View.GONE);
        } else if (type == TYPE_OUT_CALL) {
            mLayoutIncomingCall.setVisibility(View.GONE);
            mLayoutOutCall.setVisibility(View.VISIBLE);
            mLayoutConnected.setVisibility(View.GONE);
        } else if (type == TYPE_CALL_CONNECTED) {
            mLayoutIncomingCall.setVisibility(View.GONE);
            mLayoutOutCall.setVisibility(View.GONE);
            mLayoutConnected.setVisibility(View.VISIBLE);
        } else {
            TextView textView = new TextView(this);
            textView.setText("ERROR~~~~~~~~~~~~~");
            mParent.addView(textView);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mReceiver.unregister(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        SipServiceCommand.startVideoPreview(CallActivity.this, mAccountID, mCallID, mSvLocal.getHolder().getSurface());

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    }

    public BroadcastEventReceiver mReceiver = new BroadcastEventReceiver() {

        @Override
        public void onIncomingCall(String accountID, int callID, String displayName, String remoteUri, boolean isVideo) {
            super.onIncomingCall(accountID, callID, displayName, remoteUri, isVideo);
            Toast.makeText(receiverContext, String.format("收到 [%s] 的来电", remoteUri), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCallState(String accountID, int callID, pjsip_inv_state callStateCode, pjsip_status_code callStatusCode, long connectTimestamp, boolean isLocalHold, boolean isLocalMute, boolean isLocalVideoMute) {
            super.onCallState(accountID, callID, callStateCode, callStatusCode, connectTimestamp, isLocalHold, isLocalMute, isLocalVideoMute);
            if (pjsip_inv_state.PJSIP_INV_STATE_CALLING.equals(callStateCode)) {
                //呼出
                mTextViewCallState.setText("calling");
            } else if (pjsip_inv_state.PJSIP_INV_STATE_INCOMING.equals(callStateCode)) {
                //来电
                mTextViewCallState.setText("incoming");
            } else if (pjsip_inv_state.PJSIP_INV_STATE_EARLY.equals(callStateCode)) {
                //响铃
                mTextViewCallState.setText("early");
            } else if (pjsip_inv_state.PJSIP_INV_STATE_CONNECTING.equals(callStateCode)) {
                //连接中
                mTextViewCallState.setText("connecting");
            } else if (pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED.equals(callStateCode)) {
                //连接成功
                mTextViewCallState.setText("confirmed");
                showLayout(TYPE_CALL_CONNECTED);
            } else if (pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED.equals(callStateCode)) {
                //断开连接
                finish();
            } else if (pjsip_inv_state.PJSIP_INV_STATE_NULL.equals(callStateCode)) {
                //未知错误
                Toast.makeText(receiverContext, "未知错误", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        @Override
        public void onOutgoingCall(String accountID, int callID, String number, boolean isVideo, boolean isVideoConference) {
            super.onOutgoingCall(accountID, callID, number, isVideo, isVideoConference);
        }

        @Override
        public void onStackStatus(boolean started) {
            super.onStackStatus(started);
        }

        @Override
        public void onReceivedCodecPriorities(ArrayList<CodecPriority> codecPriorities) {
            super.onReceivedCodecPriorities(codecPriorities);
        }

        @Override
        public void onCodecPrioritiesSetStatus(boolean success) {
            super.onCodecPrioritiesSetStatus(success);
        }

        @Override
        public void onMissedCall(String displayName, String uri) {
            super.onMissedCall(displayName, uri);
        }

        @Override
        protected void onVideoSize(int width, int height) {
            super.onVideoSize(width, height);
        }

        @Override
        protected void onCallStats(int duration, String audioCodec, pjsip_status_code callStatusCode, RtpStreamStats rx, RtpStreamStats tx) {
            super.onCallStats(duration, audioCodec, callStatusCode, rx, tx);
        }
    };
}
