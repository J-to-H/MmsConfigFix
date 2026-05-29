package com.mmsconfigfix;

import android.content.res.Resources;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MmsConfigHook implements IXposedHookLoadPackage {

    private static final String MMS_UA_PROF = "http://uaprof.vtext.com/OnePlus/odopcph2583/odopcph2583.xml";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals("com.android.phone")) return;

        try {
            XposedHelpers.findAndHookMethod(
                Resources.class,
                "getString",
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        try {
                            String result = (String) param.getResult();
                            if (result != null && result.contains("sonymobile")) {
                                param.setResult(MMS_UA_PROF);
                            }
                        } catch (Throwable ignored) {}
                    }
                }
            );
        } catch (Throwable ignored) {}
    }
}
