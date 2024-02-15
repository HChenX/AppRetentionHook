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
package com.hchen.appretention.annotation.miui;

import static com.hchen.appretention.param.classpath.Miui.GameMemoryCleaner;
import static com.hchen.appretention.param.classpath.Miui.MiuiMemReclaimer;
import static com.hchen.appretention.param.classpath.Miui.MiuiMemReclaimer$CompactorHandler;
import static com.hchen.appretention.param.classpath.Miui.MiuiMemoryService;
import static com.hchen.appretention.param.classpath.Miui.MiuiMemoryService$ConnectionHandler;
import static com.hchen.appretention.param.classpath.Miui.MiuiMemoryService$MiuiMemServiceThread;
import static com.hchen.appretention.param.classpath.Miui.PeriodicCleanerService$MyHandler;
import static com.hchen.appretention.param.classpath.Miui.PeriodicCleanerService$PeriodicShellCmd;
import static com.hchen.appretention.param.classpath.Miui.PreloadAppControllerImpl;
import static com.hchen.appretention.param.classpath.Miui.PreloadLifecycle;
import static com.hchen.appretention.param.classpath.Miui.ProcessMemoryCleaner;
import static com.hchen.appretention.param.classpath.Miui.ProcessMemoryCleaner$H;
import static com.hchen.appretention.param.classpath.Miui.ScoutDisplayMemoryManager;
import static com.hchen.appretention.param.classpath.Miui.ScoutHelper;
import static com.hchen.appretention.param.classpath.Miui.SmartCpuPolicyManager;
import static com.hchen.appretention.param.method.Miui.checkBackgroundProcCompact;
import static com.hchen.appretention.param.method.Miui.handleLimitCpuException;
import static com.hchen.appretention.param.method.Miui.handleMessage;
import static com.hchen.appretention.param.method.Miui.isEnableResumeFeature;
import static com.hchen.appretention.param.method.Miui.run;
import static com.hchen.appretention.param.method.Miui.runClean;
import static com.hchen.appretention.param.method.Miui.startPreloadApp;

import android.content.Context;
import android.os.Message;

import com.hchen.appretention.mode.Hook;

import java.io.PrintWriter;

/**
 * 这些都是已经弃用的 Hook 方法，仅留档。
 */
public class MiuiService extends Hook {
    public static String name = "MiuiService";

    @Override
    public void init() {
        /*设置禁止Scout功能*/
        findAndHookConstructor(ScoutDisplayMemoryManager,
            new HookAction(name) {
                @Override
                protected void after(MethodHookParam param) {
                    setDeclaredField(param, "ENABLE_SCOUT_MEMORY_MONITOR", false);
                    // 报告内存泄露？
                    // getDeclaredField(param, "SCOUT_MEMORY_DISABLE_KGSL", false);
                }
            }
        );

        /*关闭Scout的一个功能，
        内存泄露恢复功能，
        注意启用改功能可能使导致内存泄露的程序被杀，
        但这是合理的*/
        findAndHookMethod(ScoutDisplayMemoryManager,
            isEnableResumeFeature,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(false);
                }
            }
        );

        /*关闭一堆Scout的功能*/
        findAndHookConstructor(ScoutHelper,
            new HookAction(name) {
                @Override
                protected void after(MethodHookParam param) {
                    // 系统监察功能 关闭可以节省功耗？
                    setBoolean(param.thisObject, "ENABLED_SCOUT", false);
                    setBoolean(param.thisObject, "ENABLED_SCOUT_DEBUG", false);
                    setBoolean(param.thisObject, "BINDER_FULL_KILL_PROC", false);
                    // setBoolean(param.thisObject, "SCOUT_BINDER_GKI", false);
                    // 是崩溃相关
                    setBoolean(param.thisObject, "PANIC_D_THREAD", false);
                    setBoolean(param.thisObject, "SYSRQ_ANR_D_THREAD", false);
                    setBoolean(param.thisObject, "PANIC_ANR_D_THREAD", false);
                    setBoolean(param.thisObject, "DISABLE_AOSP_ANR_TRACE_POLICY", true);
                }
            }
        );

        /*关闭内存回收功能，寄生于游戏清理*/
        hookAllConstructors(GameMemoryCleaner,
            new HookAction(name) {
                @Override
                protected void after(MethodHookParam param) {
                    setDeclaredField(param, "IS_MEMORY_CLEAN_ENABLED", false);
                }
            }
        );

        /*禁用预加载APP，我对此功能存怀疑态度*/
        findAndHookMethod(PreloadAppControllerImpl,
            startPreloadApp, PreloadLifecycle,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    setBoolean(param.thisObject, "ENABLE", false);
                }
            }
        );

        /*禁用PeriodicCleaner的响应
         * 与上面重复*/
        findAndHookMethod(PeriodicCleanerService$MyHandler,
            handleMessage,
            Message.class,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*禁用PeriodicCleaner清理
         * 与上面重复*/
        findAndHookMethod(PeriodicCleanerService$PeriodicShellCmd,
            runClean, PrintWriter.class,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*禁止ProcessMemoryCleaner$H响应*/
        findAndHookMethod(ProcessMemoryCleaner$H,
            handleMessage, Message.class,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        hookAllMethods(ProcessMemoryCleaner,
            checkBackgroundProcCompact,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*禁止Cpu使用检查
         * 调用killProcess*/
        findAndHookMethod(SmartCpuPolicyManager,
            handleLimitCpuException, int.class,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*多余操作*/
        // 禁用MiuiMemoryService
        findAndHookMethod(MiuiMemoryService$MiuiMemServiceThread,
            run,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        // 禁用MiuiMemoryService
        findAndHookMethod(MiuiMemoryService$ConnectionHandler,
            run,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*禁用MiuiMemoryService
         * 似乎控制内存压缩，不需要关闭*/
        findAndHookConstructor(MiuiMemoryService,
            Context.class,
            new HookAction(name) {
                @Override
                protected void after(MethodHookParam param) {
                    setDeclaredField(param, "sCompactionEnable", false);
                    setDeclaredField(param, "sCompactSingleProcEnable", false);
                    setDeclaredField(param, "sWriteEnable", false);
                }
            }
        );

        /*禁用mi_reclaim
         * 什么陈年逻辑，看不懂，似乎和压缩有关*/
        findAndHookConstructor(MiuiMemReclaimer,
            new HookAction(name) {
                @Override
                protected void after(MethodHookParam param) {
                    setDeclaredField(param, "RECLAIM_IF_NEEDED", false);
                    setDeclaredField(param, "USE_LEGACY_COMPACTION", false);
                }
            }
        );

        /*压缩进程的*/
        /*禁用MiuiMemReclaimer*/
        findAndHookMethod(MiuiMemReclaimer$CompactorHandler,
            handleMessage,
            Message.class, new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );
    }
}
