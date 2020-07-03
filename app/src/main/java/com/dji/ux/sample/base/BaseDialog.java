package com.dji.ux.sample.base;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

import com.dji.ux.sample.R;


/***
 * BaseDialog
 */
public class BaseDialog extends Dialog {
    public Context context;
    public int resLayout;

    public BaseDialog(Context context, int resLayout) {
        super(context, R.style.base_picture_alert_dialog);
        this.context = context;
        this.resLayout = resLayout;
        Window window = getWindow();
        window.setWindowAnimations(R.style.BaseDialogWindowStyle);
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(resLayout);
    }
}