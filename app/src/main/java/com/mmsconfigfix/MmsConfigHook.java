package com.mmsconfigfix;

import java.net.URL;
import java.net.URLConnection;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MmsConfigHook implements IXposedHookLoadPackage {

    private static final String PIXEL_UA = "Android-Mms/2.0";
    private static final String PIXEL_UA_PROF = "http://uaprof.google.com/Pixel_9_Pro_uaprof.xml";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals("com.android.phone")) return;

        try {
            XposedHelpers.findAndHookMethod(
                URL.class,
                "openConnection",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        try {
                            URLConnection conn = (URLConnection) param.getResult();
                            String url = ((URL) param.thisObject).toString();
                            if (url.contains("vtext.com") ||
                                url.contains("mms") ||
                                url.contains("63.59")) {
                                conn.setRequestProperty("x-wap-profile", PIXEL_UA_PROF);
                                conn.setRequestProperty("User-Agent", PIXEL_UA);
                            }
                        } catch (Throwable ignored) {}
                    }
                }
            );
        } catch (Throwable ignored) {}
    }
}
