package com.gijsvanveen.fallbackDemo;

/**
 * Created by Gijs on 22-2-2016.
 */

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implement the Command Pattern to allow for stackable requests. See https://en.wikipedia.org/wiki/Command_pattern
 */
interface FallbackRequest {
    public void performRequestForHandler(int index, RequestEventHandler handler);
}

class FallbackRequestHelper extends RequestHelper{


    ArrayList<RequestHelper> handlers;
    ArrayList<Integer> attemptsSinceLastFailure;
    ArrayList<Integer> retryAfterAttempts;
    ArrayList<Lock> locks;

    Calendar timeSinceLastRequest;

    ReentrantLock timeLock = new ReentrantLock();

    public FallbackRequestHelper(ArrayList<RequestHelper> handlers)
    {
        this.handlers = handlers;
        attemptsSinceLastFailure = new ArrayList<Integer>();
        retryAfterAttempts = new ArrayList<Integer>();
        locks = new ArrayList<Lock>();

        for (RequestHelper handler : handlers) {
            attemptsSinceLastFailure.add(0);
            retryAfterAttempts.add(0);
            locks.add(new ReentrantLock());
        }

        timeSinceLastRequest = Calendar.getInstance();
    }
    /**
     * Generic function for performing requests through the fallback request helpers.
     * This function takes the index of the requestHelper to use,
     * the handler that should be executed once the request has completed without requiring fallback and
     * a FallbackRequest to perform the request at a given index.
     *
     * Use this function to automatically perform a request in a fallback loop.
     *
     * Parameters:
     * index: The index of the RequestHelper to use.
     * finalHandler: The RequestEventHandler to perform once the request has succeeded or if a failure requires no fallback
     * request: A FallbackRequest that takes the index of the requestHelper and a completion handler to handle the request.
     */
    private void performRequest(int index, RequestEventHandler finalHandler, FallbackRequest request) {
        // If more than an hour passed since the last request, it is probably best to reset.
        Calendar currentDate = Calendar.getInstance();
        timeSinceLastRequest.add(Calendar.HOUR_OF_DAY, 1);
        if (currentDate.after(timeSinceLastRequest)) {
            resetAttempts(0);
        }

        timeLock.lock();
        timeSinceLastRequest = Calendar.getInstance();
        timeLock.unlock();

        if (canUseHelper(index) || index + 1 >= handlers.size())  {


            request.performRequestForHandler(index, new RequestEventHandler() {
                @Override
                public void onSuccess(String handlerName) {
                    resetAttempts(index);
                    finalHandler.onSuccess(handlerName);
                }

                @Override
                public void onError(RequestError error) {
                    // Check if the error should result in a fallback. If so, use this function recursively to fall back
                    if (index + 1 < handlers.size() && error.getShouldCauseFallback()) {
                        System.out.println(error.getName());
                        raiseMaxAttempts(index);
                        performRequest(index + 1, finalHandler, request);
                    } else {
                        resetAttempts(index);
                        finalHandler.onError(error);
                    }
                }
            });
        } else {
            performRequest(index + 1, finalHandler, request);
        }

    }
    /* Determine the index of the handler to use */
    private int startingHandlerIndex(boolean useFallbackByDefault) {
        if (useFallbackByDefault) {
            return handlers.size() - 1;
        } else {
            return 0;
        }
    }

    /* Check if the helper at a given index can be used or if it should be skipped */
    private boolean canUseHelper(int atIndex) {
        Lock lock = locks.get(atIndex);

        // This is a concurrency issue to please lock stuff
        lock.lock();
        boolean result = true;
        if (attemptsSinceLastFailure.get(atIndex) < retryAfterAttempts.get(atIndex)) {
            int failures = attemptsSinceLastFailure.get(atIndex);
            attemptsSinceLastFailure.set(atIndex, failures + 1);
            result = false;
        }
        lock.unlock();

        return result;
    }

    /* Reset the number of attempts for a given index */
    private void resetAttempts(int atIndex) {
        Lock lock = locks.get(atIndex);
        lock.lock();

        attemptsSinceLastFailure.set(atIndex, 0);
        retryAfterAttempts.set(atIndex,0);

        lock.unlock();

        if (atIndex + 1 < attemptsSinceLastFailure.size()) {
            resetAttempts(atIndex + 1);
        }
    }

    /* Increase the maximum amount of attempts for a requestHelper at a given index */
    private void raiseMaxAttempts(int atIndex) {
        Lock lock = locks.get(atIndex);
        lock.lock();

        attemptsSinceLastFailure.set(atIndex, 0);

        if (retryAfterAttempts.get(atIndex) == 0) {
            retryAfterAttempts.set(atIndex,1);
        } else {
            int allowedFailures = retryAfterAttempts.get(atIndex);
            retryAfterAttempts.set(atIndex, allowedFailures*2);
        }


        int MAX_RETRY_WAIT = 32;
        if (retryAfterAttempts.get(atIndex) > MAX_RETRY_WAIT) {
            retryAfterAttempts.set(atIndex, MAX_RETRY_WAIT);
        }

        lock.unlock();
    }

    public void RandomRequest(RequestEventHandler externalHandler)
    {
        // Could be a Labda in Java 8 but wont do that since Android should use Java 7
        FallbackRequest request = new FallbackRequest() {
            public void performRequestForHandler(int index, RequestEventHandler internalHandler) {
                RequestHelper handler = handlers.get(index);
                handler.RandomRequest(internalHandler);
            }
        };

        performRequest(startingHandlerIndex(false), externalHandler, request);
    }
}