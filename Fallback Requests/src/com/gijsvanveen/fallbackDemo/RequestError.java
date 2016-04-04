package com.gijsvanveen.fallbackDemo;

/**
 * Created by Gijs on 22-2-2016.
 */
public class RequestError {

    private String name;
    private boolean isFallbackableError;

    public RequestError(String name, boolean isFallbackableError) {
        this.name = name;
        this.isFallbackableError = isFallbackableError;
    }

    public String getName() {
        return name;
    }

    public boolean getShouldCauseFallback() {
        return isFallbackableError;
    }
}
