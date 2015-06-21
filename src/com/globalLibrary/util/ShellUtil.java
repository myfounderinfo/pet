package com.globalLibrary.util;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 执行 shell 命令
 */
@SuppressWarnings("UnusedDeclaration")
public final class ShellUtil {
    private static final String COMMAND_EXIT = "exit\n";
    private static final String COMMAND_LINE_END = "\n";
    private static final String COMMAND_SH = "sh";
    private static final String COMMAND_SU = "su";

    /**
     * 执行一组 shell 命令，默认获取命令执行后的输出
     *
     * @param commands 待执行的命令
     * @param isRoot   是否按 root 执行
     * @return 命令执行结果
     */
    public static CommandResult exec(String[] commands, boolean isRoot) {
        return exec(commands, isRoot, true);
    }

    /**
     * 执行 shell 命令
     *
     * @param command         shell 命令
     * @param isRoot          是否以 root 权限执行
     * @param isNeedResultMsg 是否获得命令执行输出
     * @return 执行结果
     */
    public static CommandResult exec(String command, boolean isRoot, boolean isNeedResultMsg) {
        return exec(new String[]{command}, isRoot, isNeedResultMsg);
    }

    /**
     * 执行一组 shell 命令
     *
     * @param commands        待执行的命令
     * @param isRoot          是否按 root 执行
     * @param isNeedResultMsg 是否需要获得命令执行后的输出
     * @return 命令执行结果
     */
    public static CommandResult exec(String[] commands, boolean isRoot, boolean isNeedResultMsg) {
        if (commands == null || commands.length == 0) {
            return new CommandResult(-1, null, null);
        }
        int resultCode = -1;
        String successMsg = null;
        String errorMsg = null;
        Process process;
        OutputStream out = null;
        InputStream successIn = null, errorIn = null;
        try {
            process = Runtime.getRuntime().exec(isRoot ? COMMAND_SU : COMMAND_SH);
            out = process.getOutputStream();
            for (String command : commands) {
                if (StringUtil.isEmpty(command)) {
                    continue;
                }
                out.write(command.getBytes());
                out.write(COMMAND_LINE_END.getBytes());
                out.flush();
            }
            out.write(COMMAND_EXIT.getBytes());
            out.flush();

            resultCode = process.waitFor();
            if (isNeedResultMsg) {
                successIn = process.getInputStream();
                errorIn = process.getErrorStream();
                successMsg = IOUtil.toString(successIn);
                errorMsg = IOUtil.toString(errorIn);
            }
        } catch (Exception e) {
            LogUtil.e(e);
        } finally {
            IOUtil.close(out);
            IOUtil.close(successIn);
            IOUtil.close(errorIn);
        }
        return new CommandResult(resultCode, successMsg, errorMsg);
    }

    /**
     * 判断是否具有 root 权限
     *
     * @return true 表示具有 root 权限
     */
    public static boolean checkRootPermission() {
        return exec("echo root", true).resultCode == 0;
    }

    /**
     * 执行 shell 命令，默认获取 shell 命令执行后的输出
     *
     * @param command shell 命令
     * @param isRoot  是否以 root 权限执行
     * @return 执行结果
     */
    public static CommandResult exec(String command, boolean isRoot) {
        return exec(new String[]{command}, isRoot, true);
    }

    private ShellUtil() {
    }

    public static class CommandResult {
        public final String errorMsg;
        public final String successMsg;
        public final int resultCode;

        public CommandResult(int resultCode, String successMsg, String errorMsg) {
            this.resultCode = resultCode;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }
    }
}
