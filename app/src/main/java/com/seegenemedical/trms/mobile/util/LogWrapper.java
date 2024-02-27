package com.seegenemedical.trms.mobile.util;

import android.os.Binder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * 로그 파일로 기록 하기 위한 랩퍼
 */
public class LogWrapper {
    private static final String TAG = "hopalt";
    private static final int LOG_FILE_SIZE_LIMIT = 1024*1024*10;
    private static final int LOG_FILE_MAX_COUNT = 10;
    private static final String LOG_FILE_NAME = "seegene_trms_Log%g.txt";
    private static final SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.getDefault());
    private static final Date date = new Date();
    private static Logger logger;
    private static FileHandler fileHandler;

    static {
        try{

            File f = new File(Environment.getExternalStorageDirectory()+ File.separator + "Android"+File.separator+"data"+ File.separator +"com.seegenemedical.trms.app.seegenemedical.kr.co.seesoft.seegene.trms"+ File.separator +"log");
            if(!f.exists()){
                f.mkdirs();
            }

            fileHandler = new FileHandler( f.getAbsolutePath()+ File.separator +LOG_FILE_NAME, LOG_FILE_SIZE_LIMIT, LOG_FILE_MAX_COUNT, true);

            fileHandler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    date.setTime(System.currentTimeMillis());

                    StringBuilder ret = new StringBuilder(80 );
                    ret.append(formatter.format(date ));
                    ret.append(record.getMessage());

                    return ret.toString();
                }
            });

            logger = Logger.getLogger(com.seegenemedical.trms.mobile.util.LogWrapper.class.getName());
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);
            Log.d(TAG, "초기화 성공");

        }catch (IOException e){
            Log.d(TAG, "초기화 실패");
            e.printStackTrace();
        }
    }

    public static void v(String tag, String msg){
        if(logger != null){
            logger.log(Level.INFO, String.format("V/%s(%d) : %s\n", tag, Binder.getCallingPid(), msg));
        }

        Log.v(tag, msg);
    }

    public static void v(String tag, String msg, Throwable tr){
        if(logger != null){
            StringWriter sw = new StringWriter();
            tr.printStackTrace(new PrintWriter(sw));
            logger.log(Level.INFO, String.format("V/%s(%d) : %s\n", tag, Binder.getCallingPid(), msg));
            logger.log(Level.INFO, "에러 : " + sw.toString());
        }
        Log.v(tag, "에러 : "+msg + " :: "+ tr.getMessage() + " :: " + tr.toString());
    }

}
