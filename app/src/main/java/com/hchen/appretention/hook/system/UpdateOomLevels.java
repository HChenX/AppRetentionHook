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

public final class UpdateOomLevels extends BaseHC {
    private static final int OOM_MIN_FREE_DISCOUNT = 4;
    private Object processListInstance = null;

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
                    processListInstance = thisObject();
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
                    int[] mOomMinFree = getThisField(com.hchen.appretention.data.field.System.mOomMinFree);
                    if (mOomMinFree == null) return;
                    int[] mOomMinFreeArray = Arrays.stream(mOomMinFree).map(operand -> operand / OOM_MIN_FREE_DISCOUNT).toArray();
                    setThisField(com.hchen.appretention.data.field.System.mOomMinFree, mOomMinFreeArray);
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
                        int[] mOomMinFree = getThisField(com.hchen.appretention.data.field.System.mOomMinFree);
                        if (mOomMinFree == null) return;
                        int[] mOomMinFreeArray = Arrays.stream(mOomMinFree).map(operand -> operand / OOM_MIN_FREE_DISCOUNT).toArray();
                        setThisField(com.hchen.appretention.data.field.System.mOomMinFree, mOomMinFreeArray);
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
                    ByteBuffer buffer = getArgs(0);
                    ByteBuffer bufCopy = buffer.duplicate();
                    bufCopy.rewind();
                    if (bufCopy.getInt() == 0) {
                        if (processListInstance == null)
                            return;

                        // false 说明 oomMinFree 未被更改。
                        if (Boolean.FALSE.equals(getAdditionalInstanceField(processListInstance, isChangedOomMinFree))) {
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
                    int[] mOomAdj = getField(processListInstance, com.hchen.appretention.data.field.System.mOomAdj);
                    int[] mOomMinFree = getField(processListInstance, com.hchen.appretention.data.field.System.mOomMinFree);
                    if (mOomMinFree == null || mOomAdj == null)
                        return;

                    int[] mOomMinFreeArray = Arrays.stream(mOomMinFree).map(operand -> operand / OOM_MIN_FREE_DISCOUNT).toArray();
                    setField(processListInstance, System.mOomMinFree, mOomMinFreeArray);
                    setAdditionalInstanceField(processListInstance, isChangedOomMinFree, true);
                    for (int i = 0; i < mOomAdj.length; i++) {
                        bufCopy.putInt(((mOomMinFreeArray[i] * 1024) / 4096));
                        bufCopy.putInt(mOomAdj[i]);
                    }
                }
            }.shouldObserveCall(false)
        );
    }
}
