package com.hchen.appretention.hook.powerkeeper;

import static com.hchen.appretention.data.method.PowerKeeper.kill;
import static com.hchen.appretention.data.path.PowerKeeper.ProcessManager;

import com.hchen.hooktool.BaseHC;

public class PowerKeeper extends BaseHC {
    @Override
    public void init() {
        /*
         * 禁止电量和性能杀后台
         * */
        hookAllMethod(ProcessManager,
            kill,
            returnResult(false)
        );
    }
}
