package Com.HChen.Hook.Utils;

import java.io.DataOutputStream;

public class ShellUtils {
    public static boolean RootCommand(String pkgCodePath) {
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
                os.writeBytes(pkgCodePath + "\n");
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
