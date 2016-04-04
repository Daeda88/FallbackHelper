package com.gijsvanveen.fallbackDemo;

/**
 * Created by Gijs on 22-2-2016.
 */
public class HttpRequestHelper extends RequestHelper {

    private String destination;

    public HttpRequestHelper(String destination) {
        this.destination = destination;
    }

    @Override
    public void RandomRequest(RequestEventHandler handler) {
        sendRequest(destination, handler);
    }
}
