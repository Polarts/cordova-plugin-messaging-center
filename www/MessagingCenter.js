const serviceName = "MessagingService";
var subscriptionIdCounter = 0;

class MessagingCenter {
    subscriptions = {}

    /**
     * Subscribes to a topic
     * @param {string} topic the topic you'd like to subscribe to
     * @param {(payload: object) => void} callback the callback to be called when there's a publish to this topic
     * @param {(err: any) => void} onError callback to handle error
     * @returns the subscription ID for unsubscribing
     */
    subscribe(topic, callback, onError) {
        const sub = {
            id: `web_${subscriptionIdCounter}`,
            callback
        };
        if (!this.subscriptions[topic]) {
            this.subscriptions[topic] = [];
        } 
        this.subscriptions[topic].push(sub);
        subscriptionIdCounter++;
        cordova.exec(callback, onError, serviceName, "subscribe", [topic]);
        return sub.id;
    }

    /**
     * Unsubscribes from a topic
     * @param {string} topic the topic you'd like to unsibscribe from
     * @param {string} id the ID of the specific subscription
     * @param {(err: any) => void} onError callback to handle error
     */
    unsubscribe(topic, id, onError) {
        this.subscriptions[topic] = this.subscriptions[topic].filter(sub => sub.id !== id);
        cordova.exec(() => {}, onError, serviceName, "unsubscribe", [topic, id]);
    }

    /**
     * Publishes payload to a topic
     * @param {string} topic the topic you'd like to publish to
     * @param {object} payload the payload you'd like to publish (must be an object)
     * @param {(err: any) => void} onError callback to handle error
     */
    publish(topic, payload, onError) {
        this.subscriptions[topic].forEach(sub => {
            sub.callback(payload);
        });
        cordova.exec(() => {}, onError, serviceName, "publish", [topic, payload]);
    }
}

module.exports = new MessagingCenter();