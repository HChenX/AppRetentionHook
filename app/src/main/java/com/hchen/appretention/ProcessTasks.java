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
package com.hchen.appretention;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

/**
 * 自动生成代码的流程
 *
 * @author 焕晨HChen
 */
public class ProcessTasks {
    private static final String TAG = "[FURRY!!]";
    private static final ArrayList<FutureTask<String>> futureTaskList = new ArrayList<>();
    private static final ArrayList<String> importList = new ArrayList<>();
    private static final ConcurrentHashMap<String, String> hookFilesHashMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ArrayList<String>> hookContentHashMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ArrayList<String>> readCopyContentHashMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        ArrayList<String> hookFilePathList = Arrays.stream(strToArray(args[0])).filter(s -> FileHelper.exists(s) && s.contains("\\hook\\"))
            .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<String> importFilePathList = Arrays.stream(strToArray(args[0])).filter(s -> FileHelper.exists(s) && s.contains("\\data\\"))
            .collect(Collectors.toCollection(ArrayList::new));
        printLogo();
        formatImportList(importFilePathList);
        readAllHookFileContent(hookFilePathList);
        readCopyContent();
        writeCopyContent();
        reportAllDone();
        writeHookContentChange();
        System.exit(0);
    }

    public static void formatImportList(ArrayList<String> importFilePathList) {
        for (String str : importFilePathList) {
            importList.add("import static " +
                str.substring(str.lastIndexOf("com"))
                    .replace("\\", ".")
                    .replace("java", "")
                + "*;");
        }
    }

    public static void readAllHookFileContent(ArrayList<String> hookFilePathList) {
        for (String hookFilePath : hookFilePathList) {
            FutureTask<String> futureTask = new FutureTask<>(() -> {
                String hookFileName = hookFilePath.substring(hookFilePath.lastIndexOf("\\") + 1, hookFilePath.lastIndexOf("."));
                hookFilesHashMap.put(hookFileName, hookFilePath);
                hookContentHashMap.put(hookFileName, FileHelper.read(hookFilePath));
                return "DONE";
            });
            futureTaskList.add(futureTask);
            new Thread(futureTask).start();
        }
        waitDone();
    }

    public static void readCopyContent() {
        hookContentHashMap.forEach((s, list) -> {
            FutureTask<String> futureTask = new FutureTask<>(new Callable<>() {
                String[] targetFileArray = new String[]{};
                boolean enterTargetArea = false;
                boolean startRecordCopyContent = false;
                final ArrayList<String> copyContent = new ArrayList<>();

                @Override
                public String call() throws Exception {
                    for (String content : list) {
                        if (content.contains("init()")) enterTargetArea = true;
                        if (!enterTargetArea) continue;
                        if (content.contains("// END")) {
                            if (startRecordCopyContent)
                                throwAndExit("Will end copy state, but start copy flag is true! file: " + s);
                            break;
                        }
                        if (startRecordCopyContent) {
                            if (content.contains("// DONE")) {
                                startRecordCopyContent = false;
                                for (String target : targetFileArray) {
                                    if (readCopyContentHashMap.get(target) == null)
                                        readCopyContentHashMap.put(target, new ArrayList<>(copyContent));
                                    else
                                        readCopyContentHashMap.get(target).addAll(new ArrayList<>(copyContent));
                                }
                                targetFileArray = new String[]{};
                                copyContent.clear();
                                continue;
                            }
                            copyContent.add(content);
                        }
                        if (content.contains("// FURRY") && !content.contains("OK") && !startRecordCopyContent) {
                            startRecordCopyContent = true;
                            targetFileArray = strToArray(findTargetFiles(content));
                        }
                    }
                    return "DONE";
                }
            });
            futureTaskList.add(futureTask);
            new Thread(futureTask).start();
        });
        waitDone();
        if (readCopyContentHashMap.isEmpty()) {
            log("本次无需生成任何内容！跳过！");
            System.exit(0);
        }
    }

    public static void writeCopyContent() {
        readCopyContentHashMap.forEach((s, list) -> {
            FutureTask<String> futureTask = new FutureTask<>(() -> {
                ArrayList<String> hookContent = hookContentHashMap.get(s);
                if (hookContent == null)
                    throwAndExit("Hook content is null? file: " + s);
                assert hookContent != null;
                replaceImport(hookContent);
                int startAppendIndex = -1;
                for (int i = 0; i < hookContent.size(); i++) {
                    if (hookContent.get(i).contains("copy()")) {
                        startAppendIndex = i + 1;
                        break;
                    }
                }
                if (startAppendIndex == -1)
                    throwAndExit("Failed to find append index! file: " + s);
                hookContent.addAll(startAppendIndex, list);
                return "DONE";
            });
            futureTaskList.add(futureTask);
            new Thread(futureTask).start();
        });
        waitDone();
    }

    public static void replaceImport(ArrayList<String> list) {
        int startAppendIndex = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).contains("package")) {
                startAppendIndex = i + 2;
                break;
            }
        }
        list.addAll(startAppendIndex, importList);
    }

    public static void reportAllDone() {
        hookContentHashMap.forEach((s, list) -> {
            FutureTask<String> futureTask = new FutureTask<>(() -> {
                ListIterator<String> listIterator = list.listIterator();
                while (listIterator.hasNext()) {
                    String str = listIterator.next();
                    if (str.contains("STATE: ")) {
                        listIterator.set(str.substring(0, str.lastIndexOf(":") + 2) + "OK");
                    }
                }
                return "DONE";
            });
            futureTaskList.add(futureTask);
            new Thread(futureTask).start();
        });
        waitDone();
    }

    public static void writeHookContentChange() {
        hookContentHashMap.forEach((s, list) -> {
            String path = hookFilesHashMap.get(s);
            FileHelper.write(path, list);
            log("成功生成并写入！目标文件>>>>>" + path);
        });
    }

    public static void waitDone() {
        while (!futureTaskList.isEmpty()) {
            futureTaskList.removeIf(FutureTask::isDone);
            sleep(50);
        }
    }

    public static String findTargetFiles(String content) {
        boolean startAppend = false;
        StringBuilder stringBuilder = new StringBuilder();
        for (char c : content.toCharArray()) {
            if (c == '[') startAppend = true;
            if (startAppend) {
                stringBuilder.append(c);
                if (c == ']') break;
            }
        }
        return stringBuilder.toString();
    }

    public static String[] strToArray(String str) {
        return str.replace("[", "")
            .replace(" ", "")
            .replace("]", "")
            .split(",");
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void throwAndExit(String msg) {
        new RuntimeException(msg).printStackTrace();
        System.exit(1);
    }

    public static void log(Object msg) {
        log(TAG, msg);
    }

    public static void log(String tag, Object msg) {
        System.out.println(tag + " >>>>>>" + msg);
    }

    /*
     ________  ___  ___   ________   ________       ___    ___
    |\  _____\|\  \|\  \ |\   __  \ |\   __  \     |\  \  /  /|
    \ \  \__/ \ \  \\\  \\ \  \|\  \\ \  \|\  \    \ \  \/  / /
     \ \   __\ \ \  \\\  \\ \   _  _\\ \   _  _\    \ \    / /
      \ \  \_|  \ \  \\\  \\ \  \\  \|\ \  \\  \|    \/  /  /
       \ \__\    \ \_______\\ \__\\ _\ \ \__\\ _\  __/  / /
        \|__|     \|_______| \|__|\|__| \|__|\|__||\___/ /
                                                  \|___|/
    * */
    private static void printLogo() {
        final String RESET = "\033[0m";  // Text Reset
        final String BLUE = "\033[0;34m";   // BLUE
        System.out.println(BLUE + " ________  ___  ___   ________   ________       ___    ___ " + RESET);
        System.out.println(BLUE + "|\\  _____\\|\\  \\|\\  \\ |\\   __  \\ |\\   __  \\     |\\  \\  /  /|" + RESET);
        System.out.println(BLUE + "\\ \\  \\__/ \\ \\  \\\\\\  \\\\ \\  \\|\\  \\\\ \\  \\|\\  \\    \\ \\  \\/  / /" + RESET);
        System.out.println(BLUE + " \\ \\   __\\ \\ \\  \\\\\\  \\\\ \\   _  _\\\\ \\   _  _\\    \\ \\    / / " + RESET);
        System.out.println(BLUE + "  \\ \\  \\_|  \\ \\  \\\\\\  \\\\ \\  \\\\  \\|\\ \\  \\\\  \\|    \\/  /  /  " + RESET);
        System.out.println(BLUE + "   \\ \\__\\    \\ \\_______\\\\ \\__\\\\ _\\ \\ \\__\\\\ _\\  __/  / /    " + RESET);
        System.out.println(BLUE + "    \\|__|     \\|_______| \\|__|\\|__| \\|__|\\|__||\\___/ /     " + RESET);
        System.out.println(BLUE + "                                              \\|___|/      " + RESET);
        System.out.println(BLUE + "                                                            " + RESET);
        System.out.println(BLUE + "                                               Code By HChenX      " + RESET);
        sleep(1000);
    }

    public static class FileHelper {
        public static String TAG = "FileHelper";

        public static boolean exists(String path) {
            if (path == null) return false;
            File file = new File(path);
            return file.exists() && !file.isDirectory();
        }

        public static void write(String path, ArrayList<String> list) {
            write(path, list, false);
        }

        public static void write(String path, ArrayList<String> list, boolean append) {
            if (list == null) {
                log(TAG, "write content is null?? are you sure? path: " + path);
                return;
            }
            try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(path, append))) {
                for (String s : list) {
                    writer.write(s + "\n");
                }
            } catch (IOException e) {
                log(TAG, e);
            }
        }

        public static ArrayList<String> read(String path) {
            try (BufferedReader reader = new BufferedReader(
                new FileReader(path))) {
                ArrayList<String> fileContent = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    fileContent.add(line);
                }
                return fileContent;
            } catch (IOException e) {
                log(TAG, e);
                return new ArrayList<>();
            }
        }

        public static boolean empty(String path) {
            return read(path).isEmpty();
        }
    }
}
