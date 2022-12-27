# Cordova Messaging Center Plugin

This plugin exposes a publisher-subscriber interface to allow generic intercomunication between the JS code and the platform's native code.

Inspired by the Xamarin.Forms MessagingCenter API for cross-platform communication.

## Getting Started

**NOTICE:** Currently the plugin only supports Android. It won't compile on iOS.

1. Install the plugin from git by calling `cordova plugin add https://github.com/Polarts/cordova-plugin-messaging-center.git` (npm coming soon™).
2. In your `config.xml` file, add the following node:
```xml
<feature name="MessagingCenter">
    <param name="android-package" value="com.polarts.messagingcenter.MessagingCenter" />
    <param name="onload" value="true" />
</feature>
```
3. Done! You can now start using MessagingCenter via `window.cordova.plugins.messagingCenter`.

## Usage

### Subscribing to a topic:

Subscribing returns a subscription ID that is generated upon calling the function. It can be used later to unsubscribe the specific callback from the topic.

Subscriptions are managed locally on each platform. Calling `subscribe` won't trigger `cordova.exec`.

Web:
```js
const subscriptionId = window.cordova.plugins.messagingCenter.subscribe("myTopic", (payload) => { /* handle payload */ });
```

Android (java):
```java
String subscriptionId = MessagingCenter.subscribe("myTopic", payload -> { /* handle payload */ });
```

### Unsubscribing from a topic:

Use the same subscription ID you saved from the `subscribe` function's return value.

Calling `unsibscribe` removes the subscription from the locally managed list. It won't trigger `cordova.exec`.

Web:
```js
window.cordova.plugins.messagingCenter.unsubscribe("myTopic", subscriptionId);
```

Android (java):
```java
MessagingCenter.unsubscribe("myTopic", subscriptionId);
```

### Publishing to a topic:

Publishing sends the payload accross platforms. Unlike the other functions, `publish` can call `cordova.exec` and thus has the `cordovaParams` with success and error callbacks.

Web:
```js
window.cordova.plugins.messagingCenter.publish(
    "myTopic", 
    { /* payload object */ }, 
    { 
        onSuccess: () => { /* handle success */ },
        onError: (err) => { /* handle error */ }
    }
);
```

Android (java):
```java
JSONObject payload = new JSONObject();
// put data in the payload using payload.put("key", value);
MessagingCenter.publish("myTopic", payload);
```

The payload will be published to both Web and Native subscriptions.
