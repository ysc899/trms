package com.seegenemedical.trms.mobile;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.apache.commons.lang3.StringUtils;

import com.seegenemedical.trms.mobile.util.AndroidUtil;

public class WebViewInterface {

    private Activity _ac;
    private WebView _webView;

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    final Handler handler;

    public WebViewInterface(Activity _ac, WebView _webView, SharedPreferences _sp) {
        this._ac = _ac;
        this._webView = _webView;
        this.sp = _sp;
        editor = this.sp.edit();

        handler = new Handler();
    }

    @JavascriptInterface
    public void setLoginInfo(String id, String pw){
        Log.i("hopalt", id + "  :  " + pw);

        editor.putString(AndroidUtil.SP_LOGIN_ID, id);
        editor.putString(AndroidUtil.SP_LOGIN_PW, pw);
        editor.putBoolean(AndroidUtil.SP_LOGIN_AUTO, true);

        editor.commit();

        AndroidUtil.log("로그인 정보 저장 완료");
    }

    @JavascriptInterface
    public void clearLoginInfo(){
        editor.putString(AndroidUtil.SP_LOGIN_ID, "");
        editor.putString(AndroidUtil.SP_LOGIN_PW, "");
        editor.putBoolean(AndroidUtil.SP_LOGIN_AUTO, false);

        editor.commit();

        AndroidUtil.log("로그아웃 완료");
    }
    

    @JavascriptInterface
    public void callLoginInfo(){

        AndroidUtil.log("자동 로그인 호출");
        String id = sp.getString(AndroidUtil.SP_LOGIN_ID, "");
        String pw = sp.getString(AndroidUtil.SP_LOGIN_PW, "");


        if(StringUtils.isNotEmpty(id) && StringUtils.isNotEmpty(pw)){

            handler.post(new Runnable() {
                @Override
                public void run() {
                    _webView.loadUrl("javascript:setLoginInfo('"+id+"', '"+pw+"' )");
                    AndroidUtil.log("자동 로그인 호출 완료");
                }
            });
        }
    }




}
