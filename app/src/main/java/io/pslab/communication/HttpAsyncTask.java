package io.pslab.communication;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.pslab.interfaces.HttpCallback;

public class HttpAsyncTask extends AsyncTask<byte[], Void, Void> {

    private HttpHandler mHttpHandler;
    private HttpCallback<JSONObject> mHttpCallback;

    public HttpAsyncTask(String baseIP, HttpCallback<JSONObject> httpCallback) {
        mHttpHandler = new HttpHandler(baseIP);
        mHttpCallback = httpCallback;
    }

    @Override
    protected Void doInBackground(byte[]... data) {
        int res = 0;
        try {
            if (data.length != 0) {
                res = mHttpHandler.write(data[0]);

            } else {
                res = mHttpHandler.read();
            }
        } catch (IOException | JSONException e) {
            mHttpCallback.error(e);
            e.printStackTrace();
        }
        if (res == 1) {
            mHttpCallback.success(mHttpHandler.getReceivedData());
        } else {
            mHttpCallback.error(new Exception());
        }
        return null;
    }
}
