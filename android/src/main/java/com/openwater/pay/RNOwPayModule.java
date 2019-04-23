
package com.openwater.pay;

import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.alipay.sdk.app.PayTask;
import com.unionpay.UPPayAssistEx;
//import com.braintreepayments.api.BraintreeFragment;
//import com.braintreepayments.api.Card;
//import com.braintreepayments.api.exceptions.InvalidArgumentException;
//import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
//import com.braintreepayments.api.models.CardBuilder;
//import com.braintreepayments.api.models.PaymentMethodNonce;

public class RNOwPayModule extends ReactContextBaseJavaModule implements ActivityEventListener/*, PaymentMethodNonceCreatedListener*/ {
    private final ReactApplicationContext reactContext;
    private static Callback mCallback = null;
    private static IWXAPI api;
    public static String WX_APP_ID = ""; // 微信
    public static String AL_SCHEME = ""; // 支付宝
    public static String UN_SCHEME = ""; // 银联
    public static String UN_MODE = "01"; //默认开发模式
//    private BraintreeFragment mBraintreeFragment;

    public RNOwPayModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        reactContext.addActivityEventListener(this);
    }

    @Override
    public String getName() {
    return "RNOwPay";
  }

    @ReactMethod
    public void registerWxPay(String appId) {
        api = WXAPIFactory.createWXAPI(getCurrentActivity(), appId);
        api.registerApp(appId);
        WX_APP_ID = appId;
    }

    @ReactMethod
    public void configAlipayScheme(String scheme) {
    AL_SCHEME = scheme;
  }

    @ReactMethod
    public void configUnpayScheme(String scheme) {
      UN_SCHEME = scheme;
    }

    @ReactMethod
    public void configUnpayMode(String mode) {
        UN_MODE = mode;
    }

    @ReactMethod
    public void pay(ReadableMap orderInfo, int platform, final Callback callback) {
        mCallback = callback;
        switch (platform) {
            case 0: {
                PayReq req = new PayReq();
                req.appId = orderInfo.getString("appid");
                req.partnerId = orderInfo.getString("partnerid");
                req.prepayId = orderInfo.getString("prepayid");
                req.packageValue = orderInfo.getString("package");
                req.nonceStr = orderInfo.getString("noncestr");
                req.timeStamp = orderInfo.getString("timestamp");
                req.sign = orderInfo.getString("sign");
                XWXPayEntryActivity.callback = new WXPayCallBack() {
                    public void callBack(WritableMap result) {
                        callback.invoke(result);
                    }
                };
                // 发起请求
                api.sendReq(req);
            } break;

            case 1: {
                final String orderInfoStr = orderInfo.getString("orderString");
                Runnable payRunnable = new Runnable() {
                    @Override
                    public void run() {
                        PayTask alipay = new PayTask(getCurrentActivity());
                            Map<String, String> result = alipay.payV2(orderInfoStr, true);
                        WritableMap map = Arguments.createMap();
                        map.putString("memo", result.get("memo"));
                        map.putString("result", result.get("result"));
                        map.putString("errCode", result.get("resultStatus"));
                        callback.invoke(map);
                    }
                };
                // 必须异步调用
                Thread payThread = new Thread(payRunnable);
                payThread.start();
            } break;

            case 2: {
                final String tn = orderInfo.getString("tn");
                UPPayAssistEx.startPay(getCurrentActivity(), null, null, tn, UN_MODE);
            } break;

            default: {

            }
        }
    }

//    @ReactMethod
//    public void configBrainTreeClientToken(String clientToken) {
//        try {
//            mBraintreeFragment = BraintreeFragment.newInstance(getCurrentActivity(), clientToken);
//            mBraintreeFragment.addListener(this);
//        } catch (InvalidArgumentException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (data == null || mCallback == null) {
            return;
        }
        WritableMap result = Arguments.createMap();
        /*
         * 支付控件返回字符串:success、fail、cancel 分别代表支付成功，支付失败，支付取消
         */
        String str = data.getExtras().getString("pay_result");
        if (str.equalsIgnoreCase("success")) {
            // 如果想对结果数据验签，可使用下面这段代码，但建议不验签，直接去商户后台查询交易结果
            // result_data结构见c）result_data参数说明
            if (data.hasExtra("result_data")) {
                String result_data = data.getExtras().getString("result_data");
                result.putString("result_data", result_data);
            }

            result.putString("errCode", "0");
            // 结果result_data为成功时，去商户后台查询一下再展示成功
        } else if (str.equalsIgnoreCase("fail")) {
            result.putString("errCode", "10002");
            result.putString("result_msg", "fail");
        } else if (str.equalsIgnoreCase("cancel")) {
            result.putString("errCode", "10003");
            result.putString("result_msg", "cancel");
        } else {
            result.putString("errCode", "10001");
            result.putString("result_msg", "error");
        }
        mCallback.invoke(result);
    }

    //    @ReactMethod
//    public void fetchNonce(ReadableMap params, Callback callback) {
//        mCallback = callback;
//        CardBuilder cardBuilder = new CardBuilder()
//                .cardholderName(params.getString("name"))
//                .expirationYear(params.getString("expYear"))
//                .expirationMonth(params.getString("expMonth"))
//                .cvv(params.getString("cvV"))
//                .cardNumber(params.getString("number"));
//
//        Card.tokenize(mBraintreeFragment, cardBuilder);
//    }

    @Override
    public void onNewIntent(Intent intent) {
        if(intent != null){
//            String str = intent.getExtras().getString("pay_result");
        }
    }

//    @Override
//    public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
//        WritableNativeMap options = new WritableNativeMap();
//        options.putString("nonce", paymentMethodNonce.getNonce());
//        options.putString("description", paymentMethodNonce.getDescription());
//        mCallback.invoke(options);
//    }
}