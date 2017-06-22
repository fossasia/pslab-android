package org.fossasia.pslab.others;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by viveksb007 on 21/6/17.
 */

public class PreferenceManager {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    private static final String preferenceName = "PSLAB";
    private static final String version = "version";

    public PreferenceManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
    }

    public void setVersion(String version) {
        editor = sharedPreferences.edit();
        editor.putString(version, version);
        editor.apply();
    }

    public String getVersion() {
        return sharedPreferences.getString(version, "none");
    }

}
