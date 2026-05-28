package com.mmsconfigfix;

import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;

import java.net.URL;
import java.net.URLConnection;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MmsConfigHook implements IXposedHookLoadPackage {

    private static final int MAX_MESSAGE_SIZE = 1258291;
    private static final int MAX_IMAGE_WIDTH = 2592;
    private static final int MAX_IMAGE_HEIGHT = 1944;
    private static final String PIXEL_UA = "Mozilla/5.0 (Linux; Android 14; Pixel 9 Pro Build/AD1A.240905.004) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.6668.54 Mobile Safari/537.36";
    private static final String PIXEL_UA_PROF = "http://uaprof.google.com/pixel9pro.xml";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals("com.android.phone")) return;

        // Hook CarrierConfigManager.getConfig()
        XposedHelpers.findAndHookMethod(
            CarrierConfigManager.class,
            "getConfig",
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    PersistableBundle config = (PersistableBundle) param.getResult();
                    if (config == null) config = new PersistableBundle();
                    config.putInt("maxMessageSize", MAX_MESSAGE_SIZE);
                    config.putInt("maxImageWidth", MAX_IMAGE_WIDTH);
                    config.putInt("maxImageHeight", MAX_IMAGE_HEIGHT);
                    param.setResult(config);
                }
            }
        );

        // Hook CarrierConfigManager.getConfigForSubId()
        try {
            XposedHelpers.findAndHookMethod(
                CarrierConfigManager.class,
                "getConfigForSubId",
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        PersistableBundle config = (PersistableBundle) param.getResult();
                        if (config == null) config = new PersistableBundle();
                        config.putInt("maxMessageSize", MAX_MESSAGE_SIZE);
                        config.putInt("maxImageWidth", MAX_IMAGE_WIDTH);
                        config.putInt("maxImageHeight", MAX_IMAGE_HEIGHT);
                        param.setResult(config);
                    }
                }
            );
        } catch (Throwable ignored) {}

        // Hook URL.openConnection() to inject Pixel headers into MMSC requests
        XposedHelpers.findAndHookMethod(
            URL.class,
            "openConnection",
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        URLConnection conn = (URLConnection) param.getResult();
                        String url = ((URL) param.thisObject).toString();
                        if (url.contains("63.59.138.138") || 
                            url.contains("vtext.com") || 
                            url.contains("mms")) {
                            conn.setRequestProperty("User-Agent", PIXEL_UA);
                            conn.setRequestProperty("x-wap-profile", PIXEL_UA_PROF);
                        }
                    } catch (Throwable ignored) {}
                }
            }
        );
    }
}
