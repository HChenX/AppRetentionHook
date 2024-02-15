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
package Com.HChen.Hook.annotation.android;

import static Com.HChen.Hook.param.classpath.System.ActiveServices;
import static Com.HChen.Hook.param.classpath.System.ActivityManagerConstants;
import static Com.HChen.Hook.param.classpath.System.ActivityManagerService;
import static Com.HChen.Hook.param.classpath.System.ActivityManagerService$LocalService;
import static Com.HChen.Hook.param.classpath.System.AppProfiler;
import static Com.HChen.Hook.param.classpath.System.CachedAppOptimizer;
import static Com.HChen.Hook.param.classpath.System.LowMemDetector;
import static Com.HChen.Hook.param.classpath.System.LowMemDetector$LowMemThread;
import static Com.HChen.Hook.param.classpath.System.ProcessList;
import static Com.HChen.Hook.param.classpath.System.ProcessProfileRecord;
import static Com.HChen.Hook.param.classpath.System.ProcessRecord;
import static Com.HChen.Hook.param.classpath.System.ProcessStateRecord;
import static Com.HChen.Hook.param.method.System.checkExcessivePowerUsage;
import static Com.HChen.Hook.param.method.System.getAppStartModeLOSP;
import static Com.HChen.Hook.param.method.System.getBinderFreezeInfo;
import static Com.HChen.Hook.param.method.System.isKilledByAm;
import static Com.HChen.Hook.param.method.System.killAllBackgroundProcesses;
import static Com.HChen.Hook.param.method.System.killAllBackgroundProcessesExcept;
import static Com.HChen.Hook.param.method.System.killAllBackgroundProcessesExceptLSP;
import static Com.HChen.Hook.param.method.System.killAppIfBgRestrictedAndCachedIdleLocked;
import static Com.HChen.Hook.param.method.System.killLocked;
import static Com.HChen.Hook.param.method.System.killProcessesBelowForeground;
import static Com.HChen.Hook.param.method.System.run;
import static Com.HChen.Hook.param.method.System.setMemFactorOverride;
import static Com.HChen.Hook.param.method.System.setOomAdj;
import static Com.HChen.Hook.param.method.System.setOverrideMaxCachedProcesses;
import static Com.HChen.Hook.param.method.System.setProcessMemoryTrimLevel;
import static Com.HChen.Hook.param.method.System.shouldNotKillOnBgRestrictedAndIdle;
import static Com.HChen.Hook.param.method.System.stopInBackgroundLocked;
import static Com.HChen.Hook.param.method.System.trimApplicationsLocked;
import static Com.HChen.Hook.param.method.System.trimMemoryUiHiddenIfNecessaryLSP;
import static Com.HChen.Hook.param.method.System.updateBackgroundRestrictedForUidPackageLocked;
import static Com.HChen.Hook.param.method.System.updateKillBgRestrictedCachedIdle;
import static Com.HChen.Hook.param.method.System.updateLowMemStateLSP;
import static Com.HChen.Hook.param.method.System.waitForPressure;

import android.os.Looper;

import java.nio.ByteBuffer;

import Com.HChen.Hook.mode.Hook;

/**
 * 这些都是已经弃用的 Hook 方法，仅留档。
 */
public class SystemService extends Hook {
    public static String name = "SystemService";

