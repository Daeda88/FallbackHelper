package com.gijsvanveen.fallbackDemo;

import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {

        RequestEventHandler handler = new RequestEventHandler() {
            @Override
            public void onSuccess(String helperName) {
                System.out.println(helperName);
            }

            @Override
            public void onError(RequestError error) {
                System.out.println(error.getName());
            }
        };

        HttpRequestHelper helperA = new HttpRequestHelper("http://www.google.com");
        HttpRequestHelper helperB = new HttpRequestHelper("http://www.nu.nl");

        ArrayList<RequestHelper> fallbackAHelpers = new ArrayList<RequestHelper>(Arrays.asList(helperA, helperB));
        FallbackRequestHelper fallbackA = new FallbackRequestHelper(fallbackAHelpers);

        fallbackA.RandomRequest(handler);

        HttpRequestHelper helperC = new HttpRequestHelper("http://httpstat.us/418");
        HttpRequestHelper helperD = new HttpRequestHelper("http://www.reclamefolder.nl");

        ArrayList<RequestHelper> fallbackBHelpers = new ArrayList<RequestHelper>(Arrays.asList(helperC, helperD));
        FallbackRequestHelper fallbackB = new FallbackRequestHelper(fallbackBHelpers);

        fallbackB.RandomRequest(handler);
        fallbackB.RandomRequest(handler);
        fallbackB.RandomRequest(handler);

    }
}
