package io.pslab.others;

import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

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
        TextView textView = sbView.findViewById(R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }
}
