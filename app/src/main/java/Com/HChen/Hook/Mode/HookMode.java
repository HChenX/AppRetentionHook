package Com.HChen.Hook.Mode;

import android.app.Application;
import android.content.Context;

import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Com.HChen.Hook.Base.BaseGetKey;
import Com.HChen.Hook.Utils.AllValue;
import Com.HChen.Hook.Utils.GetKey;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public abstract class HookMode extends HookLog {
    public String tag = getClass().getSimpleName();
    public final GetKey<String, Object> mPrefsMap = BaseGetKey.mPrefsMap;
    public final String[] allValue = AllValue.AllValue;
    List<String> tempString, tempStringT, tempStringS = new ArrayList<>();
    public LoadPackageParam loadPackageParam;

    public abstract void init();

    public void Run(LoadPackageParam loadPackageParam) {
        try {
            SetLoadPackageParam(loadPackageParam);
//            EzXHelper.initHandleLoadPackage(loadPackageParam);
            init();
            logI(tag, "Hook Success!");
        } catch (Throwable e) {
            logE(tag, "Hook Failed: " + e);
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
            logE(tag, "Class no found: " + e);
            return null;
        }
    }

    public Class<?> findClassIfExists(String newClassName, String oldClassName) {
        try {
            return findClass(findClassIfExists(newClassName) != null ? newClassName : oldClassName);
        } catch (XposedHelpers.ClassNotFoundError e) {
            logE(tag, "Find " + newClassName + " & " + oldClassName + " is null: " + e);
            return null;
        }
    }

    public abstract static class HookAction extends XC_MethodHook implements IHookLog {

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
                    super.before(param);
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
                super.before(param);
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
            } catch (Throwable e) {
                logE("beforeHookedMethod", "" + e);
            }
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) {
            try {
                after(param);
            } catch (Throwable e) {
                logE("afterHookedMethod", "" + e);
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

        protected abstract Object replace(XC_MethodHook.MethodHookParam param);

        @Override
        public void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
            try {
                Object result = replace(param);
                param.setResult(result);
            } catch (Throwable t) {
                logE("replaceHookedMethod", "" + t);
            }
        }
    }

    /*public static class HookDexKit {

        public static void beforeDexKit(MethodData method, LoadPackageParam param, ActionTiming actionTiming) {
            try {
                if (method == null) {
                    logE("beforeDexKit", "method is null: " + param.packageName);
                    return;
                }
                logI("AthenaApp", "class: " + method.getClassName() + " method: " + method.getMethodName());
                HookFactory.createMethodHook(method.getMethodInstance(param.classLoader),
                    new Consumer<HookFactory>() {
                        @Override
                        public void accept(HookFactory hookFactory) {
                            hookFactory.before(
                                new callBack() {
                                    @Override
                                    public void hookMethod(@NonNull XC_MethodHook.MethodHookParam methodHookParam) {
                                        actionTiming.before(methodHookParam);
                                    }
                                }
                            );
                        }
                    }
                );
            } catch (Throwable throwable) {
                logE("beforeDexKit", "" + throwable);
            }
        }

        public static void afterDexKit(MethodData method, LoadPackageParam param, ActionTiming actionTiming) {
            try {
                if (method == null) {
                    logE("afterDexKit", "method is null: " + param.packageName);
                    return;
                }
                logI("AthenaApp", "class: " + method.getClassName() + " method: " + method.getMethodName());
                HookFactory.createMethodHook(method.getMethodInstance(param.classLoader),
                    new Consumer<HookFactory>() {
                        @Override
                        public void accept(HookFactory hookFactory) {
                            hookFactory.after(
                                new callBack() {
                                    @Override
                                    public void hookMethod(@NonNull XC_MethodHook.MethodHookParam methodHookParam) {
                                        actionTiming.after(methodHookParam);
                                    }
                                }
                            );
                        }
                    }
                );
            } catch (Throwable e) {
                logE("afterDexKit", "" + e);
            }
        }
    }

    public static class ActionTiming {
        public void after(@NonNull XC_MethodHook.MethodHookParam param) {
        }

        public void before(@NonNull XC_MethodHook.MethodHookParam param) {
        }
    }

    public abstract static class callBack implements IMethodHookCallback {
        public abstract void hookMethod(@NonNull XC_MethodHook.MethodHookParam methodHookParam);

        @Override
        public void onMethodHooked(@NonNull XC_MethodHook.MethodHookParam methodHookParam) {
            try {
                hookMethod(methodHookParam);
                Info info = paramCheck(methodHookParam);
                info = getInfo(info.method, info.thisObject);
                logI(info.thisObject, info.methodProcessed + " " + paramLog(methodHookParam));
            } catch (Throwable throwable) {
                logE("onMethodHooked", "" + throwable);
            }
        }
    }*/

    public void hookMethod(Method method, HookAction callback) {
        try {
            if (method == null) {
                logE(tag, "method is null");
                return;
            }
            XposedBridge.hookMethod(method, callback);
            logI(tag, "Hook: " + method);
        } catch (Throwable e) {
            logE(tag, "Hook: " + method);
        }
    }

    public Method getMethodInstance(MethodData methodData) {
        try {
            if (methodData == null) {
                logE(tag, "methodData is null");
                return null;
            }
            return methodData.getMethodInstance(loadPackageParam.classLoader);
        } catch (Throwable throwable) {
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
                Class<?>[] classes = new Class<?>[newArray.length];
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
                /*for (int i = 0; i < newArray.length; i++) {
                    Class<?> newclass = (Class<?>) newArray[i];
                    classes[i] = newclass;
                }*/
                /*clazz.getDeclaredMethod(methodName, classes);*/
                checkDeclaredMethod(clazz, methodName, classes);
            }
            XposedHelpers.findAndHookMethod(clazz, methodName, parameterTypesAndCallback);
            logI(tag, "Hook: " + clazz + " method: " + methodName);
        } catch (NoSuchMethodException e) {
            logE(tag, "Not find method: " + methodName + " in: " + clazz);
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
                            checkAndRun(tempStringT, methodName,
                                () -> findAndHookMethod(
                                    findClassIfExists(className),
                                    methodName,
                                    parameterTypesAndCallback),
                                "findAndHookMethod the class: " + className + " ; ");
                        }
                    }
                }
            } else {
                String resultA = result.substring(0, lastIndexA);
                if (mPrefsMap.getBoolean(resultA)) {
                    findAndHookMethod(findClassIfExists(className), methodName, parameterTypesAndCallback);
                } else {
                    checkAndRun(tempStringT, methodName,
                        () -> findAndHookMethod(
                            findClassIfExists(className),
                            methodName,
                            parameterTypesAndCallback),
                        "findAndHookMethod the class: " + className + " ; ");
                }
            }
        } else {
            logE(tag, "Cant get key, class: " + className);
        }
    }

    public void findAndHookConstructor(Class<?> clazz, Object... parameterTypesAndCallback) {
        try {
            XposedHelpers.findAndHookConstructor(clazz, parameterTypesAndCallback);
            logI(tag, "Hook: " + clazz);
        } catch (Throwable f) {
            logE(tag, "findAndHookConstructor: " + f + " class: " + clazz);
        }
    }

    public void findAndHookConstructor(String className, Object... parameterTypesAndCallback) {
        int lastIndex = className.lastIndexOf(".");
        if (lastIndex != -1) {
            String result = className.substring(lastIndex + 1);
            if (mPrefsMap.getBoolean(result)) {
                findAndHookConstructor(findClassIfExists(className), parameterTypesAndCallback);
            } else {
                checkAndRun(tempStringS, result,
                    () -> findAndHookConstructor(
                        findClassIfExists(className),
                        parameterTypesAndCallback),
                    "findAndHookConstructor the class: " + className + " ; ");
            }
        } else {
            logE(tag, "Cant get key, class: " + className);
        }
    }

    public void hookAllMethods(String className, String methodName, HookAction callback) {
        try {
            Class<?> hookClass = findClassIfExists(className);
            if (hookClass == null) {
                logE(tag, "Hook class: " + className + " method: " + methodName + " is Null");
                return;
            }
            int lastIndex = className.lastIndexOf(".");
            if (mPrefsMap.getBoolean(methodName)) {
                hookAllMethods(hookClass, methodName, callback);
            } else {
                if (lastIndex != -1) {
                    String result = className.substring(lastIndex + 1);
                    if (mPrefsMap.getBoolean(result + "_" + methodName)) {
                        hookAllMethods(hookClass, methodName, callback);
                    } else {
                        if (mPrefsMap.getBoolean(result)) {
                            hookAllMethods(hookClass, methodName, callback);
                        } else {
                            checkAndRun(tempString, methodName,
                                () -> hookAllMethods(
                                    hookClass,
                                    methodName,
                                    callback),
                                "hookAllMethods the class: " + className + " ; ");
                        }
                    }
                }
            }
        } catch (Throwable e) {
            logE(tag, "Hook The: " + e);
        }
    }

    public void hookAllMethods(Class<?> hookClass, String methodName, HookAction callback) {
        try {
            int Num = XposedBridge.hookAllMethods(hookClass, methodName, callback).size();
            logI(tag, "Hook: " + hookClass + " methodName: " + methodName + " Num is: " + Num);
        } catch (Throwable e) {
            logE(tag, "Hook The: " + e);
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
            logE(tag, "hookAllConstructors: " + f + " class: " + hookClass);
        }
    }

    public void hookAllConstructors(String className, ClassLoader classLoader, HookAction callback) {
        Class<?> hookClass = XposedHelpers.findClassIfExists(className, classLoader);
        if (hookClass != null) {
            hookAllConstructors(hookClass, callback);
        }
    }

    public Object callMethod(Object obj, String methodName, Object... args) {
        return XposedHelpers.callMethod(obj, methodName, args);
    }

    public Object callStaticMethod(Class<?> clazz, String methodName, Object... args) {
        return XposedHelpers.callStaticMethod(clazz, methodName, args);
    }

    public static Context findContext() {
        Context context;
        try {
            context = (Application) XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", null), "currentApplication");
            if (context == null) {
                Object currentActivityThread = XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread");
                if (currentActivityThread != null)
                    context = (Context) XposedHelpers.callMethod(currentActivityThread, "getSystemContext");
            }
            return context;
        } catch (Throwable ignore) {
        }
        return null;
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
                    logE(tag, "IllegalAccessException to: " + iNeedString + " Need to: " + iNeedTo + " :" + e);
                }
            } catch (NoSuchFieldException e) {
                logE(tag, "No such the: " + iNeedString + " : " + e);
            }
        } else {
            logE(tag, "Param is null Code: " + iNeedString + " & " + iNeedTo);
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

    public void setInt(Object obj, String fieldName, int value) {
        checkAndHookField(obj, fieldName,
            () -> XposedHelpers.setIntField(obj, fieldName, value),
            () -> checkLast("setInt", fieldName, value,
                XposedHelpers.getIntField(obj, fieldName)));
    }

    public void setBoolean(Object obj, String fieldName, boolean value) {
        checkAndHookField(obj, fieldName,
            () -> XposedHelpers.setBooleanField(obj, fieldName, value),
            () -> checkLast("setBoolean", fieldName, value,
                XposedHelpers.getBooleanField(obj, fieldName)));
    }

    public void setObject(Object obj, String fieldName, Object value) {
        checkAndHookField(obj, fieldName,
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

    public void checkAndHookField(Object obj, String fieldName, Runnable setField, Runnable checkLast) {
        try {
            obj.getClass().getDeclaredField(fieldName);
            setField.run();
            checkLast.run();
        } catch (NoSuchFieldException e) {
            logE(tag, "No such field: " + fieldName + " in param: " + obj + " : " + e);
        }
    }

    public void checkAndRun(List<String> mList, String methodName, Runnable mCode, String log) {
        List<String> endRetrieval = retrievalValue(methodName, allValue);
        StringBuilder combinedKeywords = new StringBuilder();
        if (!endRetrieval.isEmpty()) {
            for (int i = 0; i < endRetrieval.size(); i++) {
                combinedKeywords.append(endRetrieval.get(i));
                if (i < endRetrieval.size() - 1) {
                    combinedKeywords.append("_");
                }
            }
            mList.add(combinedKeywords.toString());
            boolean check = false;
            if (mPrefsMap.getBoolean(combinedKeywords.toString())) {
                if (mCode != null) mCode.run();
            } else {
                String find = null;
                for (int i = 0; i < mList.size(); i++) {
                    try {
                        Pattern pattern = Pattern.compile(".*" + methodName + ".*");
                        Matcher matcher = pattern.matcher(mList.get(i));
                        if (matcher.matches()) {
                            find = mList.get(i);
                            mList.clear();
                            check = true;
                            break;
                        }
                    } catch (Exception e) {
                        logW(tag, "Such key: " + methodName);
                    }
                }
                if (mPrefsMap.getBoolean(find) && check) {
                    if (mCode != null) mCode.run();
                } else
                    logW(tag, log + combinedKeywords + " false");
            }
        } else {
            logW(tag, log + methodName + " false");
        }
    }

    public List<String> retrievalValue(String needString, String[] allValue) {
        List<String> matchedKeywords = new ArrayList<>();
        for (String keyWord : allValue) {
            if (isFuzzyMatch(keyWord, needString)) {
                matchedKeywords.add(keyWord);
            }
        }
        return matchedKeywords;
    }

    private boolean isFuzzyMatch(String keyWord, String needString) {
        try {
            String regex = ".*" + needString + ".*";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(keyWord);
            return matcher.matches();
        } catch (Exception e) {
            logW(tag, "Such key: " + keyWord + " " + needString);
            return false;
        }
    }
}
