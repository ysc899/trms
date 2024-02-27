package com.seegenemedical.trms.mobile.util;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;


/**
 * 공통 사용할 유틸
 */
public class AndroidUtil {

    /** SharedPreferences Name 테크 */
    public static final String TAG_SP = "seegene-TRMS";

    public static final String SP_LOGIN_AUTO = "loginAuto";

    public static final String SP_LOGIN_ID = "loginId";

    public static final String SP_LOGIN_PW = "loginPw";

    public static final String LOG_TAG = "hopalt";



    public static void log(final Object obj){
        LogWrapper.v(LOG_TAG, obj.toString());
    }

    public static void log(final String msg){
//        Log.i(LOG_TAG, msg);
        LogWrapper.v(LOG_TAG, msg);
    }
    public static void log(final String msg, final Throwable tr){
//        Log.i(LOG_TAG, msg);
        tr.printStackTrace();
        LogWrapper.v(LOG_TAG, msg, tr);
    }

    public static void toast(final Context context, final String msg){
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
    public static void toastShort(final Context context, final String msg){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }


    /**
     * 확인 다이얼로그
     * @param context context
     * @param title 타이틀
     * @param message 메시지
     */
    public static void showAlert(final Context context, final String title, final String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    public static final int ALERT_CALLBACK_OK = 300;
    public static final int ALERT_CALLBACK_CANCEL = 3001;

//    public static void showAlert(final Context context, final String title, final String message, final String btnMsg, final Handler callback){
//        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialog);
//
//        builder.setTitle(title);
//        builder.setMessage(message);
//
//        builder.setPositiveButton(btnMsg, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                callback.sendEmptyMessage(ALERT_CALLBACK_OK);
//                dialogInterface.dismiss();
//            }
//        });
//        builder.show();
//    }


    public static void copyText(final Context context, final String label, final String t){
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(label, t);
        clipboardManager.setPrimaryClip(clipData);

        toast(context, label + "이 복사되었습니다.");
    }

//    public static int textToInt(final String str){
//        if(StringUtils.isEmpty(str)){
//            return 0;
//        }else{
//            return Integer.valueOf(str);
//        }
//    }


    /** 
     * 네트워크 연결 유무 체크
     * */
    public static boolean isConnected(Context context){ //해당 context의 서비스를 사용하기위해서 context객체를 받는다.
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();

    }

}
