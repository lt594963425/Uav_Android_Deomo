package com.dji.ux.sample;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.dji.ux.sample.base.BaseDialog;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by ${LiuTao}.
 * User: Administrator
 * Name: xiaoyuanyuan
 * functiona:
 * Date: 2019/8/16 0016
 * Time: 上午 9:55
 */
public class DialogUtils {

    public static DialogUtils getInstance() {
        return new DialogUtils();
    }

    public void showDialog(Context context, String msg, OnButtonClickListener onClickListener) {
        BaseDialog dialog = new BaseDialog(context, R.layout.base_dialog);
        dialog.show();
        TextView tvCancel = (TextView) dialog.findViewById(R.id.cancel);
        TextView tvOk = (TextView) dialog.findViewById(R.id.ok);
        TextView tvMsg = (TextView) dialog.findViewById(R.id.tv_msg);
        tvMsg.setText(msg);
        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onPositiveButtonClick(dialog);
            }
        });
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onCancelButtonClick(dialog);
            }
        });
    }

    public void showInputLngLatDialog(Context context,  OnButtonLngLatClickListener onClickListener) {
        BaseDialog dialog = new BaseDialog(context, R.layout.base_inpute_dialog);
        dialog.show();
        TextView tvCancel = (TextView) dialog.findViewById(R.id.cancel);
        TextView tvOk = (TextView) dialog.findViewById(R.id.ok);
        TextView input_tv_lng = (TextView) dialog.findViewById(R.id.input_tv_lng);
        TextView input_tv_lat = (TextView) dialog.findViewById(R.id.input_tv_lat);
        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(input_tv_lng.getText())) {
                    Toast.makeText(context, "请输入经度", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(input_tv_lng.getText().toString().trim())) {
                    Toast.makeText(context, "请输入经度", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(input_tv_lat.getText())) {
                    Toast.makeText(context, "请输入纬度", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(input_tv_lat.getText().toString().trim())) {
                    Toast.makeText(context, "请输入纬度", Toast.LENGTH_SHORT).show();
                    return;
                }
                double lng =0;
                double lat=0;
                try {
                     lng = Double.parseDouble(input_tv_lng.getText().toString());
                     lat = Double.parseDouble(input_tv_lat.getText().toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "请检查输入的坐标格式是否正确", Toast.LENGTH_SHORT).show();
                    return;
                }
                onClickListener.onPositiveButtonClick(dialog,lng,lat);
            }
        });
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onCancelButtonClick(dialog);
            }
        });
    }


    private void showDialog(BaseDialog dialog) {
        if (dialog != null) {
            dialog.show();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
        }

    }

    /**
     * 按钮点击回调接口
     */
    public interface OnButtonClickListener {
        /**
         * 确定按钮点击回调方法
         *
         * @param dialog 当前 AlertDialog，传入它是为了在调用的地方对 dialog 做操作，比如 dismiss()
         *               也可以在该工具类中直接  dismiss() 掉，就不用将 AlertDialog 对象传出去了
         */
        void onPositiveButtonClick(BaseDialog dialog);

        /**
         * 取消按钮点击回调方法
         *
         * @param dialog 当前AlertDialog
         */
        void onCancelButtonClick(BaseDialog dialog);
    }
    public interface OnButtonLngLatClickListener {
        /**
         * 确定按钮点击回调方法
         *
         * @param dialog 当前 AlertDialog，传入它是为了在调用的地方对 dialog 做操作，比如 dismiss()
         *               也可以在该工具类中直接  dismiss() 掉，就不用将 AlertDialog 对象传出去了
         */
        void onPositiveButtonClick(BaseDialog dialog,double lng,double lat);

        /**
         * 取消按钮点击回调方法
         *
         * @param dialog 当前AlertDialog
         */
        void onCancelButtonClick(BaseDialog dialog);
    }
    /**
     * 按钮点击回调接口
     */
    public interface OnButtonInputeClickListener {
        /**
         * 确定按钮点击回调方法
         *
         * @param dialog 当前 AlertDialog，传入它是为了在调用的地方对 dialog 做操作，比如 dismiss()
         *               也可以在该工具类中直接  dismiss() 掉，就不用将 AlertDialog 对象传出去了
         */
        void onPositiveButtonClick(BaseDialog dialog, String msg);

        /**
         * 取消按钮点击回调方法
         *
         * @param dialog 当前AlertDialog
         */
        void onCancelButtonClick(BaseDialog dialog);
    }


}
