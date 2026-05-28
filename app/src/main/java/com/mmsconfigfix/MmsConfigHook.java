package com.mmsconfigfix;

import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;

import java.net.HttpURLConnection;
import java.net.URL;

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
        if (!lpparam.packageName.equals("com.google.android.apps.messaging")) return;

        // Hook CarrierConfigManager
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

        // Hook HttpURLConnection to inject Pixel headers on MMSC requests
        XposedHelpers.findAndHookMethod(
            HttpURLConnection.class,
            "connect",
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    try {
                        HttpURLConnection conn = (HttpURLConnection) param.thisObject;
                        String url = conn.getURL().toString();
                        if (url.contains("mms.vtext.com") || url.contains("mmsc") || url.contains("mms")) {
                            conn.setRequestProperty("User-Agent", PIXEL_UA);
                            conn.setRequestProperty("x-wap-profile", PIXEL_UA_PROF);
                        }
                    } catch (Throwable ignored) {}
                }
            }
        );
    }
}
