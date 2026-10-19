import { WebPlugin } from '@capacitor/core';
import type { PluginListenerHandle } from '@capacitor/core';
import type { ShortcutItem, AndroidShortcutsPlugin } from './definitions';
export declare class AndroidShortcutsWeb extends WebPlugin implements AndroidShortcutsPlugin {
    isDynamicSupported(): Promise<{
        result: boolean;
    }>;
    isPinnedSupported(): Promise<{
        result: boolean;
    }>;
    setDynamic(options: {
        items: ShortcutItem[];
    }): Promise<void>;
    pin(options: ShortcutItem): Promise<void>;
    update(options: {
        items: ShortcutItem[];
    }): Promise<void>;
    disable(options: {
        ids: string[];
        message?: string;
    }): Promise<void>;
    addListener(eventName: 'shortcut', listenerFunc: (response: {
        data: string;
    }) => void): Promise<PluginListenerHandle>;
    addDynamic(options: {
        items: ShortcutItem[];
    }): Promise<void>;
    addPinned(options: ShortcutItem): Promise<void>;
}
