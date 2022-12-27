const SERVICE_NAME = "MessagingCenter";
var subscriptionIdCounter = 0;

class MessagingCenter {
    subscriptions = {}

    /**
     * Subscribes to a topic
     * @param {string} topic the topic you'd like to subscribe to
     * @param {(payload: object) => void} callback the callback to be called when there's a publish to this topic
     * @returns the subscription ID for unsubscribing
     */
    subscribe(topic, callback) {
        // Generate a new Web-specific id
        const callbackId = `web_${subscriptionIdCounter}`;
        subscriptionIdCounter++;

        const sub = {
            id: callbackId,
            callback
        };
        if (!this.subscriptions[topic]) {
            this.subscriptions[topic] = [];
        } 
        this.subscriptions[topic].push(sub);
        return sub.id;
    }

    /**
     * Unsubscribes from a topic
     * @param {string} topic the topic you'd like to unsibscribe from
     * @param {string} id the ID of the specific subscription
     */
    unsubscribe(topic, id) {
        if (topic in this.subscriptions) {
            this.subscriptions[topic] = this.subscriptions[topic].filter(sub => sub.id !== id);
        }
    }

    /**
     * Publishes payload to a topic
     * @param {string} topic the topic you'd like to publish to
     * @param {object} payload the payload you'd like to publish (must be an object)
     * @param {{preventCordovaExec: boolean, onSuccess: () => void, onError: (err: any) => void}} cordovaParams cordova-related parameters
     * @param cordovaParams.preventCordovaExec set this to true if you don't want to use cordova.exec in this function call
     * @param cordovaParams.onSuccess callback to handle cordova.exec success
     * @param cordovaParams.onError callback to handle cordova.exec error
     */
    publish(topic, payload, cordovaParams) {
        if (topic in this.subscriptions) {           
            this.subscriptions[topic].forEach(sub => {
            // Invoke the Web subscriptions
                sub.callback(payload);
            });
        }
        if (cordovaParams && !cordovaParams.preventCordovaExec) {
            // Calls cordova plugin to invoke platform-specific subscriptions
            cordova.exec(cordovaParams.onSuccess, cordovaParams.onError, SERVICE_NAME, "publish", [topic, payload]);
        }
    }
}

module.exports = new MessagingCenter();