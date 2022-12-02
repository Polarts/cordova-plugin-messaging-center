package com.polarts.messagingcenter;

import android.util.Log;

import java.util.*;
import java.util.function.*;

import org.apache.cordova.*;
import org.json.*;

public class MessagingCenter extends CordovaPlugin {

    private static String TAG = "MessagingCenter";

    public interface IPayloadCallback {
        public void invoke(JSONObject payload);
    }

    private static class MessageSubscription {
        public IPayloadCallback callback;
        public String id;
        public MessageSubscription(String id, IPayloadCallback callback) {
            this.id = id;
            this.callback = callback;
        }
    }

    private static int subscriptionIdCounter = 0;

    private static Map<String, ArrayList<MessageSubscription>> subscriptions = new HashMap<>();

    private static CordovaWebView webView;
    private static CordovaInterface cordova;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.webView = webView;
        this.cordova = cordova;
        Log.d(TAG, "Initializing " + TAG);
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        
        String topic = data.getString(0);

        switch(action) {

            case "publish":
                this.publish(topic, data.getJSONObject(1));
                callbackContext.success();
                return true;

            default:
                callbackContext.error("Invalid action: " + action);
                return false;
        }
    }

    /**
     * Subscribes to a topic
     * @param topic the topic you'd like to subscribe to
     * @param callback the callback to be called when there's a publish to this topic
     * @return the subscription ID for unsubscribing
     */
    public static String subscribe(String topic, IPayloadCallback callback) {
        // Generate a new Android-specific id
        String id = "android_" + subscriptionIdCounter;
        subscriptionIdCounter++;
        
        ArrayList<MessageSubscription> subs = subscriptions.get(topic);
        if (subs == null) {
            subs = new ArrayList<>();
            subscriptions.put(topic, subs);
        }
        subs.add(new MessageSubscription(id, callback));
        Log.d(TAG, "Subscribed to topic " + topic + " with id " + id);
        return id;
    }

    /**
     * Unsubscribes from a topic
     * @param topic the topic you'd like to unsibscribe from
     * @param id the ID of the specific subscription
     */
    public static void unsubscribe(String topic, String id) {
        ArrayList<MessageSubscription> subs = subscriptions.get(topic);
        if (subs != null) {
            subs.removeIf(s -> s.id == id);
        }
    }

    /**
     * Publishes payload to a topic
     * @param topic the topic you'd like to publish to
     * @param payload the payload you'd like to publish
     */
    public static void publish(String topic, JSONObject payload) {
        ArrayList<MessageSubscription> subs = subscriptions.get(topic);
        cordova.getActivity().runOnUiThread(() -> {
            if (subs != null) {
                subs.forEach(sub -> {
                    // Invoke the Android subscriptions
                    sub.callback.invoke(payload);
                });
            }
            // Calls the web counterpart of the "publish" function via JS injection with the payload stringified.
            // This makes sure all the web subscriptions are triggered as well.
            webView.loadUrl(
                "javascript:window.cordova.plugins.messagingCenter.publish("
                    + "\"" + topic + "\","
                    + payload.toString() + ","
                    + "{preventCordovaExec: true}" // prevents infinite loop
                + ")"
            );
        });
    }

}
