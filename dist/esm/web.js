import { WebPlugin } from '@capacitor/core';
export class AndroidShortcutsWeb extends WebPlugin {
    isDynamicSupported() {
        throw new Error('Method not implemented.');
    }
    isPinnedSupported() {
        throw new Error('Method not implemented.');
    }
    setDynamic(options) {
        console.error("Method 'add' not implemented.", JSON.stringify(options));
        return Promise.reject("Method 'add' not implemented.");
    }
    pin(options) {
        console.error("Method 'add' not implemented.", options);
        return Promise.reject("Method 'add' not implemented.");
    }
    update(options) {
        console.error("Method 'update' not implemented.", JSON.stringify(options));
        return Promise.reject("Method 'update' not implemented.");
    }
    disable(options) {
        console.error("Method 'disable' not implemented.", JSON.stringify(options));
        return Promise.reject("Method 'disable' not implemented.");
    }
    addListener(eventName, listenerFunc) {
        listenerFunc({ data: '' });
        return Promise.reject(`Listener for '${eventName}' not implemented.`);
    }
    addDynamic(options) {
        console.error("Method 'add' not implemented.", JSON.stringify(options));
        return Promise.reject("Method 'add' not implemented.");
    }
    addPinned(options) {
        console.error("Method 'add' not implemented.", options);
        return Promise.reject("Method 'add' not implemented.");
    }
}
//# sourceMappingURL=web.js.map