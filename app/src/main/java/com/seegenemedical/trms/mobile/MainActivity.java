package com.seegenemedical.trms.mobile;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Environment;
import android.os.Message;
import android.util.Log;

import android.view.Window;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.net.URLDecoder;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import com.seegenemedical.trms.mobile.BuildConfig;
import com.seegenemedical.trms.mobile.R;
import com.seegenemedical.trms.mobile.util.AndroidUtil;

public class MainActivity extends AppCompatActivity {


    private Context context;

    SharedPreferences sp;
//    SharedPreferences.Editor editor;

    private FrameLayout frameLayout;

    private WebView webView;
    private WebView webViewPopup;


    private WebViewInterface webViewInterface;

    private Stack<WebView> mWebViews;

    private final String SERVER_URL = BuildConfig.URL_SERVER;

    public boolean flagAutoLogin = false;

    //권한 확인용
    private int REQUEST_CODE_PERMISSIONS = 10; //arbitrary number, can be changed accordingly
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE" };


    //인트로 다이얼로그
    private SplashDialog splashDialog;
    private long splashStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_view);

        splashDialog =new SplashDialog(this, R.style.AppBaseTheme);

        if(allPermissionsGranted()){
            this.initUI();
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

    }

    private void initUI(){

        if(!splashDialog.isShowing()) {
            splashDialog.show();
            splashStartTime = System.currentTimeMillis();
        }

        frameLayout = (FrameLayout) findViewById(R.id.fl_web);
        webView = findViewById(R.id.wv_main);

        webViewPopup = findViewById(R.id.wv_main);
        context = this;

        if(AndroidUtil.isConnected(context)){
            this.init();
        }else{
            Activity act = this;

            AndroidUtil.toast(context, "네트워크에 연결이 안되어 있습니다.");
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    act.finish();
                }
            }, 2000);

        }

    }


    private void init(){


        sp = getSharedPreferences(AndroidUtil.TAG_SP, Context.MODE_PRIVATE);

        this.flagAutoLogin = sp.getBoolean(AndroidUtil.SP_LOGIN_AUTO, false);


        mWebViews = new Stack<>();
        mWebViews.add(webView);

        webView.getSettings().setJavaScriptEnabled(true);
        //localStorage 등의 함수 사용 가능
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setBuiltInZoomControls(false);


        //줌 컨트롤롤
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setDisplayZoomControls(false);



        //크롬 인스펙터 사용 가능
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        //replaceState 관련 문제
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setSupportMultipleWindows(true);

        //사용자 제스쳐 없이 동영상 재생
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);

        //텍스트 사이즈 강제 확대 못하게 하기
        webView.getSettings().setTextZoom(100);



        //웹뷰에 자바스크립트 인터페이스를 연결
        webViewInterface = new WebViewInterface(this, webView, sp);

        webView.setWebChromeClient(new WebChromeClientClass());
        webView.setWebViewClient(new WebVeiewClientClass());

        webView.addJavascriptInterface(webViewInterface, "Android");


        if(flagAutoLogin){
            webView.loadUrl(SERVER_URL + "mobileLogin.do");

        }else {
            webView.loadUrl(SERVER_URL + "mobileMain.do");
        }

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private class WebVeiewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }


        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            mBackPressed = false;

            //로그인 페이지가 로딩 완료고 자동 로그인시 로그인 메소드 호출
            if(StringUtils.contains(url, "mobileLogin.do") && flagAutoLogin){
              webViewInterface.callLoginInfo();
            }

            if(StringUtils.contains(url, "mobileMain.do")){
                if(splashDialog.isShowing()){

                    if(System.currentTimeMillis() - splashStartTime > 2000){
                        splashDialog.dismiss();
                    }else{
                        long delay = 2000 - (System.currentTimeMillis() - splashStartTime);

                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                splashDialog.dismiss();
                            }
                        }, delay);
                    }

                }

            }

        }
    }

    private class WebChromeClientClass extends WebChromeClient {
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {

            // window.opener 시
            webViewPopup = new WebView(view.getContext());
            webViewPopup.getSettings().setJavaScriptEnabled(true);
            webViewPopup.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            webViewPopup.getSettings().setSupportMultipleWindows(true);
            webViewPopup.getSettings().setDomStorageEnabled(true);
            webViewPopup.getSettings().setTextZoom(100);
            webViewPopup.setWebChromeClient(new WebChromeClient(){
                @Override
                public void onCloseWindow(WebView window) {
                    frameLayout.removeView(window);
                    window.destroy();
                }
            });


            //줌 컨트롤롤
            webViewPopup.getSettings().setLoadWithOverviewMode(true);
            webViewPopup.getSettings().setUseWideViewPort(true);
            webViewPopup.getSettings().setBuiltInZoomControls(true);
            webViewPopup.getSettings().setSupportZoom(true);
            webViewPopup.getSettings().setDisplayZoomControls(false);


            webViewPopup.setDownloadListener(new DownloadListener() {
                @Override
                public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                    try {



                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

                        contentDisposition = URLDecoder.decode(contentDisposition,"UTF-8"); //디코딩
                        String FileName = contentDisposition.replace("attachment; filename=", ""); //attachment; filename*=UTF-8''뒤에 파일명이있는데 파일명만 추출하기위해 앞에 attachment; filename*=UTF-8''제거

                        String fileName = FileName; //위에서 디코딩하고 앞에 내용을 자른 최종 파일명
                        request.setMimeType(mimetype);

                        // 파일명 잘라내기
                        String fileName2 = contentDisposition;
                        if (fileName2 != null && fileName2.length() > 0) {
                            int idxFileName = fileName2.indexOf("filename=");
                            if (idxFileName > -1) {
                                fileName2 = fileName2.substring(idxFileName + 9).trim();
                            }

                            if (fileName2.endsWith(";")) {
                                fileName2 = fileName2.substring(0, fileName2.length() -1);
                            }

                            if (fileName2.startsWith("\"") && fileName.startsWith("\"")) {
                                fileName2 = fileName2.substring(1, fileName2.length() -1);
                            }
                        }

                        //------------------------COOKIE!!------------------------
                        String cookies = CookieManager.getInstance().getCookie(url);
                        request.addRequestHeader("cookie", cookies);
                        //------------------------COOKIE!!------------------------

                        request.addRequestHeader("User-Agent", userAgent);
                        request.setDescription("Downloading File");
                        request.setAllowedOverMetered(true);
                        request.setAllowedOverRoaming(true);
//                        request.setTitle(fileName);
                        request.setTitle(fileName2);
                        request.setRequiresCharging(false);

                        request.allowScanningByMediaScanner();
                        request.setAllowedOverMetered(true);
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName2);
                        dm.enqueue(request);
                        Toast.makeText(getApplicationContext(),"파일이 다운로드됩니다.", Toast.LENGTH_LONG).show();
                    }
                    catch (Exception e) {

                        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                Toast.makeText(getBaseContext(), "다운로드를 위해\n권한이 필요합니다.", Toast.LENGTH_LONG).show();
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1004);
                            }
                            else {
                                Toast.makeText(getBaseContext(), "다운로드를 위해\n권한이 필요합니다.", Toast.LENGTH_LONG).show();
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1004);
                            }
                        }
                    }
                }
            });


            webViewPopup.setWebViewClient(new WebViewClient());
            webViewPopup.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT));
            frameLayout.addView(webViewPopup);
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(webViewPopup);
            resultMsg.sendToTarget();
            return true;

        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Log.i("hopalt", "web : "+consoleMessage.message());
            return super.onConsoleMessage(consoleMessage);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult jsResult) {
            new android.app.AlertDialog.Builder(context)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, new android.app.AlertDialog.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            jsResult.confirm();
                        }
                    }).setCancelable(false)
                    .create()
                    .show();

            return true;
        }

    }

    boolean mBackPressed = false;

    @Override
    public void onBackPressed() {

        if(webView.canGoBack()){

            String url = webView.getUrl();
            String url2 = webViewPopup.getUrl();

            if(!StringUtils.equals(url, url2)){
                if(StringUtils.contains(url2, "mobileRstUserTableMini02.do" ) || StringUtils.contains(url2, "mobileNoticeView.do" ) || StringUtils.contains(url2, "mobileTestItemView.do" )){
                    webViewPopup.loadUrl("javascript:window.close();");
                    return;
                }
            }

            if(url.contains("/mobileMain.do")){
//                Log.i("hopalt", "메인 중임");

                if(!mBackPressed) {
                    mBackPressed = true;
                    Toast.makeText(context, "한 번 더 누르시면 앱을 종료합니다.", Toast.LENGTH_LONG).show();
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            mBackPressed = false;
                        }
                    }, 2000);
                }else{
                    finish();
                }

            }else {
                webView.goBack();
            }
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //start camera when permissions have been granted otherwise exit app
        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()){
                this.initUI();
            } else{
                Toast.makeText(this, "권한을 허용 하여야만 모든 기능이 사용 가능 합니다.", Toast.LENGTH_SHORT).show();
                this.initUI();
            }
        }
    }

    private boolean allPermissionsGranted(){
        //check if req permissions have been granted
        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

}