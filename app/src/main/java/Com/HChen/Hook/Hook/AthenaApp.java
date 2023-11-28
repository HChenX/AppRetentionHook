package Com.HChen.Hook.Hook;

import static Com.HChen.Hook.Param.Name.OplusName.IAthenaService$Stub$Proxy;

import androidx.annotation.NonNull;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import Com.HChen.Hook.Mode.DexKit.DexKit;
import Com.HChen.Hook.Mode.HookMode;
import de.robv.android.xposed.XC_MethodHook;

public class AthenaApp extends HookMode {
    @Override
    public void init() {
        DexKit.INSTANCE.initDexKit(loadPackageParam);
        /*防止意外报错导致的未能关闭DexKit*/
        try {
            /*boolean c*/
            MethodData methodData = DexKit.INSTANCE.getDexKitBridge().findMethod(
                FindMethod.create()
                    .matcher(MethodMatcher.create()
                        .declaredClass(ClassMatcher.create()
                            .usingStrings(" is reused by others, skip kill "))
                        .usingStrings(" is reused by others, skip kill "))
            ).firstOrNull();
            HookDexKit.beforeDexKit(methodData, loadPackageParam,
                new ActionTiming() {
                    @Override
                    public void before(@NonNull XC_MethodHook.MethodHookParam param) {
                        String reason = (String) param.args[7];
                        int code = (int) param.args[4];
                        if (!("oneclick".equals(reason) && code == 2)) {
                            param.setResult(false);
                        }
                    }
                }
            );
        /*try {
            HookFactory.createMethodHook(methodData.getMethodInstance(loadPackageParam.classLoader),
                new Consumer<HookFactory>() {
                    @Override
                    public void accept(HookFactory hookFactory) {
                        hookFactory.before(
                            new callBack() {
                                @Override
                                public void hookMethod(@NonNull XC_MethodHook.MethodHookParam methodHookParam) {
                                    String reason = (String) methodHookParam.args[7];
                                    int code = (int) methodHookParam.args[4];
                                    if (!("oneclick".equals(reason) && code == 2)) {
                                        methodHookParam.setResult(false);
                                    }
                                }
                            }
                        );
                    }
                }
            );
        } catch (Throwable e) {
            logE("AthenaApp", "NoSuchMethodException :" + e);
        }*/
            /*void b*/
            MethodData methodData1 = DexKit.INSTANCE.getDexKitBridge().findMethod(
                FindMethod.create()
                    .matcher(MethodMatcher.create()
                        .declaredClass(ClassMatcher.create()
                            .usingStrings(" is reused by others, skip kill "))
                        .usingStrings("context is null, not to force stop"))
            ).firstOrNull();
            HookDexKit.beforeDexKit(methodData1, loadPackageParam,
                new ActionTiming() {
                    @Override
                    public void before(@NonNull XC_MethodHook.MethodHookParam param) {
                        String reason = (String) param.args[6];
                        switch (reason) {
                            case "swipe up", "removeTask" -> {

                            }
                            default -> param.setResult(null);
                        }
                    }
                }
            );
            /*try {*/
           /* HookFactory.createMethodHook(methodData1.getMethodInstance(loadPackageParam.classLoader), new Consumer<HookFactory>() {
                @Override
                public void accept(HookFactory hookFactory) {
                    hookFactory.before(new callBack() {
                        @Override
                        public void hookMethod(@NonNull XC_MethodHook.MethodHookParam methodHookParam) {

                        }
                    });
                }
            });
        } catch (Throwable e) {
            logE("AthenaApp", "NoSuchMethodException :" + e);
        }*/
            /*与Boolean c重复*/
        /*MethodData methodData2 = DexKit.INSTANCE.getDexKitBridge().findMethod(
            FindMethod.create()
                .matcher(MethodMatcher.create()
                    .declaredClass(ClassMatcher.create()
                        .usingStrings("kill process group synchronously"))
                    .usingStrings("kill process group synchronously"))
        ).firstOrNull();
        HookDexKit.beforeDexKit(methodData2, loadPackageParam,
            new ActionTiming() {
                @Override
                public void before(@NonNull XC_MethodHook.MethodHookParam param) {

                }
            }
        );*/

            MethodData methodData2 = DexKit.INSTANCE.getDexKitBridge().findMethod(
                FindMethod.create()
                    .matcher(MethodMatcher.create()
                        .declaredClass(ClassMatcher.create()
                            .usingStrings("set package stopped state get error:"))
                        .usingStrings("set package stopped state get error:"))
            ).firstOrNull();
            HookDexKit.beforeDexKit(methodData2, loadPackageParam,
                new ActionTiming() {
                    @Override
                    public void before(@NonNull XC_MethodHook.MethodHookParam param) {
                        param.setResult(null);
                    }
                }
            );

            /*my那里学到的*/
            try {
                Class<?> athena = findClassIfExists(IAthenaService$Stub$Proxy);
                for (Method athenaKill : athena.getDeclaredMethods()) {
                    if (!athenaKill.getReturnType().equals(int.class)) {
                        continue;
                    }
                    if ("athenaKill".contains(athenaKill.getName())) {
                        ArrayList<Object> param = new ArrayList<>(Arrays.asList(athenaKill.getParameterTypes()));
                        param.add(new HookAction() {
                            @Override
                            protected void before(MethodHookParam param) {
                                param.setResult(0);
                            }
                        });
                        findAndHookMethod(athena, athenaKill.getName(), param.toArray());
                    }
                }
            } catch (Throwable throwable) {
                logE("athenaKill", "error: " + throwable);
            }
        } catch (Throwable throwable) {
            logE("AthenaApp", "un error:" + throwable);
            DexKit.INSTANCE.closeDexKit();
        }
        DexKit.INSTANCE.closeDexKit();
    }
}
