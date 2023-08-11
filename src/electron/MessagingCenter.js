const SERVICE_NAME = "MessagingCenter";
var subscriptionIdCounter = 0;

const { app, ipcMain, ipcRenderer, contextBridge } = require('electron');

contextBridge.exposeInMainWorld(`electron${SERVICE_NAME}`, {
    invoke: (msg) => ipcRenderer.invoke(SERVICE_NAME, msg)
})

class MessagingCenter {
    subscriptions = {}

    constructor() {
        this.handleElectronMessage = this.handleElectronMessage.bind(this);
        app.whenReady().then(() => {
            ipcMain.handle(SERVICE_NAME, this.handleElectronMessage)
        })
    }

    handleElectronMessage(_, msg /* {topic: string, payload: T} */) {
        this.publish(msg.topic, msg.payload, { preventCordovaExec: true });
    }

    /**
     * Subscribes to a topic
     * @param {string} topic the topic you'd like to subscribe to
     * @param {(payload: object) => void} callback the callback to be called when there's a publish to this topic
     * @returns the subscription ID for unsubscribing
     */
    subscribe(topic, callback) {
        // Generate a new Electron-specific id
        const callbackId = `electron_${subscriptionIdCounter}`;
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
            if (this.subscriptions[topic].length == 0) {
                delete this.subscriptions[topic];
            }
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
            // Calls web subscription
            ipcMain.invoke(SERVICE_NAME, {topic, payload})
        }
    }
}

module.exports = new MessagingCenter();