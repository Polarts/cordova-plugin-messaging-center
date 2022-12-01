package com.polarts.messagingcenter;

import java.util.*;
import java.util.function.*;

import org.apache.cordova.*;
import org.json.*;

public class MessagingCenter extends CordovaPlugin {

    private static class MessageSubscription {
        public CallbackContext callback;
        public String id;
        public MessageSubscription(String id, CallbackContext callback) {
            this.id = id;
            this.callback = callback;
        }
    }

    private static int subscriptionIdCounter = 0;

    private static Map<String, ArrayList<MessageSubscription>> subscriptions = new HashMap<>();

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        
        String topic = data.getString(0);

        switch(action) {
            case "subscribe":
                this.subscribe(topic, callbackContext);
                return true;

            case "unsubscribe":
                this.unsubscribe(topic, data.getString(1));
                return true;

            case "publish":
                this.publish(topic, data.getJSONObject(1));
                return true;

            default:
                return false;
        }
    }

    /**
     * Subscribes to a topic
     * @param topic the topic you'd like to subscribe to
     * @param callbackContext override success(JSONObject) for the callback that'd be called
     * @return the subscription ID for unsubscribing
     */
    public static String subscribe(String topic, CallbackContext callbackContext) {
        ArrayList<MessageSubscription> subs = subscriptions.get(topic);
        String id = "android_" + subscriptionIdCounter;
        if (subs == null) {
            subs = new ArrayList<MessageSubscription>();
            subscriptions.put(topic, subs);
        }
        subs.add(new MessageSubscription(id, callbackContext));
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
            subs.removeIf(new Predicate<MessageSubscription>() {
                @Override
                public boolean test(MessageSubscription s) {
                    return s.id == id;
                };
            });
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
            subs.forEach(new Consumer<MessageSubscription>() {
                @Override
                public void accept(MessageSubscription sub) {
                    sub.callback.success(payload);
                }
            });
        }
    }

}
