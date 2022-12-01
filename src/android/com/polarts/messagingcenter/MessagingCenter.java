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

    private static IPayloadCallback noop = payload -> {
        // Do nothing
    };

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
            case "subscribe":
                this.subscribe(topic, noop, data.getString(1));
                callbackContext.success();
                return true;

            case "unsubscribe":
                this.unsubscribe(topic, data.getString(1));
                callbackContext.success();
                return true;

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
     * @param callback override success(JSONObject) for the callback that'd be called
     * @param id an existing id for the subscription (new one is generated if null)
     * @return the subscription ID for unsubscribing
     */
    public static String subscribe(String topic, IPayloadCallback callback, String id) {
        ArrayList<MessageSubscription> subs = subscriptions.get(topic);
        if (id == null) { // if id is null generate a new one
            id = "android_" + subscriptionIdCounter;
            subscriptionIdCounter++;
        }
        if (subs == null) {
            subs = new ArrayList<>();
            subscriptions.put(topic, subs);
        }
        subs.add(new MessageSubscription(id, callback));
        Log.d(TAG, "Subscribed to " + id);
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
        if (subs != null) {
            cordova.getActivity().runOnUiThread(() -> {
                subs.forEach(sub -> {
                    sub.callback.invoke(payload);
                    // Due to Cordova's limitation of being able to call the success callback only once,
                    // I call the web "publish" function instead via JS injection with the payload stringified.
                    webView.loadUrl("javascript:window.cordova.plugins.messagingCenter.publish(\""+topic+"\", "+payload.toString()+", () => {}, true)");
                });
            });
        }
    }

}
