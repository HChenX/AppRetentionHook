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
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.FutureTask;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 自动生成代码的流程
 * <br><br>请使用 4 空格缩进！
 *
 * @author 焕晨HChen
 */
public class ProcessTasks {
    // 使用示例：
    // COPY TO: [AndroidU]
    // DONE
    private static final String TAG = "[COPY!!]";
    private static final ArrayList<FutureTask<String>> futureTaskList = new ArrayList<>();
    private static final HashMap<String, String> hookFilesHashMap = new HashMap<>();
    private static final HashMap<String, ArrayList<String>> hookCropContentHashMap = new HashMap<>(); // 裁剪处理后的内容
    private static final HashMap<String, ArrayList<String>> hookImportHashMap = new HashMap<>(); // import 列表
    private static final HashMap<CopyMutual, ArrayList<String>> copyContentHashMap = new HashMap<>(); // 需要复制的内容
    private static final HashSet<String> copyToSet = new HashSet<>(); // 需要复制到的 copy 列表

    public static void main(String[] args) {
        ArrayList<String> hookFilePathList = Arrays.stream(strToArray(args[0])).filter(s -> FileHelper.exists(s) && s.contains("\\hook\\"))
            .collect(Collectors.toCollection(ArrayList::new));

        printLogo();
        processContent(hookFilePathList);
        System.exit(0);
    }

    private static void processContent(ArrayList<String> hookFilePathList) {
        for (String hookFilePath : hookFilePathList) {
            FutureTask<String> futureTask = new FutureTask<>(() -> {
                // D:\Android\AppRetentionHook\app\src\main\java\com\hchen\appretention\ProcessTasks.java
                String hookFileName = hookFilePath.substring(hookFilePath.lastIndexOf("\\") + 1, hookFilePath.lastIndexOf("."));
                hookFilesHashMap.put(hookFileName, hookFilePath);

                ArrayList<String> mContent = FileHelper.read(hookFilePath);
                if (shouldSkip(mContent)) return "DONE";

                readCopyContentNew(hookFileName, mContent);

                copyContentHashMap.forEach(new BiConsumer<CopyMutual, ArrayList<String>>() {
                    @Override
                    public void accept(CopyMutual copyMutual, ArrayList<String> strings) {
                        log(strings);
                    }
                });

                hookImportHashMap.put(hookFileName,
                    mContent.stream()
                        .filter(s -> s.contains("import"))
                        .collect(Collectors.toCollection(ArrayList::new)));
                cropContent(hookFileName, mContent);

                return "DONE";
            });
            futureTaskList.add(futureTask);
            new Thread(futureTask).start();
        }
        waitDone();

        regroupImport();
        writeHookContentChange();
    }

    private static boolean shouldSkip(ArrayList<String> content) {
        boolean shouldSkip = true;
        for (String c : content) {
            if (c.contains("copy()")) {
                shouldSkip = false;
                break;
            }
        }
        return shouldSkip;
    }

    private static void readCopyContentNew(String hookFileName, ArrayList<String> content) {
        ArrayList<String> copyContent = new ArrayList<>();
        ArrayList<String> targetFileList = new ArrayList<>();
        boolean start = false;
        for (String c : content) {
            if (c.contains("// DONE") && start) {
                start = false;
                for (String target : targetFileList) {
                    CopyMutual copyMutual = new CopyMutual(hookFileName, target);
                    if (copyContentHashMap.get(copyMutual) == null) {
                        copyContentHashMap.put(copyMutual, new ArrayList<>(copyContent));
                    } else {
                        copyContentHashMap.get(copyMutual).add("\n");
                        copyContentHashMap.get(copyMutual).addAll(new ArrayList<>(copyContent));
                    }
                    copyToSet.add(target);
                }
                copyContent.clear();
                continue;
            }

            if (start)
                copyContent.add(c);

            if (c.contains("// COPY TO")) {
                // COPY TO: [AndroidU, AndroidV]
                targetFileList.addAll(Arrays.asList(
                    c.substring(c.indexOf("[") + 1, c.indexOf("]"))
                        .replace(" ", "")
                        .split(",")));
                log(targetFileList.toString());
                start = true;
            }
        }
    }

    /**
     * @noinspection ExtractMethodRecommender
     */
    private static void cropContent(String hookFileName, ArrayList<String> content) {
        final ArrayList<String> removedImportList = new ArrayList<>();
        boolean inImportBlock = false;
        boolean lastImportDeleted = false;
        for (int i = 0; i < content.size(); i++) {
            String line = content.get(i).trim();

            if (line.startsWith("import")) {
                if (!inImportBlock) inImportBlock = true;
                lastImportDeleted = false;
            } else if (inImportBlock && line.isEmpty()) {
                inImportBlock = false;
                lastImportDeleted = true;
            } else if (lastImportDeleted) {
                removedImportList.add("// THIS IMPORT");
                removedImportList.add("\n" + content.get(i));
                lastImportDeleted = false;
            } else if (inImportBlock) {
                continue;
            } else {
                removedImportList.add(content.get(i));
            }
        }

        final ArrayList<String> processedList = new ArrayList<>();
        boolean inCopyMethod = false;
        for (int i = 0; i < removedImportList.size(); i++) {
            String line = removedImportList.get(i);
            if (line.contains("copy()")) {
                processedList.add(line);
                processedList.add("        // THIS COPY");
                inCopyMethod = true;
            } else if (inCopyMethod && line.equals("    }")) {
                processedList.add(line);
                inCopyMethod = false;
            } else if (inCopyMethod) {
                continue;
            } else {
                processedList.add(line);
            }
        }

        hookCropContentHashMap.put(hookFileName, processedList);
    }

