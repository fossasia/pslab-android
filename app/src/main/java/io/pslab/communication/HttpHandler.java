package io.pslab.communication;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpHandler {

    private final String TAG = this.getClass().getSimpleName();
    private String baseIP;
    private String sendDataEndPoint = "send";
    private String getDataEndPoint = "get";
    private String dataKeyString = "data";
    private OkHttpClient client;
    private JSONObject receivedData;

    public HttpHandler(String baseIP) {
        this.baseIP = baseIP;
        this.client = new OkHttpClient();
    }

    /**
     * Method to send data to ESP
     *
     * @param data data to be sent in byte array
     * @return 1 if response code is "200" 0 otherwise;
     */
    public int write(byte[] data) throws IOException, JSONException {
        int result = 1;
        URL baseURL = new URL("http://" + baseIP + "/" + sendDataEndPoint);
        int written = 0;
        JSONArray responseArray = new JSONArray();
        while (written < data.length) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(dataKeyString, data[written]);
            RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(baseURL)
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            responseArray.put(new JSONObject(response.body().string()));
            if (response.code() != 200) {
                Log.e(TAG, "Error writing byte:" + written);
                return 0;
            }
            written++;
        }
        receivedData = new JSONObject(responseArray.toString());
        return result;
    }

    /**
     * Method to get data from ESP
     * @return 1 if data was received 0 otherwise
     */
    public int read() throws IOException, JSONException {
        int result = 1;
        URL baseURL = new URL("http://" + baseIP + "/" + getDataEndPoint);
        Request request = new Request.Builder()
                .url(baseURL)
                .build();
        Response response = client.newCall(request).execute();
        if (response.code() != 200) {
            Log.e(TAG, "Error reading data");
            return 0;
        } else {
            receivedData = new JSONObject(response.body().string());
        }
        return  result;
    }

    public JSONObject getReceivedData() {
        return receivedData;
    }
}
