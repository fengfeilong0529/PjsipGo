package com.example.pjsipgo;

import android.util.Log;
import android.widget.Toast;

import net.gotev.sipservice.BroadcastEventReceiver;
import net.gotev.sipservice.CodecPriority;
import net.gotev.sipservice.RtpStreamStats;

import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_status_code;

import java.util.ArrayList;

public class MyReceiver extends BroadcastEventReceiver {
    private static final String TAG = "MyReceiver";

    @Override
    public void onRegistration(String accountID, pjsip_status_code registrationStateCode) {
        super.onRegistration(accountID, registrationStateCode);
        Log.i(TAG, "onRegistration: ");
        if (registrationStateCode == pjsip_status_code.PJSIP_SC_OK) {
            Toast.makeText(receiverContext, "注册成功，账号：" + accountID, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(receiverContext, "注册失败，code：" + registrationStateCode, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onIncomingCall(String accountID, int callID, String displayName, String remoteUri, boolean isVideo) {
        super.onIncomingCall(accountID, callID, displayName, remoteUri, isVideo);
        Log.i(TAG, "onIncomingCall: ");
        CallActivity.startActivityIn(getReceiverContext(), accountID, callID, displayName, remoteUri, isVideo);
    }

    @Override
    public void onCallState(String accountID, int callID, pjsip_inv_state callStateCode, pjsip_status_code callStatusCode, long connectTimestamp, boolean isLocalHold, boolean isLocalMute, boolean isLocalVideoMute) {
        super.onCallState(accountID, callID, callStateCode, callStatusCode, connectTimestamp, isLocalHold, isLocalMute, isLocalVideoMute);
        Log.i(TAG, "onCallState - accountID: " + accountID +
                ", callID: " + callID +
                ", callStateCode: " + callStateCode +
                ", callStatusCode: " + callStatusCode +
                ", connectTimestamp: " + connectTimestamp +
                ", isLocalHold: " + isLocalHold +
                ", isLocalMute: " + isLocalMute +
                ", isLocalVideoMute: " + isLocalVideoMute);

        if (pjsip_inv_state.PJSIP_INV_STATE_CALLING.equals(callStateCode)) {
            //呼出

        } else if (pjsip_inv_state.PJSIP_INV_STATE_INCOMING.equals(callStateCode)) {
            //来电

        } else if (pjsip_inv_state.PJSIP_INV_STATE_EARLY.equals(callStateCode)) {
            //对方响铃

        } else if (pjsip_inv_state.PJSIP_INV_STATE_CONNECTING.equals(callStateCode)) {
            //连接中

        } else if (pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED.equals(callStateCode)) {
            //连接成功

        } else if (pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED.equals(callStateCode)) {
            //断开连接

        } else if (pjsip_inv_state.PJSIP_INV_STATE_NULL.equals(callStateCode)) {
            //未知错误

        }
    }

    @Override
    public void onOutgoingCall(String accountID, int callID, String number, boolean isVideo, boolean isVideoConference) {
        super.onOutgoingCall(accountID, callID, number, isVideo, isVideoConference);
        Log.i(TAG, "onOutgoingCall: ");
        CallActivity.startActivityOut(getReceiverContext(), accountID, callID, number, isVideo, isVideoConference);
    }

    @Override
    public void onStackStatus(boolean started) {
        super.onStackStatus(started);
        Log.i(TAG, "onStackStatus: ");
    }

    @Override
    public void onReceivedCodecPriorities(ArrayList<CodecPriority> codecPriorities) {
        super.onReceivedCodecPriorities(codecPriorities);
        Log.i(TAG, "onReceivedCodecPriorities: ");
    }

    @Override
    public void onCodecPrioritiesSetStatus(boolean success) {
        super.onCodecPrioritiesSetStatus(success);
        Log.i(TAG, "onCodecPrioritiesSetStatus: ");
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
}
