package nepheus.capacitor.androidshortcuts;

import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;

import androidx.annotation.RequiresApi;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

@CapacitorPlugin(name = "AndroidShortcuts")
public class AndroidShortcutsPlugin extends Plugin {

    private AndroidShortcuts implementation = new AndroidShortcuts();

    //the intent which launched the activity, already emitted from load().
    //BridgeActivity.load() replays it through onNewIntent(getIntent()), so
    //handleOnNewIntent() must not emit it a second time.
    private Intent launchIntent;
    private boolean launchIntentReplayed;

    /**
     * Cold start: the app was launched by tapping a shortcut, so no JS
     * listener exists yet. Emitting here retains the data until the JS
     * 'shortcut' listener subscribes (Capacitor retains unhandled events).
     */
    @Override
    public void load() {
        super.load();
        launchIntent = getActivity() != null ? getActivity().getIntent() : null;
        launchIntentReplayed = false;
        emitShortcut(launchIntent);
    }

    @PluginMethod
    public void isDynamicSupported(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("result", implementation.isDynamicSupported(this.getContext()));
        call.resolve(ret);
    }

    @PluginMethod
    public void isPinnedSupported(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("result", implementation.isPinnedSupported(this.getContext()));
        call.resolve(ret);
    }

    @PluginMethod
    public void setDynamic(PluginCall call) {
        JSArray items = call.getArray("items");

        if (items == null) {
            call.reject("'items' array must be set");
            return;
        }

        try {
            implementation.setDynamic(this.getBridge(), items);
        } catch (Exception e) {
            call.reject(e.getMessage());
            return;
        }

        call.resolve();
    }

    @PluginMethod
    public void update(PluginCall call) {
        JSArray items = call.getArray("items");

        if (items == null) {
            call.reject("'items' array must be set");
            return;
        }

        try {
            implementation.update(this.getBridge(), items);
        } catch (Exception e) {
            call.reject(e.getMessage());
            return;
        }

        call.resolve();
    }

    @PluginMethod
    public void disable(PluginCall call) {
        JSArray ids = call.getArray("ids");

        if (ids == null || ids.length() == 0) {
            call.reject("'ids' array must be set");
            return;
        }

        List<String> idList = new ArrayList<String>(ids.length());
        try {
            for (int i = 0; i < ids.length(); i++) {
                idList.add(ids.getString(i));
            }
        } catch (JSONException e) {
            call.reject("'ids' array is not parsable");
            return;
        }

        try {
            implementation.disable(this.getBridge(), idList, call.getString("message", ""));
        } catch (Exception e) {
            call.reject(e.getMessage());
            return;
        }

        call.resolve();
    }

    @PluginMethod
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void pin(PluginCall call) {
        String id = call.getString("id");
        String shortLabel = call.getString("shortLabel");
        String longLabel = call.getString("longLabel");
        String data = call.getString("data");

        ShortcutIcon shortcutIcon = null;
        try {
            JSONObject iconObject = call.getObject("icon");
            if (iconObject == null) {
                call.reject("'icon' object must be set");
                return;
            }
            shortcutIcon = new ShortcutIcon(ShortcutIconEnum.valueOf(iconObject.getString("type")), iconObject.getString("name"));
        } catch (JSONException e) {
            call.reject("'icon' object is not parsable");
            return;
        }

        try {
            Icon icon = implementation.generateIcon(bridge, shortcutIcon);
            implementation.pin(this.getBridge(), id, shortLabel, longLabel, icon, data);
        } catch (Exception e) {
            call.reject(e.getMessage());
            return;
        }

        call.resolve();
    }

    /**
     * Listen for EXTRA_SHORTCUT_INTENT intents
     * @param intent
     */
    @Override
    protected void handleOnNewIntent(Intent intent) {
        super.handleOnNewIntent(intent);

        //the launch intent was already emitted from load(), and Capacitor
        //replays it once through onNewIntent right after the bridge is
        //created: skip that single replay only, so a later tap of the very
        //same Intent instance is still delivered
        if (intent != null && intent == launchIntent && !launchIntentReplayed) {
            launchIntentReplayed = true;
            return;
        }

        emitShortcut(intent);
    }

    private void emitShortcut(Intent intent) {
        if (intent == null || !Intent.EXTRA_SHORTCUT_INTENT.equals(intent.getAction())) {
            return;
        }

        JSObject ret = new JSObject();
        ret.put("data", intent.getStringExtra("data"));
        notifyListeners("shortcut", ret, true);
    }
}