    @Override
    public void init() {
        /*关闭Cpu超时检查*/
        hookAllMethods(ActivityManagerService,
            checkExcessivePowerUsage,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*防止kill冻结的应用*/
        findAndHookMethod(CachedAppOptimizer,
            getBinderFreezeInfo, int.class,
            new HookAction(name) {
                @Override
                protected void after(MethodHookParam param) {
                    int end = (int) param.getResult();
                    if ((end & 1) != 0) {
                        param.setResult(5);
                    }
                }
            }
        );

        /*防止各种原因的kill*/
        findAndHookMethod(ProcessRecord, killLocked,
            String.class, String.class, int.class, int.class, boolean.class,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    boolean hook = false;
                    // "isolated not needed"被隔离的进程只有变成空进程时才会被清理，我认为是合理的
                    switch ((String) param.args[0]) {
                        case "Unable to query binder frozen stats",
                            "Unable to unfreeze", "Unable to freeze binder interface",
                            "start timeout", "crash", "bg anr", "skip anr", "anr",
                            "swap low and too many cached" -> {
                            param.setResult(null);
                            hook = true;
                        }
                    }
                    // "depends on provider",内容提供者被杀导致的调用方被杀，
                    // 这是软件作者的bug，不应该在用户端禁止。
                    // reason.contains("depends on provider")
                    String reason = (String) param.args[0];
                    if (reason.contains("scheduleCrash for")) {
                        param.setResult(null);
                        hook = true;
                    }
                    logSI(null, killLocked, "reason: " + param.args[0] + " description: " + param.args[1]
                        + " reasonCode: " + param.args[2]
                        + " subReason: " + param.args[3]
                        + " noisy: " + param.args[4] + " im hook? " + hook);
                }
            }
        );

