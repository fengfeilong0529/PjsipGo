package com.example.pjsipgo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import net.gotev.sipservice.BroadcastEventReceiver;
import net.gotev.sipservice.Logger;
import net.gotev.sipservice.SipAccountData;
import net.gotev.sipservice.SipServiceCommand;

import org.pjsip.pjsua2.pjsip_status_code;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @BindView(R.id.etAccount)
    EditText mEtAccount;
    @BindView(R.id.etPwd)
    EditText mEtPwd;
    @BindView(R.id.etServer)
    EditText mEtServer;
    @BindView(R.id.etPort)
    EditText mEtPort;
    @BindView(R.id.layoutLogin)
    LinearLayout mLayoutLogin;
    @BindView(R.id.etCallNumer)
    EditText mEtCallNumer;
    @BindView(R.id.layoutCallOut)
    LinearLayout mLayoutCallOut;

    private SipAccountData mAccount;
    private String mAccountId;

    private static final int REQUEST_PERMISSIONS_STORAGE = 0x100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mReceiver.register(this);
        Logger.setLogLevel(Logger.LogLevel.DEBUG);
        requestPermissions();
    }

    //登录
    public void login(View view) {
        String server = mEtServer.getText().toString().trim();
        String account = mEtAccount.getText().toString().trim();
        String pwd = mEtPwd.getText().toString().trim();
        String port = mEtPort.getText().toString().trim();

        if (TextUtils.isEmpty(server) || TextUtils.isEmpty(account) || TextUtils.isEmpty(pwd) || TextUtils.isEmpty(port)) {
            Toast.makeText(this, "请将信息填写完整！", Toast.LENGTH_SHORT).show();
            return;
        }

        mAccount = new SipAccountData();
        mAccount.setHost(server);
        mAccount.setRealm("*"); //realm指的是：sip:1004@192.168.2.243中的192.168.2.243
        mAccount.setPort(Integer.parseInt(port));
        mAccount.setUsername(account);
        mAccount.setPassword(pwd);
        mAccountId = SipServiceCommand.setAccount(this, mAccount);
        Log.i(TAG, "login: " + mAccountId);
    }

    //语音呼叫
    public void audioCall(View view) {
        requestPermissions();
        String callNumber = mEtCallNumer.getText().toString().trim();
        if (TextUtils.isEmpty(callNumber)) {
            Toast.makeText(this, "请输入呼叫号码！", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            SipServiceCommand.makeCall(this, mAccountId, callNumber, false, false);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Account error", Toast.LENGTH_SHORT).show();
        }
    }

    //视频呼叫
    public void videoCall(View view) {
        requestPermissions();
        String callNumber = mEtCallNumer.getText().toString().trim();
        if (TextUtils.isEmpty(callNumber)) {
            Toast.makeText(this, "请输入呼叫号码！", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            SipServiceCommand.makeCall(this, mAccountId, callNumber, true, false);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Account error", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 申请动态权限
     */
    private void requestPermissions() {
        String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,// 写入权限
                Manifest.permission.READ_EXTERNAL_STORAGE, // 读取权限
                Manifest.permission.CAMERA,                // 相机权限
                Manifest.permission.RECORD_AUDIO,          // 麦克风权限
        };
        if (!checkPermissionAllGranted(permissions)) {
            // 一次请求多个权限, 如果其他有权限是已经授予的将会自动忽略掉
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS_STORAGE);
        } else {

        }
    }

    /**
     * 检查是否拥有指定的所有权限
     */
    protected boolean checkPermissionAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_STORAGE) {
            boolean ok = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    ok = false;
                }
            }
            if (ok) {
                Toast.makeText(MainActivity.this, "权限申请成功！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mReceiver.unregister(this);
        if (mAccount != null) {
            SipServiceCommand.removeAccount(this, mAccountId);
        }
    }

    public BroadcastEventReceiver mReceiver = new BroadcastEventReceiver() {

        @Override
        public void onRegistration(String accountID, pjsip_status_code registrationStateCode) {
            super.onRegistration(accountID, registrationStateCode);
            Log.i(TAG, "onRegistration: ");
            if (registrationStateCode == pjsip_status_code.PJSIP_SC_OK) {
                Toast.makeText(receiverContext, "登录成功，账号：" + accountID, Toast.LENGTH_SHORT).show();
                mLayoutCallOut.setVisibility(View.VISIBLE);
                mLayoutLogin.setVisibility(View.GONE);
            } else {
                Toast.makeText(receiverContext, "登录失败，code：" + registrationStateCode, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onIncomingCall(String accountID, int callID, String displayName, String remoteUri, boolean isVideo) {
            super.onIncomingCall(accountID, callID, displayName, remoteUri, isVideo);
            CallActivity.startActivityIn(getReceiverContext(), accountID, callID, displayName, remoteUri, isVideo);
        }

        @Override
        public void onOutgoingCall(String accountID, int callID, String number, boolean isVideo, boolean isVideoConference) {
            super.onOutgoingCall(accountID, callID, number, isVideo, isVideoConference);
            CallActivity.startActivityOut(getReceiverContext(), accountID, callID, number, isVideo, isVideoConference);
        }
    };
}
