package com.seegenemedical.trms.mobile;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.NonNull;

import com.seegenemedical.trms.mobile.R;

public class SplashDialog extends Dialog {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_splash);

    }

//    public SplashDialog(@NonNull Context context) {
//        super(context);
//
//    }

    public SplashDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }
}
