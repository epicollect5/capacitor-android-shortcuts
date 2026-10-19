package nepheus.capacitor.androidshortcuts;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.util.Base64;

import androidx.annotation.RequiresApi;
import com.getcapacitor.Bridge;
import com.getcapacitor.JSArray;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class AndroidShortcuts {

    public Boolean isDynamicSupported(Context context) {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1;
    }

    public Boolean isPinnedSupported(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
            return shortcutManager.isRequestPinShortcutSupported();
        }

        return false;
    }

    public void setDynamic(Bridge bridge, JSArray items) throws PackageManager.NameNotFoundException, JSONException {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1) {
            throw new UnsupportedOperationException("Dynamic shortcuts are not supported on this device");
        }

        ShortcutManager shortcutManager = bridge.getContext().getSystemService(ShortcutManager.class);

        ArrayList<ShortcutInfo> shortcuts = this.buildShortcuts(bridge, items);

        shortcutManager.setDynamicShortcuts(shortcuts);
    }

    /**
     * Updates already published shortcuts, keeping their IDs.
     * Shortcuts which are not published yet are ignored by the system.
     */
    public void update(Bridge bridge, JSArray items) throws PackageManager.NameNotFoundException, JSONException {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1) {
            throw new UnsupportedOperationException("Dynamic shortcuts are not supported on this device");
        }

        ShortcutManager shortcutManager = bridge.getContext().getSystemService(ShortcutManager.class);

        ArrayList<ShortcutInfo> shortcuts = this.buildShortcuts(bridge, items);

        shortcutManager.updateShortcuts(shortcuts);
    }

    /**
     * Disables the given shortcuts, so the launcher greys them out and offers
     * the user the chance to remove them. Pinned shortcuts cannot be removed
     * programmatically, so disabling is the closest the platform allows.
     */
    public void disable(Bridge bridge, List<String> ids, String message) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1) {
            throw new UnsupportedOperationException("Dynamic shortcuts are not supported on this device");
        }

        ShortcutManager shortcutManager = bridge.getContext().getSystemService(ShortcutManager.class);

        //disableShortcuts() rejects the whole batch if any ID is unknown to the
        //system (e.g. the shortcut was never pinned, or the user already removed
        //it), so only pass the IDs the system still knows about
        List<String> existingIds = new ArrayList<String>();
        List<ShortcutInfo> publishedShortcuts = new ArrayList<ShortcutInfo>();
        publishedShortcuts.addAll(shortcutManager.getDynamicShortcuts());
        publishedShortcuts.addAll(shortcutManager.getPinnedShortcuts());
        for (ShortcutInfo shortcut : publishedShortcuts) {
            if (ids.contains(shortcut.getId()) && !existingIds.contains(shortcut.getId())) {
                existingIds.add(shortcut.getId());
            }
        }

        if (existingIds.isEmpty()) {
            return;
        }

        shortcutManager.disableShortcuts(existingIds, message);

        //Pinned shortcuts are not removed by this call; dynamic ones are.
        //IDs which only exist as pinned shortcuts are not dynamic shortcuts,
        //which the platform rejects as a whole batch: the disable above has
        //already been applied, so that rejection must not fail the call.
        try {
            shortcutManager.removeDynamicShortcuts(existingIds);
        } catch (IllegalArgumentException e) {
            //no dynamic shortcut for (at least) one of the IDs: nothing to remove
        }
    }

    public void pin(Bridge bridge, String id, String shortLabel, String longLabel, Icon icon, String data)
        throws UnsupportedOperationException {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            throw new UnsupportedOperationException("Pinned shortcuts are not supported on this device");
        }

        ShortcutManager shortcutManager = bridge.getContext().getSystemService(ShortcutManager.class);

        if (!shortcutManager.isRequestPinShortcutSupported()) {
            throw new UnsupportedOperationException("Pinned shortcuts are not supported on this device");
        }

        ShortcutInfo shortcut = buildShortcut(bridge, id, shortLabel, longLabel, icon, data);

        shortcutManager.requestPinShortcut(shortcut, null);
    }

    @RequiresApi(api = android.os.Build.VERSION_CODES.M)
    private ArrayList<ShortcutInfo> buildShortcuts(Bridge bridge, JSArray items)
        throws PackageManager.NameNotFoundException, JSONException {
        int count = items.length();
        ArrayList<ShortcutInfo> shortcuts = new ArrayList<ShortcutInfo>(count);

        for (int i = 0; i < count; ++i) {
            JSONObject item = items.getJSONObject(i);
            String id = item.getString("id");
            String shortLabel = item.getString("shortLabel");
            String longLabel = item.getString("longLabel");
            String data = item.getString("data");

            ShortcutIcon shortcutIcon = null;
            try {
                JSONObject iconObject = item.getJSONObject("icon");
                shortcutIcon = new ShortcutIcon(ShortcutIconEnum.valueOf(iconObject.getString("type")), iconObject.getString("name"));
            } catch (JSONException e) {
                System.out.println("'icon' Object is not parsable");
            }

            Icon icon = this.generateIcon(bridge, shortcutIcon);

            shortcuts.add(buildShortcut(bridge, id, shortLabel, longLabel, icon, data));
        }

        return shortcuts;
    }

    /**
     * Generates an Icon based on the given parameter
     * @param shortcutIcon parameter of the icon that should be set
     * @return
     */
    @RequiresApi(api = android.os.Build.VERSION_CODES.M)
    public Icon generateIcon(
            Bridge bridge, ShortcutIcon shortcutIcon) throws PackageManager.NameNotFoundException {
        if (shortcutIcon == null) {
            return this.getDefaultIcon(bridge);
        }

        try {
            if (shortcutIcon.getType().equals(ShortcutIconEnum.Bitmap)) {
                return Icon.createWithBitmap(decodeBase64Bitmap(shortcutIcon.getName()));
            } else if (shortcutIcon.getType().equals(ShortcutIconEnum.Resource)) {
                Resources activityRes = bridge.getContext().getResources();
                String activityPackage = bridge.getActivity().getPackageName();
                return Icon.createWithResource(bridge.getContext(), activityRes.getIdentifier(shortcutIcon.getName(), "drawable", activityPackage));
            } else {
                throw new InvalidParameterException("Parameter 'icon.type' is invalid");
            }
        } catch (Exception e) {
            return this.getDefaultIcon(bridge);
        }
    }

    @RequiresApi(api = android.os.Build.VERSION_CODES.M)
    private Icon getDefaultIcon(Bridge bridge) throws PackageManager.NameNotFoundException {
        Context context = bridge.getContext();
        PackageManager pm = context.getPackageManager();
        ApplicationInfo applicationInfo = pm.getApplicationInfo(bridge.getActivity().getPackageName(), PackageManager.GET_META_DATA);
        return Icon.createWithResource(bridge.getActivity().getPackageName(), applicationInfo.icon);
    }

    @RequiresApi(api = android.os.Build.VERSION_CODES.N_MR1)
    private ShortcutInfo buildShortcut(
        Bridge bridge,
        String id,
        String shortLabel,
        String longLabel,
        Icon icon,
        String data
    ) throws InvalidParameterException {
        if (id.length() == 0) {
            throw new InvalidParameterException("Parameter 'Id' invalid");
        }

        if (shortLabel.length() == 0 || longLabel.length() == 0) {
            throw new InvalidParameterException("Parameter 'shortLabel' or 'longLabel' invalid");
        }

        Context context = bridge.getContext();
        ShortcutInfo.Builder builder = new ShortcutInfo.Builder(context, id);

        Intent intent = new Intent(
            Intent.EXTRA_SHORTCUT_INTENT,
            bridge.getIntentUri(),
            bridge.getContext(),
            bridge.getActivity().getClass()
        );
        intent.putExtra("data", data);

        return builder
            .setActivity(bridge.getActivity().getComponentName())
            .setIntent(intent)
            .setShortLabel(shortLabel)
            .setLongLabel(longLabel)
            .setIcon(icon)
            .build();
    }

    private static Bitmap decodeBase64Bitmap(String input) {
        byte[] decodedString = Base64.decode(input, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }
}
