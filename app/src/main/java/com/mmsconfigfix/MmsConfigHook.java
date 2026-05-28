package com.mmsconfigfix;

import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MmsConfigHook implements IXposedHookLoadPackage {

    private static final int MAX_MESSAGE_SIZE = 1258291;
    private static final int MAX_IMAGE_WIDTH = 2592;
    private static final int MAX_IMAGE_HEIGHT = 1944;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals("com.google.android.apps.messaging")) return;

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
    }
}
