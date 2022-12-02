export interface MessageSubscription {
    id: string;
    callback: (payload: object) => void
}

export interface CordovaParams {
    /** set this to true if you don't want to use cordova.exec in this function call */
    preventCordovaExec?: boolean;
    /** callback to handle cordova.exec success */
    onSuccess?: () => void;
    /** callback to handle cordova.exec error */
    onError?: (err: any) => void;
}

export class MessagingCenter {
    
    subscriptions: {[key:string]: MessageSubscription};

    /**
     * Subscribes to a topic
     * @param topic the topic you'd like to subscribe to
     * @param callback the callback to be called when there's a publish to this topic
     * @returns the subscription ID for unsubscribing
     */
    subscribe: (
        topic: string,
        callback: (payload: object) => void
    ) => string;

    /**
     * Unsubscribes from a topic
     * @param topic the topic you'd like to unsibscribe from
     * @param id the ID of the specific subscription
     */
    unsubscribe: (
        topic: string,
        id: string
    ) => void;

     /**
     * Publishes payload to a topic
     * @param topic the topic you'd like to publish to
     * @param payload the payload you'd like to publish (must be an object)
     * @param cordovaParams cordova-related params.
     */
    publish: (
        topic: string,
        payload: object,
        cordovaParams?: CordovaParams
    ) => void;

}