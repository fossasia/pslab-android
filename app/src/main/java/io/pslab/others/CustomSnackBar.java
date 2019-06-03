package io.pslab.others;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import io.pslab.R;

/**
 * Created by Avjeet on 12-07-2018.
 */
public class CustomSnackBar {

    public static Snackbar snackbar;

    public static void showSnackBar(@NonNull View holderLayout, @NonNull String displayText,
                                    String actionText, View.OnClickListener clickListener, int duration) {
        snackbar = Snackbar.make(holderLayout, displayText, duration)
                .setAction(actionText, clickListener);
        snackbar.setActionTextColor(ContextCompat.getColor(holderLayout.getContext(), R.color.colorPrimary));
        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }
}
