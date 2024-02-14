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
package Com.HChen.Hook.utils;

import java.io.DataOutputStream;

public class ShellUtils {

    /* 用于检查是否授予Su权限,执行简单的Shell命令
    和返回执行结果是否成功. */
    public static boolean RootCommand(String command) {
        Process process;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su -c true");
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return false;
            }
            try {
                process = Runtime.getRuntime().exec("su");
                os = new DataOutputStream(process.getOutputStream());
                os.writeBytes(command + "\n");
                os.writeBytes("exit\n");
                os.flush();
                process.waitFor();
            } catch (Exception e) {
                return false;
            } finally {
                try {
                    if (os != null) {
                        os.close();
                    }
                    if (process != null) {
                        process.destroy();
                    }
                } catch (Exception ignored) {
                }
            }
            return true;
        } catch (Exception t) {
            return false;
        }
    }

}
