package Com.HChen.Hook.Mode;

import java.lang.reflect.Field;

import Com.HChen.Hook.Base.BasePutKey;
import Com.HChen.Hook.Utils.GetKey;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public abstract class HookMode extends HookLog {
    GetKey<String, Object> mPrefsMap = BasePutKey.mPrefsMap;
    LoadPackageParam loadPackageParam;

    public abstract void init();

    public void Run(LoadPackageParam loadPackageParam) {
        try {
            SetLoadPackageParam(loadPackageParam);
            init();
            logI("Hook Success!");
        } catch (Throwable e) {
            logE("Hook Failed! Because: " + e);
        }
    }

    public void SetLoadPackageParam(LoadPackageParam loadPackageParam) {
        this.loadPackageParam = loadPackageParam;
    }

    public Class<?> findClass(String className) {
        return findClass(className, loadPackageParam.classLoader);
    }

    public Class<?> findClass(String className, ClassLoader classLoader) {
        return XposedHelpers.findClass(className, classLoader);
    }

    public Class<?> findClassIfExists(String className) {
        try {
            return findClass(className);
        } catch (XposedHelpers.ClassNotFoundError e) {
            logE("Find " + className + " is Null, Code is: " + e);
            return null;
        }
    }

    public Class<?> findClassIfExists(String newClassName, String oldClassName) {
        try {
            return findClass(findClassIfExists(newClassName) != null ? newClassName : oldClassName);
        } catch (XposedHelpers.ClassNotFoundError e) {
            logE("Find " + newClassName + " and " + oldClassName + " is Null, Code is: " + e);
            return null;
        }
    }

    public static class HookAction extends HookLog {

        protected void before(MethodHookParam param) throws Throwable {
        }

        protected void after(MethodHookParam param) throws Throwable {
        }

        public HookAction() {
            super();
        }

        public HookAction(int priority) {
            super(priority);
        }

        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            try {
                before(param);
            } catch (Throwable e) {
                logE("" + e);
            }
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            try {
                after(param);
            } catch (Throwable e) {
                logE("" + e);
            }
        }

    }

    public void findAndHookMethod(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
        try {
            if (parameterTypesAndCallback.length != 1) {
                Object[] newArray = new Object[parameterTypesAndCallback.length - 1];
                System.arraycopy(parameterTypesAndCallback, 0, newArray, 0, newArray.length);
                Class<?>[] classes = new Class<?>[newArray.length];
                for (int i = 0; i < newArray.length; i++) {
                    Class<?> newclass = (Class<?>) newArray[i];
                    classes[i] = newclass;
                }
                clazz.getDeclaredMethod(methodName, classes);
            }
            XposedHelpers.findAndHookMethod(clazz, methodName, parameterTypesAndCallback);
            logI("Hook: " + clazz + " method is: " + methodName);
        } catch (NoSuchMethodException e) {
            logE("Not find method: " + methodName + " in: " + clazz);
        }
    }

    public void findAndHookMethod(String className, String methodName, Object... parameterTypesAndCallback) {
        int lastIndex = className.lastIndexOf(".");
        if (lastIndex != -1) {
            String result = className.substring(lastIndex + 1);
            int lastIndexA = result.lastIndexOf("$");
            if (lastIndexA == -1) {
                if (mPrefsMap.getBoolean(methodName)) {
                    findAndHookMethod(findClassIfExists(className), methodName, parameterTypesAndCallback);
                } else {
                    if (mPrefsMap.getBoolean(result + "_" + methodName)) {
                        findAndHookMethod(findClassIfExists(className), methodName, parameterTypesAndCallback);
                    } else {
                        if (mPrefsMap.getBoolean(result)) {
                            findAndHookMethod(findClassIfExists(className), methodName, parameterTypesAndCallback);
                        } else {
                            logW("findAndHookMethod the: " + lastIndex + " ; " + methodName + " is false");
                        }
                    }
                }
            } else {
                String resultA = result.substring(0, lastIndexA);
                if (mPrefsMap.getBoolean(resultA)) {
                    findAndHookMethod(findClassIfExists(className), methodName, parameterTypesAndCallback);
                } else {
                    logW("findAndHookMethod the: " + lastIndex + " ; " + methodName + " is false");
                }
            }
        }
    }

    public void findAndHookConstructor(Class<?> clazz, Object... parameterTypesAndCallback) {
        XposedHelpers.findAndHookConstructor(clazz, parameterTypesAndCallback);
    }

    public void findAndHookConstructor(String className, Object... parameterTypesAndCallback) {
        int lastIndex = className.lastIndexOf(".");
        if (lastIndex != -1) {
            String result = className.substring(lastIndex + 1);
            if (mPrefsMap.getBoolean(result)) {
                findAndHookConstructor(findClassIfExists(className), parameterTypesAndCallback);
            } else {
                logW("findAndHookConstructor the: " + lastIndex + " is false");
            }
        } else {
            logE(className);
        }
    }

    public void hookAllMethods(String className, String methodName, XC_MethodHook callback) {
        try {
            Class<?> hookClass = findClassIfExists(className);
            int lastIndex = className.lastIndexOf(".");
            if (mPrefsMap.getBoolean(methodName)) {
                if (hookClass != null) {
                    hookAllMethods(hookClass, methodName, callback);
                } else {
                    logE("Hook class: " + className + " method: " + methodName + " is Null");
                }
            } else {
                if (lastIndex != -1) {
                    String result = className.substring(lastIndex + 1);
                    if (mPrefsMap.getBoolean(result + "_" + methodName)) {
                        if (hookClass != null) {
                            hookAllMethods(hookClass, methodName, callback);
                        } else {
                            logE("Hook class: " + className + " method: " + methodName + " is Null");
                        }
                    } else {
                        if (mPrefsMap.getBoolean(result)) {
                            if (hookClass != null) {
                                hookAllMethods(hookClass, methodName, callback);
                            } else {
                                logE("Hook class: " + className + " method: " + methodName + " is Null");
                            }
                        } else {
                            logW("hookAllMethods the: " + lastIndex + " ; " + methodName + " is false");
                        }
                    }
                }
            }
        } catch (Throwable e) {
            logE("Hook The: " + e + " Error");
        }
    }

    public void hookAllMethods(Class<?> hookClass, String methodName, XC_MethodHook callback) {
        try {
            int Num = XposedBridge.hookAllMethods(hookClass, methodName, callback).size();
            logI("Hook: " + hookClass + " methodName: " + methodName + " Num is: " + Num);
        } catch (Throwable e) {
            logE("Hook The: " + e + " Error");
        }
    }

    public void callMethod(Object obj, String methodName, Object... args) {
        XposedHelpers.callMethod(obj, methodName, args);
    }

    public void callStaticMethod(Class<?> clazz, String methodName, Object... args) {
        XposedHelpers.callStaticMethod(clazz, methodName, args);
    }

    public void getDeclaredField(XC_MethodHook.MethodHookParam param, String iNeedString, Object iNeedTo) {
        if (param != null) {
            try {
                Field setString = param.thisObject.getClass().getDeclaredField(iNeedString);
                setString.setAccessible(true);
                try {
                    setString.set(param.thisObject, iNeedTo);
                    Object result = setString.get(param.thisObject);
                    checkLast("getDeclaredField", iNeedString, iNeedTo, result);
                } catch (IllegalAccessException e) {
                    logE("IllegalAccessException to: " + iNeedString + " Need to: " + iNeedTo + " Code:" + e);
                }
            } catch (NoSuchFieldException e) {
                logE("No such the: " + iNeedString + " Code: " + e);
            }
        } else {
            logE("Param is null Code: " + iNeedString + " And: " + iNeedTo);
        }
    }

    public void checkLast(String setObject, Object fieldName, Object value, Object last) {
        if (value.equals(last)) {
            logI(setObject + " Success! set " + fieldName + " to " + value);
        } else {
            logE(setObject + " Failed! set " + fieldName + " to " + value + " i hope is: " + value + " but is: " + last);
        }
    }

    public void setInt(Object obj, String fieldName, int value) {
        XposedHelpers.setIntField(obj, fieldName, value);
        checkLast("setInt", fieldName, value, XposedHelpers.getIntField(obj, fieldName));
    }

    public void setBoolean(Object obj, String fieldName, boolean value) {
        XposedHelpers.setBooleanField(obj, fieldName, value);
        checkLast("setBoolean", fieldName, value, XposedHelpers.getBooleanField(obj, fieldName));
    }

    public void setObject(Object obj, String fieldName, Object value) {
        XposedHelpers.setObjectField(obj, fieldName, value);
        checkLast("setObject", fieldName, value, XposedHelpers.getObjectField(obj, fieldName));
    }
}
