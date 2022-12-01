export interface Subscription {
    id: string;
    callback: (payload: object) => void
}

export class MessagingCenter {
    
    subscriptions: {[key:string]: Subscription};

    /**
     * Subscribes to a topic
     * @param topic the topic you'd like to subscribe to
     * @param callback the callback to be called when there's a publish to this topic
     * @param onError callback to handle error
     * @returns the subscription ID for unsubscribing
     */
    subscribe: (
        topic: string,
        callback: (payload: object) => void,
        onError: (err: any) => void
    ) => string;

    /**
     * Unsubscribes from a topic
     * @param topic the topic you'd like to unsibscribe from
     * @param id the ID of the specific subscription
     * @param onError callback to handle error
     */
    unsubscribe: (
        topic: string,
        id: string,
        onError: (err: any) => void
    ) => void;

     /**
     * Publishes payload to a topic
     * @param topic the topic you'd like to publish to
     * @param payload the payload you'd like to publish (must be an object)
     * @param onError callback to handle error
     * @param preventCordovaExec set this to true if you don't want the publish to execute on cordova's runtime 
     */
    publish: (
        topic: string,
        payload: object,
        onError: (err: any) => void
    ) => void;

}