    private static void regroupImport() {
        copyContentHashMap.keySet().forEach(new Consumer<CopyMutual>() {
            @Override
            public void accept(CopyMutual copyMutual) {
                FutureTask<String> futureTask = new FutureTask<>(() -> {
                    ArrayList<String> fromImport = hookImportHashMap.get(copyMutual.copyFrom);
                    ArrayList<String> toImport = hookImportHashMap.get(copyMutual.copyTo);
                    if (fromImport == null || toImport == null) {
                        throwAndExit("List is null! form: " + fromImport + ", to: " + toImport);
                        return "DONE";
                    }
                    for (String fromImportItem : fromImport) {
                        if (!toImport.contains(fromImportItem)) {
                            toImport.add(fromImportItem);
                        }
                    }
                    return "DONE";
                });

                futureTaskList.add(futureTask);
                new Thread(futureTask).start();
            }
        });

        waitDone();
    }

    private static void writeHookContentChange() {
        hookCropContentHashMap.forEach(new BiConsumer<String, ArrayList<String>>() {
            @Override
            public void accept(String file, ArrayList<String> content) {
                FutureTask<String> futureTask = new FutureTask<>(() -> {
                    if (copyToSet.contains(file)) {
                        copyContentHashMap.keySet().stream().filter(copyMutual -> copyMutual.copyTo.equals(file))
                            .collect(Collectors.toCollection(ArrayList::new))
                            .forEach(copyMutual -> {
                                ArrayList<String> cropContent = hookCropContentHashMap.get(copyMutual.copyTo);
                                ArrayList<String> importList = hookImportHashMap.get(copyMutual.copyTo);

                                for (int i = 0; i < cropContent.size(); i++) {
                                    if (cropContent.get(i).contains("// THIS IMPORT")) {
                                        cropContent.addAll(i, importList);
                                        break;
                                    }
                                }

                                for (int i = 0; i < cropContent.size(); i++) {
                                    if (cropContent.get(i).contains("// THIS COPY")) {
                                        cropContent.addAll(i, copyContentHashMap.get(copyMutual));
                                        break;
                                    }
                                }

                                cropContent.removeIf(s -> s.contains("// THIS IMPORT") || s.contains("// THIS COPY"));

                                String path = hookFilesHashMap.get(copyMutual.copyTo);
                                FileHelper.write(path, cropContent);
                                log("成功生成并写入！写入目标文件: " + path);
                            });
                    } else {
                        ArrayList<String> cropContent = hookCropContentHashMap.get(file);
                        ArrayList<String> importList = hookImportHashMap.get(file);

                        for (int i = 0; i < cropContent.size(); i++) {
                            if (cropContent.get(i).contains("// THIS IMPORT")) {
                                cropContent.addAll(i, importList);
                                break;
                            }
                        }

                        cropContent.removeIf(s -> s.contains("// THIS IMPORT") || s.contains("// THIS COPY"));

                        String path = hookFilesHashMap.get(file);
                        FileHelper.write(path, cropContent);
                        log("成功生成并写入！写入目标文件: " + path);
                    }
                    return "DONE";
                });

                futureTaskList.add(futureTask);
                new Thread(futureTask).start();
            }
        });

        waitDone();
    }

    private static void waitDone() {
        while (!futureTaskList.isEmpty()) {
            futureTaskList.removeIf(FutureTask::isDone);
            sleep(50);
        }
    }

    private static String[] strToArray(String str) {
        return str.replace("[", "")
            .replace(" ", "")
            .replace("]", "")
            .split(",");
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void throwAndExit(String msg) {
        new RuntimeException(msg).printStackTrace();
        System.exit(1);
    }

    private static void log(Object msg) {
        log(TAG, msg);
    }

    private static void log(String tag, Object msg) {
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

    private static class FileHelper {
        private static final String TAG = "FileHelper";

        private static boolean exists(String path) {
            if (path == null) return false;
            File file = new File(path);
            return file.exists() && !file.isDirectory();
        }

        private static void write(String path, ArrayList<String> list) {
            write(path, list, false);
        }

        private static void write(String path, ArrayList<String> list, boolean append) {
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

        private static ArrayList<String> read(String path) {
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

        private static boolean empty(String path) {
            return read(path).isEmpty();
        }
    }

    private record CopyMutual(String copyFrom, String copyTo) {
        /**
         * @noinspection NullableProblems
         */
        @Override
        public String toString() {
            return "CopyMutual: copyFrom: " + copyFrom + ", copyTo: " + copyTo;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof CopyMutual copyMutual
                && copyMutual.copyFrom.equals(this.copyFrom)
                && copyMutual.copyTo.equals(this.copyTo);
        }
    }
}
