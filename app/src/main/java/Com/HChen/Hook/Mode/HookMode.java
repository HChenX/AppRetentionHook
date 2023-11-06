package Com.HChen.Hook.Mode;

import java.lang.reflect.Field;
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
    LoadPackageParam loadPackageParam;

    public abstract void init();

    public void Run(LoadPackageParam loadPackageParam) {
        try {
            SetLoadPackageParam(loadPackageParam);
            init();
            logI(tag, "Hook Success!");
        } catch (Throwable e) {
            logE(tag, "Hook Failed! code: " + e);
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
            logE(tag, "Find " + className + " is null, code: " + e);
            return null;
        }
    }

    public Class<?> findClassIfExists(String newClassName, String oldClassName) {
        try {
            return findClass(findClassIfExists(newClassName) != null ? newClassName : oldClassName);
        } catch (XposedHelpers.ClassNotFoundError e) {
            logE(tag, "Find " + newClassName + " and " + oldClassName + " is null, code: " + e);
            return null;
        }
    }

    public static class HookAction extends XC_MethodHook {

        private String method = null;

        protected void before(MethodHookParam param) {
//            String all = param.method.toString();
//            String className = param.thisObject.toString();
            Info info = paramCheck(param);
            info = getInfo(info.all, info.className);
            logSI(info.className, info.method + " before");
        }

        protected void after(MethodHookParam param) {
//            String all = param.method.toString();
//            String className = param.thisObject.toString();
            Info info = paramCheck(param);
            info = getInfo(info.all, info.className);
            logSI(info.className, info.method + " after");
        }

        private Info paramCheck(MethodHookParam param) {
            String all = null;
            String className = null;
            if (param.method != null) {
                all = param.method.toString();
            }
            if (param.thisObject != null) {
                className = param.thisObject.toString();
            }
            if (param.method == null || param.thisObject == null)
                logE("paramCheck", "param.method is: " + param.method
                    + " param.thisObject is: " + param.thisObject);
            return new Info(all, className, null);
        }

        private Info getInfo(String all, String className) {
//            int lastIndex = all.lastIndexOf(".");
            if (all == null || className == null) return new Info(null, null, null);
            Pattern pattern = Pattern.compile(".*\\.(.*\\(.*\\))");
            Matcher matcher = pattern.matcher(all);
            if (className.contains("@")) {
                if (matcher.find()) {
                    method = matcher.group(1);
                } else method = null;
                pattern = Pattern.compile(".*\\.(\\w+)\\..*\\(.*\\)");
                matcher = pattern.matcher(all);
                if (matcher.find()) {
                    className = matcher.group(1);
                } else className = null;
            } else {
                if (matcher.find()) {
                    className = matcher.group(1);
                } else method = "constructor";
            }
            return new Info(null, className, method);
        }

        private static class Info {
            public String all;
            public String method;
            public String className;

            public Info(String all, String className, String method) {
                this.all = all;
                this.className = className;
                this.method = method;
            }
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
                protected void before(MethodHookParam param) {
                    super.before(param);
                    param.setResult(result);
                }
            };
        }

        public static final HookAction DO_NOTHING = new HookAction(PRIORITY_HIGHEST * 2) {
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

    public void findAndHookMethod(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
        try {
            /*获取class*/
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
            logI(tag, "Hook: " + clazz + " method is: " + methodName);
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
        XposedHelpers.findAndHookConstructor(clazz, parameterTypesAndCallback);
        logI(tag, "Hook: " + clazz + " is success");
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
            logE(tag, "Hook The: " + e + " Error");
        }
    }

    public void hookAllMethods(Class<?> hookClass, String methodName, HookAction callback) {
        try {
            int Num = XposedBridge.hookAllMethods(hookClass, methodName, callback).size();
            logI(tag, "Hook: " + hookClass + " methodName: " + methodName + " Num is: " + Num);
        } catch (Throwable e) {
            logE(tag, "Hook The: " + e + " Error");
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
                    logE(tag, "IllegalAccessException to: " + iNeedString + " Need to: " + iNeedTo + " Code:" + e);
                }
            } catch (NoSuchFieldException e) {
                logE(tag, "No such the: " + iNeedString + " Code: " + e);
            }
        } else {
            logE(tag, "Param is null Code: " + iNeedString + " And: " + iNeedTo);
        }
    }

    public void checkLast(String setObject, Object fieldName, Object value, Object last) {
        if (value.equals(last)) {
            logI(tag, setObject + " Success! set " + fieldName + " to " + value);
        } else {
            logE(tag, setObject + " Failed! set " + fieldName + " to " + value + " i hope is: " + value + " but is: " + last);
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
                        logW(tag, "Such key error: " + methodName);
                    }
                }
                if (mPrefsMap.getBoolean(find) && check) {
                    if (mCode != null) mCode.run();
                } else
                    logW(tag, log + combinedKeywords + " is false");
            }
        } else {
            logW(tag, log + methodName + " is false");
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
            logW(tag, "Such key error: " + keyWord + " " + needString);
            return false;
        }
    }
}
