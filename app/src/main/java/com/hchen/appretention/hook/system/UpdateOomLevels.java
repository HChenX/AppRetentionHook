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

 * Copyright (C) 2023-2024 HChenX
 */
package com.hchen.appretention.hook.system;

import static com.hchen.appretention.data.field.System.isChangedOomMinFree;
import static com.hchen.appretention.data.method.System.onLmkdConnect;
import static com.hchen.appretention.data.method.System.updateOomLevels;
import static com.hchen.appretention.data.method.System.writeLmkd;
import static com.hchen.appretention.data.path.System.ProcessList;

import com.hchen.appretention.data.field.System;
import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.hook.IHook;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * 修改系统 oomMinFree 的值
 *
 * @author 焕晨HChen
 */
public final class UpdateOomLevels extends BaseHC {
    private static final int OOM_MIN_FREE_DISCOUNT = 4;
    private Object mProcessListInstance = null;

    @Override
    public void init() {
        updateOomLevels();
    }

    private void updateOomLevels() {
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
            OutputStream.class, new IHook() {
                @Override
                public void before() {
                    if (Boolean.TRUE.equals(getThisAdditionalInstanceField(isChangedOomMinFree)))
                        return;
                    int[] mOomMinFree = (int[]) getThisField(System.mOomMinFree);
                    if (mOomMinFree == null) return;
                    int[] mOomMinFreeArray = Arrays.stream(mOomMinFree).map(operand -> operand / OOM_MIN_FREE_DISCOUNT).toArray();
                    setThisField(System.mOomMinFree, mOomMinFreeArray);
                    setThisAdditionalInstanceField(isChangedOomMinFree, true);
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
                @Override
                public void before() {
                    setThisAdditionalInstanceField(isChangedOomMinFree, false);
                }

                @Override
                public void after() {
                    if ((getArgs(2) instanceof Boolean b) && !b) {
                        int[] mOomMinFree = (int[]) getThisField(System.mOomMinFree);
                        if (mOomMinFree == null) return;
                        int[] mOomMinFreeArray = Arrays.stream(mOomMinFree).map(operand -> operand / OOM_MIN_FREE_DISCOUNT).toArray();
                        setThisField(System.mOomMinFree, mOomMinFreeArray);
                        setThisAdditionalInstanceField(isChangedOomMinFree, true);
                    }
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
                    ByteBuffer bufCopy = buffer.duplicate();
                    bufCopy.rewind();
                    if (bufCopy.getInt() == 0) {
                        if (mProcessListInstance == null)
                            return;

                        // false 说明 oomMinFree 未被更改。
                        if (Boolean.FALSE.equals(getAdditionalInstanceField(mProcessListInstance, isChangedOomMinFree))) {
                            setOomMinFreeBuf(bufCopy);
                            setArgs(0, buffer);
                        }
                    }
                }

                /*
                 * 设置 OomMinFree 值。
                 * */
                private void setOomMinFreeBuf(ByteBuffer bufCopy) {
                    bufCopy.rewind();
                    bufCopy.putInt(0);
                    int[] mOomAdj = (int[]) getField(mProcessListInstance, System.mOomAdj);
                    int[] mOomMinFree = (int[]) getField(mProcessListInstance, System.mOomMinFree);
                    if (mOomMinFree == null || mOomAdj == null)
                        return;

                    int[] mOomMinFreeArray = Arrays.stream(mOomMinFree).map(operand -> operand / OOM_MIN_FREE_DISCOUNT).toArray();
                    setField(mProcessListInstance, System.mOomMinFree, mOomMinFreeArray);
                    setAdditionalInstanceField(mProcessListInstance, isChangedOomMinFree, true);
                    for (int i = 0; i < mOomAdj.length; i++) {
                        bufCopy.putInt(((mOomMinFreeArray[i] * 1024) / 4096));
                        bufCopy.putInt(mOomAdj[i]);
                    }
                }
            }.shouldObserveCall(false)
        );
    }
}
