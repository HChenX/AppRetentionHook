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

 * Copyright (C) 2023-2025 HChenX
 */
package com.hchen.appretention.hook.system.opt;

import static com.hchen.appretention.data.method.SystemMethod.onLmkdConnect;
import static com.hchen.appretention.data.method.SystemMethod.updateOomLevels;
import static com.hchen.appretention.data.method.SystemMethod.writeLmkd;
import static com.hchen.appretention.data.path.SystemClass.ProcessList;
import static com.hchen.hooktool.tool.CoreTool.getField;
import static com.hchen.hooktool.tool.CoreTool.hookConstructor;
import static com.hchen.hooktool.tool.CoreTool.hookMethod;
import static com.hchen.hooktool.tool.CoreTool.setField;

import android.system.Os;
import android.system.OsConstants;

import com.hchen.appretention.data.field.SystemField;
import com.hchen.hooktool.hook.IHook;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * 修改系统 oomMinFree 的值
 *
 * @author 焕晨HChen
 */
public final class OomLevelsOpt {
    private static final int OOM_MIN_FREE_DISCOUNT = 3;
    private static final int PAGE_SIZE = (int) Os.sysconf(OsConstants._SC_PAGESIZE);
    private static Object mProcessListInstance = null;

    /*
     *  K50 12G
     * oom adj: [0, 100, 200, 250, 900, 950], oom min free: [73728, 92160, 110592, 129024, 221184, 322560]
     * -900 75497472 -800 75497472 -700 75497472 0 75497472 100 94371840 200 113246208
     * 225 132120576 250 132120576 300 226492416 400 226492416 500 226492416 600 226492416 700 226492416
     * 800 226492416 900 226492416 999 330301440
     * */
    public static void init() {
        /*
         * 获取 ProcessList 的实例
         * */
        hookConstructor(ProcessList,
            new IHook() {
                @Override
                public void after() {
                    mProcessListInstance = thisObject();
                }
            }
        );

        /*
         * 当系统连接 lmkd 时会初始化一些 lmkd 参数。
         * 本 hook 将修改系统初始化时写入的 oomMinFree 参数。
         * */
        hookMethod(ProcessList,
            onLmkdConnect,
            OutputStream.class,
            new IHook() {
                @Override
                public void before() {
                    updateOomMinFree(thisObject());
                    // if (Boolean.TRUE.equals(getThisAdditionalInstanceField(isChangedOomMinFree)))
                    //     return;
                    // int[] mOomMinFree = (int[]) getThisField(SystemField.mOomMinFree);
                    // if (mOomMinFree == null) return;
                    // int[] mOomMinFreeArray = Arrays.stream(mOomMinFree).map(operand -> operand / OOM_MIN_FREE_DISCOUNT).toArray();
                    // setThisField(SystemField.mOomMinFree, mOomMinFreeArray);
                    // setThisAdditionalInstanceField(isChangedOomMinFree, true);
                }
            }
        );

        /*
         * 系统更新 oomLevel 时使用，监控 oomMinFree 更改。
         * */
        hookMethod(ProcessList,
            updateOomLevels,
            int.class, int.class, boolean.class,
            new IHook() {
                // @Override
                // public void before() {
                //     setThisAdditionalInstanceField(isChangedOomMinFree, false);
                // }

                @Override
                public void after() {
                    updateOomMinFree(thisObject());
                    // if ((getArgs(2) instanceof Boolean b) && !b) {
                    //     int[] mOomMinFree = (int[]) getThisField(SystemField.mOomMinFree);
                    //     if (mOomMinFree == null) return;
                    //     int[] mOomMinFreeArray = Arrays.stream(mOomMinFree).map(operand -> operand / OOM_MIN_FREE_DISCOUNT).toArray();
                    //     setThisField(SystemField.mOomMinFree, mOomMinFreeArray);
                    //     setThisAdditionalInstanceField(isChangedOomMinFree, true);
                    // }
                }
            }
        );

        /*
         * 设置一些 lmkd 参数。
         * */
        hookMethod(ProcessList,
            writeLmkd,
            ByteBuffer.class, ByteBuffer.class,
            new IHook() {

                @Override
                public void before() {
                    ByteBuffer buffer = (ByteBuffer) getArgs(0);
                    if (buffer == null) return;

                    ByteBuffer bufCopy = buffer.duplicate();
                    bufCopy.rewind();
                    if (bufCopy.getInt() == 0) {
                        setOomMinFreeBuf(bufCopy);
                        setArgs(0, buffer);
                    }
                }

                /*
                 * 设置 OomMinFree 值。
                 * */
                private void setOomMinFreeBuf(ByteBuffer bufCopy) {
                    int[] mOomAdj = (int[]) getField(mProcessListInstance, SystemField.mOomAdj);
                    int[] mOomMinFree = (int[]) getField(mProcessListInstance, SystemField.mOomMinFree);
                    if (mOomMinFree == null || mOomAdj == null)
                        return;

                    int[] mOomMinFreeArray = updateOomMinFree(mProcessListInstance);
                    if (mOomMinFreeArray == null) return;
                    // setAdditionalInstanceField(mProcessListInstance, isChangedOomMinFree, true);

                    bufCopy.rewind();
                    bufCopy.putInt(0);
                    for (int i = 0; i < mOomAdj.length; i++) {
                        bufCopy.putInt(((mOomMinFreeArray[i] * 1024) / PAGE_SIZE));
                        bufCopy.putInt(mOomAdj[i]);
                    }
                }
            }
        );
    }

    private static int[] updateOomMinFree(Object processListInstance) {
        int[] mOomMinFree = (int[]) getField(processListInstance, SystemField.mOomMinFree);
        if (mOomMinFree == null)
            return null;

        int[] mOomMinFreeArray = Arrays.stream(mOomMinFree).map(operand -> operand / OOM_MIN_FREE_DISCOUNT).toArray();
        setField(processListInstance, SystemField.mOomMinFree, mOomMinFreeArray);
        return mOomMinFreeArray;
    }
}
