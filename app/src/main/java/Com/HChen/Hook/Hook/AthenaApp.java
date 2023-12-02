package Com.HChen.Hook.Hook;

import static Com.HChen.Hook.Param.Name.OplusName.AppIAthenaKillCallback$Stub;
import static Com.HChen.Hook.Param.Name.OplusName.AppIAthenaKillCallback$Stub$Proxy;
import static Com.HChen.Hook.Param.Name.OplusName.AthenaService$OKillerBinder$AthenaKillCallback;
import static Com.HChen.Hook.Param.Name.OplusName.IAthenaKillCallback$Stub;
import static Com.HChen.Hook.Param.Name.OplusName.IAthenaKillCallback$Stub$Proxy;
import static Com.HChen.Hook.Param.Name.OplusName.IAthenaService$Stub$Proxy;
import static Com.HChen.Hook.Param.Name.OplusName.RemoteService;

import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Com.HChen.Hook.Mode.DexKit.DexKit;
import Com.HChen.Hook.Mode.HookMode;

public class AthenaApp extends HookMode implements AthenaKill.pidCallBackListener {

    public static String name = "AthenaApp";

    public static int myPid = 0;

    public static int test = 0;

    public static void getPid(int pid) {
        myPid = pid;
    }

    @Override
    public void init() {
        /*防止意外报错导致的未能关闭DexKit*/
        try {
            /*注册回调*/
            AthenaKill.setPidCallBackListener(this);
            /*boolean c*/
            MethodData methodData = DexKit.INSTANCE.getDexKitBridge().findMethod(
                FindMethod.create()
                    .matcher(MethodMatcher.create()
                        .declaredClass(ClassMatcher.create()
                            .usingStrings(" is reused by others, skip kill "))
                        .usingStrings(" is reused by others, skip kill "))
            ).firstOrNull();
            hookMethod(getMethodInstance(methodData),
                new HookAction() {
                    @Override
                    public String hookLog() {
                        return name;
                    }

                    @Override
                    protected void before(MethodHookParam param) {
                        String reason = (String) param.args[7];
                        /*不稳定*/
                        /*int code = (int) param.args[5];*/
                        if (!"oneclick".equals(reason)) {
                            param.setResult(false);
                        }
                    }
                }
            );
            /*HookDexKit.beforeDexKit(methodData, loadPackageParam,
                new ActionTiming() {
                    @Override
                    public void before(@NonNull XC_MethodHook.MethodHookParam param) {
                        String reason = (String) param.args[7];
                        *//*不稳定*//*
             *//*int code = (int) param.args[5];*//*
                        if (!"oneclick".equals(reason)) {
                            param.setResult(false);
                        }
                    }
                }
            );*/
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
            hookMethod(getMethodInstance(methodData1),
                new HookAction() {
                    @Override
                    public String hookLog() {
                        return name;
                    }

                    @Override
                    protected void before(MethodHookParam param) {
                        String reason = (String) param.args[6];
                        switch (reason) {
                            case "swipe up", "removeTask" -> {

                            }
                            default -> param.setResult(null);
                        }
                    }
                }
            );
            /*HookDexKit.beforeDexKit(methodData1, loadPackageParam,
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
            );*/
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
            hookMethod(getMethodInstance(methodData2),
                new HookAction() {
                    @Override
                    public String hookLog() {
                        return name;
                    }

                    @Override
                    protected void before(MethodHookParam param) {
                        param.setResult(null);
                    }
                }
            );
            /*HookDexKit.beforeDexKit(methodData2, loadPackageParam,
                new ActionTiming() {
                    @Override
                    public void before(@NonNull XC_MethodHook.MethodHookParam param) {
                        param.setResult(null);
                    }
                }
            );*/

            /*void g*/
            MethodData methodData3 = DexKit.INSTANCE.getDexKitBridge().findMethod(
                FindMethod.create()
                    .matcher(MethodMatcher.create()
                        .declaredClass(ClassMatcher.create()
                            .usingStrings("com.android.server.am.OplusAthenaAmManager error"))
                        .paramTypes(List.class, List.class)
                    )
            ).firstOrNull();
            hookMethod(getMethodInstance(methodData3),
                new HookAction() {
                    @Override
                    public String hookLog() {
                        return name;
                    }

                    @Override
                    protected void before(MethodHookParam param) {
                        param.setResult(null);
                    }
                }
            );

            /*void a*/
            MethodData methodData4 = DexKit.INSTANCE.getDexKitBridge().findMethod(
                FindMethod.create()
                    .matcher(MethodMatcher.create()
                        .declaredClass(ClassMatcher.create()
                            .usingStrings("com.android.server.am.OplusAthenaAmManager error"))
                        .usingStrings("policyAction")
                    )
            ).firstOrNull();
            hookMethod(getMethodInstance(methodData4),
                new HookAction() {
                    @Override
                    public String hookLog() {
                        return name;
                    }

                    @Override
                    protected void before(MethodHookParam param) {
                        Object o = param.args[param.args.length - 1];
                        if (o != null) {
                            param.setResult(null);
                        }
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
                    if (athenaKill.getName().contains("athenaKill")) {
                        hookAthena(athena, athenaKill, 0);
                        /*ArrayList<Object> param = new ArrayList<>(Arrays.asList(athenaKill.getParameterTypes()));
                        param.add(new HookAction() {
                            @Override
                            protected void before(MethodHookParam param) {
                                param.setResult(0);
                            }
                        });
                        findAndHookMethod(athena, athenaKill.getName(), param.toArray());*/
                    }
                }
            } catch (Throwable throwable) {
                logE("athenaKill", "" + throwable);
            }

            try {
                Class<?> remoteService = findClassIfExists(RemoteService);
                for (Method remote : remoteService.getDeclaredMethods()) {
                    if (!remote.getReturnType().equals(int.class)) {
                        continue;
                    }
                    if (remote.getName().contains("athenaKill")) {
                        hookAthena(remoteService, remote, 0);
                        /*ArrayList<Object> param = new ArrayList<>(Arrays.asList(remote.getParameterTypes()));
                        param.add(new HookAction() {
                            @Override
                            protected void before(MethodHookParam param) {
                                param.setResult(0);
                            }
                        });
                        findAndHookMethod(remoteService, remote.getName(), param.toArray());*/
                    }
                }
            } catch (Throwable throwable) {
                logE("remoteService", "" + throwable);
            }

            try {
                String[] mClass = new String[]{
                    IAthenaKillCallback$Stub$Proxy,
                    AppIAthenaKillCallback$Stub$Proxy,
                    AthenaService$OKillerBinder$AthenaKillCallback,
                    IAthenaKillCallback$Stub,
                    AppIAthenaKillCallback$Stub
                };
                for (String getClass : mClass) {
                    Class<?> killCall = findClassIfExists(getClass);
                    for (Method killCallBack : killCall.getDeclaredMethods()) {
                        if (killCallBack.getReturnType().equals(void.class)) {
                            switch (killCallBack.getName()) {
                                case "onAppKilled", "onAthenaCleanup", "onClearParamChanged" -> {
                                    hookAthena(killCall, killCallBack, null);
                                }
                            }
                        } else if (killCallBack.getReturnType().equals(boolean.class)) {
                            if (killCallBack.getName().equals("onTransact")) {
                                hookAthena(killCall, killCallBack, true);
                            }
                        }
                    }
                }
            } catch (Throwable throwable) {
                logE("killCallBack", "" + throwable);
            }

        } catch (Throwable throwable) {
            if (myPid != 0 && test < 5) {
                callStaticMethod(findClassIfExists("android.os.Process"), "sendSignal", myPid, 9);
                logE("AthenaApp", throwable + " will kill athena, pid: " + myPid + " number of times: " + test);
                myPid = 0;
                test++;
            }
        }
    }

    public void hookAthena(Class<?> mClass, Method method, Object result) {
        ArrayList<Object> param = new ArrayList<>(Arrays.asList(method.getParameterTypes()));
        param.add(new HookAction() {
            @Override
            public String hookLog() {
                return name;
            }

            @Override
            protected void before(MethodHookParam param) {
                param.setResult(result);
            }
        });
        findAndHookMethod(mClass, method.getName(), param.toArray());
    }

    @Override
    public void pidCallBack(int pid) {
        myPid = pid;
    }
}