        /*禁止killProcessesBelowForeground_和下面重复*/
        findAndHookMethod(ActivityManagerService,
            killProcessesBelowForeground, String.class,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(true);
                }
            }
        );

        /*调整内存因子，一般来说0是无压力*/
        findAndHookMethod(ActivityManagerService,
            setMemFactorOverride, int.class,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.args[0] = 0;
                }
            }
        );

        /*禁止清理全部后台
         * 未经测试,暂时封存*/
        findAndHookMethod(ActivityManagerService,
            killAllBackgroundProcesses,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        hookAllMethods(ActivityManagerService,
            killAllBackgroundProcessesExcept,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*禁止kill除条件之外的所有后台进程
         * 未经测试*/
        findAndHookMethod(ActivityManagerService$LocalService,
            killAllBackgroundProcessesExcept, int.class, int.class,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*禁止清理空进程_空进程不包含任何活动，清理是合理的,牵扯太多*/
        findAndHookMethod(ActivityManagerService,
            trimApplicationsLocked, boolean.class, String.class,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*关闭后台服务限制_可能导致后台异常
         * 需要测试
         * 应该不需要hook*/
        findAndHookMethod(ActivityManagerService,
            getAppStartModeLOSP, int.class,
            String.class, int.class, int.class, boolean.class,
            boolean.class, boolean.class, String.class,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(0);
                }
            }
        );

        /*禁止停止后台空闲服务_与getAppStartModeLOSP的Hook重复_不适合存活的service应该杀掉*/
        findAndHookMethod(ActiveServices,
            stopInBackgroundLocked, int.class,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*调整内存因子，一般来说0是无压力
         * 调用好多*/
        findAndHookConstructor(AppProfiler,
            findClassIfExists(ActivityManagerService),
            Looper.class, findClassIfExists(LowMemDetector),
            new HookAction(name) {
                @Override
                protected void after(MethodHookParam param) {
                    setDeclaredField(param, "mMemFactorOverride", 0);
                }
            }
        );

        /*禁止trim应用内存
         * 当应用处于不可见或不活跃状态时，释放不必要的内存，这是合理的*/
        hookAllMethods(AppProfiler,
            trimMemoryUiHiddenIfNecessaryLSP,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*禁止停止后台受限和缓存的app
         * 是否允许在后台限制和闲置的情况下杀死应用程序进程*/
        hookAllMethods(ProcessList,
            killAppIfBgRestrictedAndCachedIdleLocked,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    // param.setResult(0L);
                    logSI(killAppIfBgRestrictedAndCachedIdleLocked, "app: " + param.args[0]
                        + " nowElapsed: " + param.args[1]);
                }
            }
        );

        /*设置不得清理*/
        findAndHookMethod(ProcessStateRecord,
            shouldNotKillOnBgRestrictedAndIdle, new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(true);
                }
            }
        );

        /*去除受限限制
         * 目前待测试*/
        findAndHookMethod(ProcessList,
            updateBackgroundRestrictedForUidPackageLocked,
            int.class, String.class, boolean.class,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.args[2] = false;
                }
            }
        );

        /*禁止停止后台受限和缓存的app*/
        findAndHookMethod(ProcessList,
            killAppIfBgRestrictedAndCachedIdleLocked,
            ProcessRecord, long.class,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(0L);
                }
            }
        );

        /*禁止kill除条件之外的所有后台进程
         * 未经测试*/
        findAndHookMethod(ProcessList,
            killAllBackgroundProcessesExceptLSP, int.class, int.class,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*虚假设置adj数值，妈的法克太容易爆内存了*/
        findAndHookMethod(ProcessList,
            setOomAdj, int.class, int.class, int.class,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    int pid = (int) param.args[0];
                    int uid = (int) param.args[1];
                    param.args[2] = -1000;
                    param.args[2] = 1001;
                    int amt = 1001;
                    ByteBuffer buffer = ByteBuffer.allocate(16);
                    buffer.putInt(1);
                    buffer.putInt(pid);
                    buffer.putInt(uid);
                    buffer.putInt(amt);
                    callStaticMethod(findClassIfExists(ProcessList),
                        "writeLmkd", buffer, null);
                    param.setResult(null);

                }
            }
        );

        findAndHookMethod(LowMemDetector$LowMemThread,
            run,
            new HookAction(name) {
                @Override
                protected void after(MethodHookParam param) {
                    setInt(param.thisObject, "newPressureState", 0);
                }
            }
        );

        findAndHookMethod(LowMemDetector,
            waitForPressure,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(0);
                }
            }
        );

        /*没必要了*/
        /*谎报内存压力
         * 导致频繁唤醒*/
        findAndHookConstructor(LowMemDetector,
            findClassIfExists(ActivityManagerService),
            new HookAction(name) {
                @Override
                protected void after(MethodHookParam param) {
                    setDeclaredField(param, "mPressureState", 0);
                }
            }
        );

        // 禁止更新压力
        findAndHookMethod(LowMemDetector$LowMemThread,
            run,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*禁止kill受限的缓存*/
        findAndHookMethod(ActivityManagerConstants,
            updateKillBgRestrictedCachedIdle,
            new HookAction(name) {
                @Override
                protected void after(MethodHookParam param) {
                }
            }
        );

        /*设置自定义最大缓存数量*/
        findAndHookMethod(ActivityManagerConstants,
            setOverrideMaxCachedProcesses, int.class,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.args[0] = Integer.MAX_VALUE;
                }
            }
        );

        /*谎称已经被杀_调用多，容易卡开机*/
        findAndHookMethod(ProcessRecord,
            isKilledByAm,
            new HookAction(name) {
                @Override
                protected void after(MethodHookParam param) {
                    param.setResult(true);
                }
            }
        );

        /*锁定内存因子*/
        findAndHookConstructor(AppProfiler,
            findClassIfExists(ActivityManagerService),
            Looper.class, findClassIfExists(LowMemDetector),
            new HookAction(name) {
                @Override
                protected void after(MethodHookParam param) {
                    setDeclaredField(param, "mMemFactorOverride", -1);
                    setDeclaredField(param, "mLastMemoryLevel", 0);
                }
            }
        );

        /*拒绝根据压力trim进程_我TM看不懂逻辑，什么内存因子修剪等级去死吧。
         * 看了最终调用，触发GC回收系统垃圾内存，所以可以保留。
         * */
        findAndHookMethod(AppProfiler,
            updateLowMemStateLSP, int.class, int.class, int.class,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(false);

                }
            }
        );

        findAndHookMethod(ActivityManagerService,
            setProcessMemoryTrimLevel, String.class, int.class, int.class,
            new HookAction(name) {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.args[2] = 0;
                }
            }
        );

        findAndHookConstructor(ProcessProfileRecord,
            findClassIfExists(ProcessRecord),
            new HookAction(name) {
                @Override
                protected void after(MethodHookParam param) {
                    setObject(param.thisObject, "mTrimMemoryLevel", 5);
                }
            }
        );
    }
}
