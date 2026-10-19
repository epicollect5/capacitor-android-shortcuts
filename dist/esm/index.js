import { registerPlugin } from '@capacitor/core';
const AndroidShortcuts = registerPlugin('AndroidShortcuts', {
    web: () => import('./web').then(m => new m.AndroidShortcutsWeb()),
});
export * from './definitions';
export { AndroidShortcuts };
//# sourceMappingURL=index.js.map