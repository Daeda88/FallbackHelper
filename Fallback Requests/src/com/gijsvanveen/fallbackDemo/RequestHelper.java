package com.gijsvanveen.fallbackDemo;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Gijs on 22-2-2016.
 */
public abstract class RequestHelper {

    /**
     * Very basic HTTPS Request.
     * Just copied it mostly from Stack overflow now since I dont feel it is critical to the demo.
     * @param targetURL The url to connect to
     * @param handler The handler to handle the request
     * @return
     */
    public void sendRequest(String targetURL, RequestEventHandler handler) {
        HttpURLConnection connection = null;
        try {
            //Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            String urlParameters = "test=test";
            connection.setRequestProperty("Content-Length",
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.close();

            if (connection.getResponseCode() == 418) {
                handler.onError(new RequestError("Response Error " + targetURL, true));
            } else {
                handler.onSuccess(targetURL);
            }

        } catch (IOException e) {
            e.printStackTrace();
            handler.onError(new RequestError("IO Error", false));
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }

    public abstract void RandomRequest(RequestEventHandler handler);

}
