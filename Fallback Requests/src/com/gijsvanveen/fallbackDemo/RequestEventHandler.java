package com.gijsvanveen.fallbackDemo;

/**
 * Created by Gijs on 22-2-2016.
 */
public interface RequestEventHandler {

    public void onSuccess(String helper);
    public void onError(RequestError error);

}
