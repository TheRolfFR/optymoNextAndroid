package com.therolf.optymoNext.controller.global;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.therolf.optymoNext.R;

@SuppressWarnings({"unused", "NullableProblems", "UnusedReturnValue"})
public class AlertController {

    private Dialog dialog;

    public AlertController(Context context) {
        dialog = new Dialog(context) {
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                // Tap anywhere to close dialog.
                Rect dialogBounds = new Rect();
                Window window = this.getWindow();
                InputMethodManager inputMethodManager = (InputMethodManager) context
                        .getSystemService(Context.INPUT_METHOD_SERVICE);

                if(window != null && inputMethodManager != null) {
                    window.getDecorView().getHitRect(dialogBounds);
                    if (!dialogBounds.contains((int) event.getX(), (int) event.getY())) {
                        // You have clicked the grey area
                        View v = this.getCurrentFocus();
                        if(v != null) {
                            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        }
                    } else {
                        View v = this.getCurrentFocus();
                        if(v != null) {
                            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                            inputMethodManager.hideSoftInputFromWindow(this
                                    .getCurrentFocus().getWindowToken(), 0);
                        }
                        this.dismiss();
                    }
                }

                return true;
            }
        };
        dialog.setCanceledOnTouchOutside(true);
        Utility.setMargins(dialog, 0, 0, 0, 0);
        dialog.setContentView(R.layout.dialog_alert);
        Window window = dialog.getWindow();
        if(window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        }

        // hide buttons
        dialog.findViewById(R.id.dialog_alert_positive).setVisibility(View.GONE);
        dialog.findViewById(R.id.dialog_alert_negative).setVisibility(View.GONE);
    }

    public AlertController setTitle(int titleId) {
        ((TextView) dialog.findViewById(R.id.dialog_alert_title)).setText(titleId);

        return this;
    }

    public AlertController setMessage(int messageId) {
        ((TextView) dialog.findViewById(R.id.dialog_alert_content)).setText(messageId);

        return this;
    }

    public AlertController setMessage(CharSequence message) {
        ((TextView) dialog.findViewById(R.id.dialog_alert_content)).setText(message);

        return this;
    }

    public AlertController setPositiveButton(int resId, DialogInterface.OnClickListener yesListener) {
        Button button = dialog.findViewById(R.id.dialog_alert_positive);
        button.setVisibility(View.VISIBLE);
        button.setText(resId);
        button.setOnClickListener(v -> {
            yesListener.onClick(dialog, dialog.hashCode());
            dialog.cancel();
        });

        return this;
    }

    public AlertController setNegativeButton(int resId, DialogInterface.OnClickListener noListener) {
        Button button = dialog.findViewById(R.id.dialog_alert_negative);
        button.setVisibility(View.VISIBLE);
        button.setText(resId);
        button.setOnClickListener(v -> {
            noListener.onClick(dialog, dialog.hashCode());
            dialog.cancel();
        });

        return this;
    }

    public void show() {
        dialog.show();
    }
}
