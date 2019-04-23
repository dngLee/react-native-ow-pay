package com.openwater.pay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.os.Handler.Callback;
import com.unionpay.UPPayAssistEx;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

public class UNPayEntryActivity extends Activity implements Callback {
    public WXPayCallBack callback = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.obj == null || ((String) msg.obj).length() == 0) {
            //取不到数据
        } else {
            String tn = (String) msg.obj;
            UPPayAssistEx.startPay(this, null, null, tn, "01");
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        WritableMap result = Arguments.createMap();
        result.putString("requestCode", String.valueOf(requestCode));
        result.putString("requestCode", String.valueOf(resultCode));
        result.putString("data", "" + data);
        callback.callBack(result);
    }
}
