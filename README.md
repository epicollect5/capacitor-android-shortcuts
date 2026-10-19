[![capacitor support](https://img.shields.io/badge/capacitor%20support-v8-brightgreen?logo=capacitor)](https://capacitorjs.com/)

# capacitor-android-shortcuts (Epicollect5 fork)

Fork of [NePheus/capacitor-android-shortcuts](https://github.com/NePheus/capacitor-android-shortcuts), upgraded to Capacitor 8. Install it from the fork, pinned to a commit hash:

```bash
npm install github:epicollect5/capacitor-android-shortcuts#<commit-sha>
```

Changes over upstream:

- Capacitor 8 (`@capacitor/core`/`@capacitor/android` `^8`).
- Cold-start tap delivery: the launch intent is read in `load()` and retained until the JS `shortcut` listener subscribes, so a tap that starts the app is delivered too (upstream only handled `handleOnNewIntent`).
- `update({ items })`: updates published shortcuts in place via `updateShortcuts`, keeping the same IDs.
- `disable({ ids, message })`: disables published shortcuts via `disableShortcuts` and removes any dynamic shortcut with the same IDs. Pinned shortcuts cannot be removed programmatically, so the launcher greys them out and offers the user the chance to remove them.

This plugin provides the feature to add dynamic and pinned shortcuts in Android apps. See more [in the Android docs](https://developer.android.com/guide/topics/ui/shortcuts). Its possible to listen to a shortcut tap action with an event listener (see `Usage`).

**Dynamic shortcut**<br/>
Tap and hold on the app icon and you will see the dynamic shortcuts in the dropdown<br/>
=> Usage: Call the `setDynamic` method one time to set the array of dynamic shortcuts

**Pinned shortcut**<br/>
You can add a pinned shortcut programmatically inside your app, i.e. let a customer add a favorite of an article/product/... to the home screen<br/>
=> Usage: Call the `pin` method where the user wants to pin something. An alert will be shown to add the pinned shortcut to the home screen.

## Supported platforms

| Platform | Supported |
| -------- | --------: |
| Android  |         ✔ |
| iOS      |         ✖ |
| Web      |         ✖ |

## Install

```bash
npm install github:epicollect5/capacitor-android-shortcuts#<commit-sha>
npx cap sync android
```

## Usage

```javascript
import { AndroidShortcuts } from '@epicollect5/capacitor-android-shortcuts';

...

// Set dynamic shortcuts
AndroidShortcuts.isDynamicSupported().then(({ result }) => {
    if (result) {
        AndroidShortcuts.setDynamic({
            items: [
                {
                    id: "myfirstid",
                    shortLabel: "My first short label",
                    longLabel: "My first long label",
                    icon: {
                        type: "Bitmap",
                        name: "<base64-string>"
                    },
                    data: "I am a simple string",
                },
                {
                    id: "mysecondid",
                    shortLabel: "My first short label",
                    longLabel: "My first long label",
                    icon: {
                        type: "Resource",
                        name: "<vector-asset-name>"
                    },
                    data: JSON.stringify({
                        myProperty: "Pass a stringified JSON object",
                    }),
                },
            ],
        });
    }
});
...

// Add a pinned shortcut
AndroidShortcuts.isPinnedSupported().then(({ result }) => {
    if (result) {
        AndroidShortcuts.pin({
            id: "mypinnedid",
            shortLabel: "My pinned short label",
            longLabel: "My pinned long label",
            icon: {
                type: "Bitmap",
                name: "<base64-string>"
            },
            data: "I am a simple string",
        });
    }
});

// Update published shortcuts in place (same IDs)
AndroidShortcuts.update({
    items: [ /* same shape as setDynamic */ ],
});

// Disable published shortcuts: pinned icons stay on the Home screen
// (the platform does not allow removing them) but are greyed out
AndroidShortcuts.disable({
    ids: ["mypinnedid"],
    message: "This project is no longer available",
});

// Triggered when the app is opened by a shortcut, cold start included
AndroidShortcuts.addListener('shortcut', (response: any) => {
  // response.data contains the content of the 'data' property of the created shortcut
});
```

## Usage of icons

See [Wiki: Icon examples](https://github.com/NePheus/capacitor-android-shortcuts/wiki/Icon-examples)

## API

<docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### isDynamicSupported()

```typescript
isDynamicSupported() => any
```

Checks if dynamic shortcuts are supported on the device

**Returns:** <code>any</code>

--------------------


### isPinnedSupported()

```typescript
isPinnedSupported() => any
```

Checks if pinned shortcuts are supported on the device

**Returns:** <code>any</code>

--------------------


### setDynamic(...)

```typescript
setDynamic(options: { items: ShortcutItem[]; }) => any
```

Set dynamic shortcuts

| Param         | Type                        | Description                                      |
| ------------- | --------------------------- | ------------------------------------------------ |
| **`options`** | <code>{ items: {}; }</code> | An items array with the options of each shortcut |

**Returns:** <code>any</code>

--------------------


### pin(...)

```typescript
pin(options: ShortcutItem) => any
```

Add a pinned shortcut

| Param         | Type                                                  | Description                              |
| ------------- | ----------------------------------------------------- | ---------------------------------------- |
| **`options`** | <code><a href="#shortcutitem">ShortcutItem</a></code> | An option object for the pinned shortcut |

**Returns:** <code>any</code>

--------------------


### update(...)

```typescript
update(options: { items: ShortcutItem[]; }) => any
```

Update already published shortcuts, keeping the same IDs.
Shortcuts that are not published yet are ignored by the system.

| Param         | Type                        | Description                                      |
| ------------- | --------------------------- | ------------------------------------------------ |
| **`options`** | <code>{ items: {}; }</code> | An items array with the options of each shortcut |

**Returns:** <code>any</code>

--------------------


### disable(...)

```typescript
disable(options: { ids: string[]; message?: string; }) => any
```

Disable already published shortcuts.
Disabled shortcuts are greyed out by the launcher, which offers the user
the chance to remove them. Pinned shortcuts cannot be removed
programmatically, so this is the closest removal the platform allows.

| Param         | Type                                        | Description                                                                                                                |
| ------------- | ------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------- |
| **`options`** | <code>{ ids: {}; message?: string; }</code> | An option object with the IDs of the shortcuts to disable and the message shown by the launcher when one of them is tapped |

**Returns:** <code>any</code>

--------------------


### addListener('shortcut', ...)

```typescript
addListener(eventName: 'shortcut', listenerFunc: (response: { data: string; }) => void) => any
```

Add a listener to a shortcut tap event

| Param              | Type                                                  |
| ------------------ | ----------------------------------------------------- |
| **`eventName`**    | <code>'shortcut'</code>                               |
| **`listenerFunc`** | <code>(response: { data: string; }) =&gt; void</code> |

**Returns:** <code>any</code>

--------------------


### removeAllListeners()

```typescript
removeAllListeners() => any
```

Removes all listeners.

**Returns:** <code>any</code>

--------------------


### Interfaces


#### ShortcutItem

| Prop             | Type                                                                                       | Description                                                                                                                                                                                                                                                                                                  |
| ---------------- | ------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **`id`**         | <code>string</code>                                                                        | ID of the shortcut                                                                                                                                                                                                                                                                                           |
| **`shortLabel`** | <code>string</code>                                                                        | Sets the short title of a shortcut. This is a mandatory field when publishing a new shortcut with ShortcutManager.addDynamicShortcuts(List) or ShortcutManager.setDynamicShortcuts(List). This field is intended to be a concise description of a shortcut. The recommended maximum length is 10 characters. |
| **`longLabel`**  | <code>string</code>                                                                        | Sets the text of a shortcut. This field is intended to be more descriptive than the shortcut title. The launcher shows this instead of the short title when it has enough space. The recommend maximum length is 25 characters.                                                                              |
| **`icon`**       | <code>{ type: <a href="#availableicontypes">AvailableIconTypes</a>; name: string; }</code> | Defines the icon of the shortcut. You can set the icon as a BASE64-Bitmap or as a Resource name                                                                                                                                                                                                              |
| **`data`**       | <code>string</code>                                                                        | Data that is passed to the 'shortcut' event                                                                                                                                                                                                                                                                  |


#### PluginListenerHandle

| Prop         | Type                      |
| ------------ | ------------------------- |
| **`remove`** | <code>() =&gt; any</code> |


### Type Aliases


#### AvailableIconTypes

<code>'Bitmap' | 'Resource'</code>

</docgen-api>
