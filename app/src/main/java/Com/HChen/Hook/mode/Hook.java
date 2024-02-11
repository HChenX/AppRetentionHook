/*
 * This file is part of AppRetentionHook.

 * AppRetentionHook is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.

 * Author of this project: 焕晨HChen
 * You can reference the code of this project,
 * but as a project developer, I hope you can indicate it when referencing.

 * Copyright (C) 2023-2024 AppRetentionHook Contributions
 */
package Com.HChen.Hook.mode;

import static Com.HChen.Hook.HookInlet.modulePath;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import androidx.annotation.IntDef;

import org.luckypray.dexkit.result.MethodData;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import Com.HChen.Hook.callback.IHookLog;
import Com.HChen.Hook.mode.log.HookLog;
import dalvik.system.PathClassLoader;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public abstract class Hook extends HookLog {
    public String tag = getClass().getSimpleName(); // 获取继承类的类名

    @IntDef(value = {
        FLAG_ALL,
        FLAG_CURRENT_APP,
        FlAG_ONLY_ANDROID
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Duration {
    }

    // 尝试全部
    public static final int FLAG_ALL = 0;
    // 仅获取当前应用
    public static final int FLAG_CURRENT_APP = 1;
    // 获取 Android 系统
    public static final int FlAG_ONLY_ANDROID = 2;

    public XC_LoadPackage.LoadPackageParam loadPackageParam;

    public abstract void init();

    public void runHook(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        try {
            setLoadPackageParam(loadPackageParam);
            init();
            logI(tag, "Hook Done!");
        } catch (Throwable s) {
//            logE(tag, "Hook Failed: " + e);
        }
    }

    public void setLoadPackageParam(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        this.loadPackageParam = loadPackageParam;
    }

    /**
     * @noinspection JavaReflectionMemberAccess
     */
    public static Resources addModuleRes(Context context) {
        String tag = "addModuleRes";
        try {
            @SuppressLint("DiscouragedPrivateApi")
            Method AssetPath = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
            AssetPath.setAccessible(true);
            AssetPath.invoke(context.getResources().getAssets(), modulePath);
            return context.getResources();
        } catch (NoSuchMethodException e) {
            logE(tag, "Method addAssetPath is null: " + e);
        } catch (InvocationTargetException e) {
            logE(tag, "InvocationTargetException: " + e);
        } catch (IllegalAccessException e) {
            logE(tag, "IllegalAccessException: " + e);
        }
        return null;
    }

    /*不能处理报错的方法需要私有*/
    private Class<?> findClass(String className) {
        return findClass(className, loadPackageParam.classLoader);
    }

    public Class<?> findClass(String className, ClassLoader classLoader) {
        return XposedHelpers.findClass(className, classLoader);
    }

    public Class<?> findClassIfExists(String className) {
        try {
            return findClass(className);
        } catch (XposedHelpers.ClassNotFoundError e) {
            logE(tag, "Class not found: " + e);
            return null;
        }
    }

    public Class<?> findClassIfExists(String newClassName, String oldClassName) {
        try {
            return findClass(findClassIfExists(newClassName) != null ? newClassName : oldClassName);
        } catch (XposedHelpers.ClassNotFoundError e) {
            logE(tag, "Class " + newClassName + " & " + oldClassName + " not found: " + e);
            return null;
        }
    }

    public Class<?> findClassIfExists(String className, ClassLoader classLoader) {
        try {
            return findClass(className, classLoader);
        } catch (XposedHelpers.ClassNotFoundError e) {
            logE(tag, "Class " + className + " classLoader: " + className + " not found: " + e);
            return null;
        }
    }

    public abstract static class HookAction extends XC_MethodHook implements IHookLog {
        /*这种情况下报错应主动处理而不是上抛*/
        protected void before(MethodHookParam param) {
        }

        protected void after(MethodHookParam param) {
        }

        public HookAction() {
            super();
        }

        public HookAction(int priority) {
            super(priority);
        }

        public static HookAction returnConstant(final Object result) {
            return new HookAction(PRIORITY_DEFAULT) {
                @Override
                public String hookLog() {
                    return "returnConstant";
                }

                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(result);
                }
            };
        }

        public static final HookAction DO_NOTHING = new HookAction(PRIORITY_HIGHEST * 2) {

            @Override
            public String hookLog() {
                return "DO_NOTHING";
            }

            @Override
            protected void before(MethodHookParam param) {
                param.setResult(null);
            }

        };

        @Override
        protected void beforeHookedMethod(MethodHookParam param) {
            try {
                before(param);
                Info info = paramCheck(param);
                final Info getInfo = getInfo(info.method, info.thisObject);
                /*日志过滤*/
                /*logFilter(hookLog(), new String[]{"AthenaApp", "OplusBattery"},
                    () -> logI(hookLog(), getInfo.thisObject, getInfo.methodProcessed),
                    () -> logSI(hookLog(), getInfo.thisObject, getInfo.methodProcessed + " " + paramLog(param)));*/
                logSI(hookLog(), getInfo.thisObject, getInfo.methodProcessed + " " + paramLog(param));
            } catch (Exception e) {
                logE("before", e.toString());
            }
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) {
            try {
                after(param);
            } catch (Exception e) {
                logE("after", e.toString());
            }
        }
    }

    public abstract static class ReplaceHookedMethod extends HookAction {

        public ReplaceHookedMethod() {
            super();
        }

        public ReplaceHookedMethod(int priority) {
            super(priority);
        }

        protected abstract Object replace(MethodHookParam param) throws Throwable;

        @Override
        public void beforeHookedMethod(MethodHookParam param) {
            try {
                Object result = replace(param);
                param.setResult(result);
            } catch (Throwable t) {
                logE("replace", t.toString());
            }
        }
    }

    public void hookMethod(Method method, HookAction callback) {
        try {
            if (method == null) {
                logW(tag, "method is null!!");
                return;
            }
            XposedBridge.hookMethod(method, callback);
            logI(tag, "Hook: " + method);
        } catch (Throwable e) {
            logE(tag, "Hook failed: " + method + " E: " + e);
        }
    }

    public Method getMethodInstance(MethodData methodData) {
        try {
            if (methodData == null) {
                logE(tag, "methodData is null");
                return null;
            }
            return methodData.getMethodInstance(loadPackageParam.classLoader);
        } catch (Exception throwable) {
            logE(tag, "getMethodInstance: " + throwable);
        }
        return null;
    }

    public void findAndHookMethod(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
        try {
            /*获取class*/
            if (parameterTypesAndCallback.length != 1) {
                Object[] newArray = new Object[parameterTypesAndCallback.length - 1];
                System.arraycopy(parameterTypesAndCallback, 0, newArray, 0, newArray.length);
                getDeclaredMethod(clazz, methodName, newArray);
                /*旧实现*/
                /*Class<?>[] classes = new Class<?>[newArray.length];
                Class<?> newclass = null;
                for (int i = 0; i < newArray.length; i++) {
                    Object type = newArray[i];
                    if (type instanceof Class) {
                        newclass = (Class<?>) newArray[i];
                    } else if (type instanceof String) {
                        newclass = findClassIfExists((String) type);
                        if (newclass == null) {
                            logE(tag, "class can't is null class:" + clazz + " method: " + methodName);
                            return;
                        }
                    }
                    classes[i] = newclass;
                }
                checkDeclaredMethod(clazz, methodName, classes);*/
            }
            XposedHelpers.findAndHookMethod(clazz, methodName, parameterTypesAndCallback);
            logI(tag, "Hook: " + clazz + " method: " + methodName);
        } catch (Throwable e) {
            logE(tag, "Not find method: " + methodName + " in: " + clazz + " E: " + e);
        }
    }

    public void findAndHookMethod(String className, String methodName, Object... parameterTypesAndCallback) {
        findAndHookMethod(findClassIfExists(className), methodName, parameterTypesAndCallback);
    }

    public void findAndHookMethod(String className, ClassLoader classLoader, String methodName, Object... parameterTypesAndCallback) {
        findAndHookMethod(findClassIfExists(className, classLoader), methodName, parameterTypesAndCallback);
    }

    public void findAndHookConstructor(Class<?> clazz, Object... parameterTypesAndCallback) {
        try {
            XposedHelpers.findAndHookConstructor(clazz, parameterTypesAndCallback);
            logI(tag, "HookConstructor: " + clazz);
        } catch (Throwable f) {
            logE(tag, "FindAndHookConstructor E: " + f + " class: " + clazz);
        }
    }

    public void findAndHookConstructor(String className, Object... parameterTypesAndCallback) {
        findAndHookConstructor(findClassIfExists(className), parameterTypesAndCallback);
    }

    public void findAndHookConstructor(String className, ClassLoader classLoader, Object... parameterTypesAndCallback) {
        findAndHookConstructor(findClassIfExists(className, classLoader), parameterTypesAndCallback);
    }

    public void hookAllMethods(String className, String methodName, HookAction callback) {
        try {
            Class<?> hookClass = findClassIfExists(className);
            hookAllMethods(hookClass, methodName, callback);
        } catch (Throwable e) {
            logE(tag, "Hook The: " + e);
        }
    }

    public void hookAllMethods(Class<?> hookClass, String methodName, HookAction callback) {
        try {
            int num = XposedBridge.hookAllMethods(hookClass, methodName, callback).size();
            logI(tag, "Hook: " + hookClass + " methodName: " + methodName + " Num is: " + num);
        } catch (Throwable e) {
            logE(tag, "HookAllMethod E: " + e);
        }
    }

    public void hookAllConstructors(String className, HookAction callback) {
        Class<?> hookClass = findClassIfExists(className);
        if (hookClass != null) {
            hookAllConstructors(hookClass, callback);
        }
    }

    public void hookAllConstructors(Class<?> hookClass, HookAction callback) {
        try {
            XposedBridge.hookAllConstructors(hookClass, callback);
        } catch (Throwable f) {
            logE(tag, "HookAllConstructors E: " + f + " class: " + hookClass);
        }
    }

    public void hookAllConstructors(String className, ClassLoader classLoader, HookAction callback) {
        Class<?> hookClass = XposedHelpers.findClassIfExists(className, classLoader);
        if (hookClass != null) {
            hookAllConstructors(hookClass, callback);
        }
    }

    public Object callMethod(Object obj, String methodName, Object... args) {
        try {
            return XposedHelpers.callMethod(obj, methodName, args);
        } catch (Throwable e) {
            logE(tag, "callMethod: " + obj.toString() + " method: "
                + methodName + " args: " + Arrays.toString(args) + " e: " + e);
            return null;
        }
    }

    public Object callStaticMethod(Class<?> clazz, String methodName, Object... args) {
        try {
            return XposedHelpers.callStaticMethod(clazz, methodName, args);
        } catch (Throwable e) {
            logE(tag, "callStaticMethod: " + clazz.getSimpleName() + " method: "
                + methodName + " args: " + Arrays.toString(args) + " e: " + e);
            return null;
        }
    }

    public Method getDeclaredMethod(String className, String method, Object... type) throws NoSuchMethodException {
        return getDeclaredMethod(findClassIfExists(className), method, type);
    }

    public Method getDeclaredMethod(Class<?> clazz, String method, Object... type) throws NoSuchMethodException {
//        String tag = "getDeclaredMethod";
        ArrayList<Method> haveMethod = new ArrayList<>();
        Method hqMethod = null;
        int methodNum;
        if (clazz == null) {
            throw new NoSuchMethodException("class must not is null");
        }
        for (Method getMethod : clazz.getDeclaredMethods()) {
            if (getMethod.getName().equals(method)) {
                haveMethod.add(getMethod);
            }
        }
        if (haveMethod.isEmpty()) {
            throw new NoSuchMethodException("this class: " + clazz + " declared method is empty");
        }
        methodNum = haveMethod.size();
        if (type != null) {
            Class<?>[] classes = new Class<?>[type.length];
            Class<?> newclass = null;
            Object getType;
            for (int i = 0; i < type.length; i++) {
                getType = type[i];
                if (getType instanceof Class<?>) {
                    newclass = (Class<?>) getType;
                }
                if (getType instanceof String) {
                    newclass = findClassIfExists((String) getType);
                    if (newclass == null) {
                        throw new NoSuchMethodException("object to class failed: " + getType);
                    }
                }
                classes[i] = newclass;
            }
            boolean noError = true;
            for (int i = 0; i < methodNum; i++) {
                hqMethod = haveMethod.get(i);
                boolean have = true;
                // 判断类中声明的方法所包含的参数数量是否与传入的相同
                if (hqMethod.getParameterTypes().length != classes.length) {
                    if (methodNum - 1 == i) {
                        throw new NoSuchMethodException("last method param is: "
                            + Arrays.toString(hqMethod.getParameterTypes()) +
                            " but afferent param is: " + Arrays.toString(classes));
                    } else {
                        noError = false;
                        continue;
                    }
                }
                // 判断类中声明方法的参数名是否与传入的相同
                for (int t = 0; t < hqMethod.getParameterTypes().length; t++) {
                    Class<?> getClass = hqMethod.getParameterTypes()[t];
                    if (!getClass.getName().equals(classes[t].getName())) {
                        have = false;
                        break;
                    }
                }
                if (!have) {
                    if (methodNum - 1 == i) {
                        throw new NoSuchMethodException("check all declared classes, " +
                            "but none meet the requirements, declared: "
                            + Arrays.toString(haveMethod.toArray()));
                    } else {
                        noError = false;
                        continue;
                    }
                }
                if (noError) {
                    break;
                }
            }
            return hqMethod;
        } else {
            if (methodNum > 1) {
                throw new NoSuchMethodException("There are multiple declaration methods obtained," +
                    " but they cannot be distinguished: "
                    + Arrays.toString(haveMethod.toArray()));
            }
        }
        return haveMethod.get(0);
    }

    public void setDeclaredField(XC_MethodHook.MethodHookParam param, String iNeedString, Object iNeedTo) {
        if (param != null) {
            try {
                Field setString = param.thisObject.getClass().getDeclaredField(iNeedString);
                setString.setAccessible(true);
                try {
                    setString.set(param.thisObject, iNeedTo);
                    Object result = setString.get(param.thisObject);
                    checkLast("setDeclaredField", iNeedString, iNeedTo, result);
                } catch (IllegalAccessException e) {
                    logE(tag, "IllegalAccessException: " + iNeedString + " need: " + iNeedTo + " E:" + e);
                }
            } catch (NoSuchFieldException e) {
                logE(tag, "No such field: " + iNeedString + " E: " + e);
            }
        } else {
            logW(tag, "Param is null: " + iNeedString + " & " + iNeedTo);
        }
    }

    public void checkLast(String setObject, Object fieldName, Object value, Object last) {
        if (value != null && last != null) {
            if (value == last || value.equals(last)) {
                logI(tag, setObject + " Success! set " + fieldName + " to " + value);
            } else {
                logE(tag, setObject + " Failed! set " + fieldName + " to " + value + " hope: " + value + " but: " + last);
            }
        } else {
            logE(tag, setObject + " Error value: " + value + " or last: " + last + " is null");
        }
    }

    public Object getObjectField(Object obj, String fieldName) {
        try {
            return XposedHelpers.getObjectField(obj, fieldName);
        } catch (Throwable e) {
            logE(tag, "getObject field E:" + fieldName);
            return null;
        }
    }

    public void setInt(Object obj, String fieldName, int value) {
        checkAndSetField(obj, fieldName,
            () -> XposedHelpers.setIntField(obj, fieldName, value),
            () -> checkLast("setInt", fieldName, value,
                XposedHelpers.getIntField(obj, fieldName)));
    }

    public void setBoolean(Object obj, String fieldName, boolean value) {
        checkAndSetField(obj, fieldName,
            () -> XposedHelpers.setBooleanField(obj, fieldName, value),
            () -> checkLast("setBoolean", fieldName, value,
                XposedHelpers.getBooleanField(obj, fieldName)));
    }

    public void setObject(Object obj, String fieldName, Object value) {
        checkAndSetField(obj, fieldName,
            () -> XposedHelpers.setObjectField(obj, fieldName, value),
            () -> checkLast("setObject", fieldName, value,
                XposedHelpers.getObjectField(obj, fieldName)));
    }

    public void checkDeclaredMethod(String className, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        Class<?> hookClass = findClassIfExists(className);
        if (hookClass != null) {
            hookClass.getDeclaredMethod(name, parameterTypes);
            return;
        }
        throw new NoSuchMethodException();
    }

    public void checkDeclaredMethod(Class<?> clazz, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        if (clazz != null) {
            clazz.getDeclaredMethod(name, parameterTypes);
            return;
        }
        throw new NoSuchMethodException();
    }

    public void checkAndSetField(Object obj, String fieldName, Runnable setField, Runnable checkLast) {
        try {
            obj.getClass().getDeclaredField(fieldName);
        } catch (Exception e) {
            logE(tag, "No such field: " + fieldName + " E: " + e);
        }
        try {
            setField.run();
            checkLast.run();
        } catch (Exception f) {
            logE(tag, "Set field: " + fieldName + " E: " + f);
        }
    }

    public static Context findContext(@Duration int flag) {
        Context context = null;
        try {
            switch (flag) {
                case 0 -> {
                    if ((context = currentApplication()) == null)
                        context = getSystemContext();
                }
                case 1 -> {
                    context = currentApplication();
                }
                case 2 -> {
                    context = getSystemContext();
                }
                default -> {
                }
            }
            return context;
        } catch (Throwable ignore) {
        }
        return null;
    }

    private static Context currentApplication() {
        return (Application) XposedHelpers.callStaticMethod(XposedHelpers.findClass(
                "android.app.ActivityThread", null),
            "currentApplication");
    }

    private static Context getSystemContext() {
        Context context = null;
        Object currentActivityThread = XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread",
                null),
            "currentActivityThread");
        if (currentActivityThread != null)
            context = (Context) XposedHelpers.callMethod(currentActivityThread,
                "getSystemContext");
        if (context == null)
            context = (Context) XposedHelpers.callMethod(currentActivityThread,
                "getSystemUiContext");
        return context;
    }

    public PathClassLoader pathClassLoader(String path, ClassLoader classLoader) {
        try {
            return new PathClassLoader(path, classLoader);
        } catch (Throwable e) {
            logE(tag, "pathClassLoader E: " + e);
            return null;
        }
    }
}
