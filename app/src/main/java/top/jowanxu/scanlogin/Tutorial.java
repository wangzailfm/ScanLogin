package top.jowanxu.scanlogin;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.widget.Button;

import java.lang.reflect.Field;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Tutorial implements IXposedHookLoadPackage {

    private static final String COM_TENCENT_TIM = "com.tencent.tim";
    private static final String COM_TENCENT_QQ = "com.tencent.mobileqq";
    private static final String COM_TENCENT_BIZ_QRCODE_ACTIVITY_QRLOGIN_ACTIVITY = "com.tencent.biz.qrcode.activity.QRLoginActivity";
    private static final String DO_ON_CREATE = "doOnCreate";
    private static final String ANDROID_WIDGET_BUTTON = "android.widget.Button";
    private static final String HANDLE_MESSAGE = "handleMessage";
    private static final String CONTAIN_TEXT = "允许登录";
    private static final String HOOK_ERROR = "Hook 出错 ";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals(COM_TENCENT_TIM)
                || lpparam.packageName.equals(COM_TENCENT_QQ)) {
            // 获取Class
            final Class<?> aClass = XposedHelpers
                    .findClassIfExists(COM_TENCENT_BIZ_QRCODE_ACTIVITY_QRLOGIN_ACTIVITY,
                            lpparam.classLoader);
            if (aClass == null) {
                return;
            }
            // 获取Class里面的Field
            final Field[] declaredFields = aClass.getDeclaredFields();
            if (declaredFields == null) {
                return;
            }
            try {
                // Hook指定方法
                XposedHelpers.findAndHookMethod(aClass,
                        DO_ON_CREATE,
                        Bundle.class,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                Activity activity = (Activity) param.thisObject;
                                final String resultStr = getHookName(lpparam.packageName,
                                        activity.getPackageManager().getPackageInfo(lpparam.packageName, 0).versionName);
                                for (Field declaredField : declaredFields) {
                                    // 设置true
                                    declaredField.setAccessible(true);
                                    // 判断类型是否是Button
                                    if (declaredField.getGenericType().toString().contains(ANDROID_WIDGET_BUTTON)) {
                                        // 获取值
                                        final Button loginButton = (Button) declaredField.get(param.thisObject);
                                        if (loginButton == null) {
                                            return;
                                        }
                                        // 默认的Button的Text为空，需要在Handler这个类里面的方法后面加上判断
                                        Class<?> handlerClass = XposedHelpers.findClassIfExists(resultStr,
                                                lpparam.classLoader);
                                        if (handlerClass == null) {
                                            return;
                                        }
                                        try {
                                            // Hook方法，对handleMessage方法调用后，进行判断Button的Text进行判断，并且自动调用点击方法
                                            XposedHelpers.findAndHookMethod(handlerClass,
                                                    HANDLE_MESSAGE,
                                                    Message.class,
                                                    new XC_MethodHook() {
                                                        @Override
                                                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                                            // 当Button的Text为允许登录TIM/允许登录QQ的时候才实现点击
                                                            if (loginButton.getText().toString()
                                                                    .contains(CONTAIN_TEXT)) {
                                                                loginButton.performClick();
                                                            }
                                                        }
                                                    });
                                        } catch (Throwable t) {
                                            XposedBridge.log(HOOK_ERROR + t);
                                        }
                                    }
                                }
                            }
                        });
            } catch (Throwable t) {
                XposedBridge.log(HOOK_ERROR + t);
            }
        }
    }

    /**
     * 根据包名和版本号获取需要Hook的类名
     * @param packageName 包名
     * @param versionName 版本号
     * @return 类名
     */
    private String getHookName(String packageName, String versionName) {
        switch (packageName) {
            case COM_TENCENT_TIM:
                return getTIMHookName(versionName);
            default:
            case COM_TENCENT_QQ:
                return getQQHookName(versionName);
        }
    }
    /**
     * 根据版本号获取TIM需要Hook的类名
     * @param versionName 版本号
     * @return 类名
     */
    private String getTIMHookName(String versionName) {
        switch (versionName) {
            case "1.2.0":
                return "hzq";
            case "1.1.5":
                return "ghk";
            case "1.1.0":
                return "giy";
            case "1.0.5":
                return "gjd";
            case "1.0.4":
                return "gir";
            case "1.0.0":
                return "gik";
            default:
                return "hzq";
        }
    }

    /**
     * 根据版本号获取QQ需要Hook的类名
     * @param versionName 版本号
     * @return 类名
     */
    private String getQQHookName(String versionName) {
        switch (versionName) {
            case "7.2.0":
                return "myi";
            case "7.1.8":
                return "mco";
            case "7.1.5":
                return "mcf";
            case "7.1.0":
                return "lri";
            case "7.0.0":
                return "lhi";
            default:
                return "myi";
        }
    }
}
