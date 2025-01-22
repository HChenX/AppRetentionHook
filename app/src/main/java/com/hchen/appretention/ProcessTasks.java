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
import java.util.function.Predicate;
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
    private static ArrayList<String> mHookFilePathList = new ArrayList<>();
    private static final ArrayList<String> mHookFileNameList = new ArrayList<>();
    private static final ArrayList<FutureTask<String>> mFutureTaskList = new ArrayList<>();
    private static final HashMap<String, String> mHookFilesHashMap = new HashMap<>(); // 文件名和文件完整路径
    private static final HashMap<String, ArrayList<String>> mHookFileContentHashMap = new HashMap<>(); // 文件名和文件内容
    @Deprecated
    private static final HashMap<String, ArrayList<String>> mHookCropContentHashMap = new HashMap<>(); // 裁剪处理后的内容
    private static final HashMap<String, ArrayList<String>> mHookImportHashMap = new HashMap<>(); // import 列表
    private static final HashMap<CopyMutual, ArrayList<String>> mCopyContentHashMap = new HashMap<>(); // 需要复制的内容
    private static final HashMap<String, ArrayList<String>> mProcessedContentHashMap = new HashMap<>();
    private static final HashSet<String> mCopyToSet = new HashSet<>(); // 需要复制到的 copy 列表

    public static void main(String[] args) {
        mHookFilePathList = Arrays.stream(strToArray(args[0])).filter(s -> FileHelper.exists(s) && s.contains("\\hook\\"))
            .collect(Collectors.toCollection(ArrayList::new));

        printLogo();
        processContent();
        System.exit(0);
    }

    private static void processContent() {
        for (String hookFilePath : mHookFilePathList) {
            FutureTask<String> futureTask = new FutureTask<>(() -> {
                // D:\Android\AppRetentionHook\app\src\main\java\com\hchen\appretention\ProcessTasks.java
                String hookFileName = hookFilePath.substring(hookFilePath.lastIndexOf("\\") + 1, hookFilePath.lastIndexOf("."));
                ArrayList<String> mContent = FileHelper.read(hookFilePath);
                if (shouldSkip(mContent)) return "DONE";

                mHookFilesHashMap.put(hookFileName, hookFilePath);
                mHookFileContentHashMap.put(hookFileName, mContent);
                mHookFileNameList.add(hookFileName);

                readNeedCopyContent(hookFileName, mContent);
                mHookImportHashMap.put(hookFileName,
                    mContent.stream()
                        .filter(s -> s.contains("import"))
                        .collect(Collectors.toCollection(ArrayList::new)));
                // cropContent(hookFileName, mContent);

                return "DONE";
            });
            mFutureTaskList.add(futureTask);
            new Thread(futureTask).start();
        }
        waitDone();

        createProcessedContent();
        writProcessedContentToFile();
        // regroupImport();
        // writeHookContentChange();
    }

    // 是否要跳过当前文件，因为不包含 copy() 方法
    private static boolean shouldSkip(ArrayList<String> content) {
        for (String c : content) {
            if (c.contains("copy()")) {
                return false;
            }
        }
        return true;
    }

    private static void readNeedCopyContent(String hookFileName, ArrayList<String> content) {
        ArrayList<String> copyContent = new ArrayList<>();
        ArrayList<String> targetFileList = new ArrayList<>();
        boolean start = false;
        for (String c : content) {
            if (c.contains("// DONE") && start) {
                start = false;
                for (String target : targetFileList) {
                    if (target.isEmpty()) continue;

                    CopyMutual copyMutual = new CopyMutual(hookFileName, target);
                    if (mCopyContentHashMap.get(copyMutual) == null) {
                        mCopyContentHashMap.put(copyMutual, new ArrayList<>(copyContent));
                        mCopyContentHashMap.get(copyMutual).add("        // DONE\n");
                    } else {
                        mCopyContentHashMap.get(copyMutual).addAll(new ArrayList<>(copyContent));
                        mCopyContentHashMap.get(copyMutual).add("        // DONE");
                    }
                    mCopyToSet.add(target);
                }
                targetFileList.clear();
                copyContent.clear();
                continue;
            }

            if (start) {
                if (copyContent.isEmpty())
                    copyContent.add("        // COPY FROM: " + hookFileName);
                copyContent.add(c);
            }

            if (c.contains("// COPY TO")) {
                // COPY TO: [AndroidU, AndroidV]
                targetFileList.addAll(Arrays.asList(
                    c.substring(c.indexOf("[") + 1, c.indexOf("]"))
                        .replace(" ", "")
                        .split(",")));
                start = true;
            }
        }
    }

    private static void createProcessedContent() {
        for (String hookFileName : mHookFileNameList) {
            FutureTask<String> futureTask = new FutureTask<>(() -> {
                if (mCopyToSet.contains(hookFileName)) {
                    ArrayList<String> processedContent = new ArrayList<>();
                    ArrayList<String> originalContent = mHookFileContentHashMap.get(hookFileName);
                    ArrayList<CopyMutual> mutuals = mCopyContentHashMap.keySet().stream().filter(new Predicate<CopyMutual>() {
                        @Override
                        public boolean test(CopyMutual copyMutual) {
                            return copyMutual.copyTo.equals(hookFileName);
                        }
                    }).collect(Collectors.toCollection(ArrayList::new));

                    ArrayList<String> allImportList = new ArrayList<>();
                    ArrayList<String> allCopyContentList = new ArrayList<>();
                    for (CopyMutual mutual : mutuals) {
                        String copyFrom = mutual.copyFrom;
                        allImportList.addAll(mHookImportHashMap.get(copyFrom));
                        allCopyContentList.addAll(mCopyContentHashMap.get(mutual));
                    }

                    ArrayList<String> processedImportList = new ArrayList<>(mHookImportHashMap.get(hookFileName));
                    for (String all : allImportList) {
                        if (!processedImportList.contains(all)) {
                            processedImportList.add(all);
                        }
                    }

                    int startIndex = -1;
                    int endIndex = -1;

                    for (int i = 0; i < originalContent.size(); i++) {
                        if (startIndex == -1 && originalContent.get(i).contains("import")) {
                            startIndex = i;
                        }

                        if (originalContent.get(i).contains("/**") || originalContent.get(i).contains("@HookEntrance")) {
                            endIndex = i - 2;
                            break;
                        }
                    }

                    if (startIndex != -1 && endIndex != -1) {
                        for (int i = originalContent.size() - 1; i >= 0; i--) {
                            if (i >= startIndex && i <= endIndex)
                                originalContent.remove(i);
                        }
                    }

                    originalContent.addAll(startIndex, processedImportList);
                    processedContent = originalContent;

                    startIndex = -1;
                    endIndex = -1;
                    for (int i = 0; i < processedContent.size(); i++) {
                        if (processedContent.get(i).contains("copy()"))
                            startIndex = i + 1;

                        if (processedContent.get(i).equals("    }") && startIndex != -1) {
                            endIndex = i - 1;
                            break;
                        }
                    }

                    if (startIndex != -1 && endIndex != -1) {
                        for (int i = processedContent.size() - 1; i >= 0; i--) {
                            if (i >= startIndex && i <= endIndex)
                                processedContent.remove(i);
                        }
                    }

                    processedContent.addAll(startIndex, allCopyContentList);
                    mProcessedContentHashMap.put(hookFileName, processedContent);
                }
                return "DONE";
            });
            mFutureTaskList.add(futureTask);
            new Thread(futureTask).start();
        }
        waitDone();
    }

    private static void writProcessedContentToFile() {
        mProcessedContentHashMap.forEach(new BiConsumer<String, ArrayList<String>>() {
            @Override
            public void accept(String fileName, ArrayList<String> processedContent) {
                FutureTask<String> futureTask = new FutureTask<>(() -> {
                    String path = mHookFilesHashMap.get(fileName);
                    FileHelper.write(path, processedContent);
                    log("成功生成并写入！写入目标文件: " + path);
                    return "DONE";
                });
                mFutureTaskList.add(futureTask);
                new Thread(futureTask).start();
            }
        });
        waitDone();
    }

    /**
     * @noinspection ExtractMethodRecommender
     * @deprecated
     */
    @Deprecated
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

        mHookCropContentHashMap.put(hookFileName, processedList);
    }

    @Deprecated
    private static void regroupImport() {
        mCopyContentHashMap.keySet().forEach(new Consumer<CopyMutual>() {
            @Override
            public void accept(CopyMutual copyMutual) {
                FutureTask<String> futureTask = new FutureTask<>(() -> {
                    ArrayList<String> fromImport = mHookImportHashMap.get(copyMutual.copyFrom);
                    ArrayList<String> toImport = mHookImportHashMap.get(copyMutual.copyTo);
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

                mFutureTaskList.add(futureTask);
                new Thread(futureTask).start();
            }
        });

        waitDone();
    }

    @Deprecated
    private static void writeHookContentChange() {
        mHookCropContentHashMap.forEach(new BiConsumer<String, ArrayList<String>>() {
            @Override
            public void accept(String file, ArrayList<String> content) {
                FutureTask<String> futureTask = new FutureTask<>(() -> {
                    if (mCopyToSet.contains(file)) {
                        mCopyContentHashMap.keySet().stream().filter(copyMutual -> copyMutual.copyTo.equals(file))
                            .collect(Collectors.toCollection(ArrayList::new))
                            .forEach(copyMutual -> {
                                ArrayList<String> cropContent = mHookCropContentHashMap.get(copyMutual.copyTo);
                                ArrayList<String> importList = mHookImportHashMap.get(copyMutual.copyTo);

                                for (int i = 0; i < cropContent.size(); i++) {
                                    if (cropContent.get(i).contains("// THIS IMPORT")) {
                                        cropContent.addAll(i, importList);
                                        break;
                                    }
                                }

                                for (int i = 0; i < cropContent.size(); i++) {
                                    if (cropContent.get(i).contains("// THIS COPY")) {
                                        cropContent.addAll(i, mCopyContentHashMap.get(copyMutual));
                                        break;
                                    }
                                }

                                cropContent.removeIf(s -> s.contains("// THIS IMPORT") || s.contains("// THIS COPY"));

                                String path = mHookFilesHashMap.get(copyMutual.copyTo);
                                FileHelper.write(path, cropContent);
                                log("成功生成并写入！写入目标文件: " + path);
                            });
                    } else {
                        ArrayList<String> cropContent = mHookCropContentHashMap.get(file);
                        ArrayList<String> importList = mHookImportHashMap.get(file);

                        for (int i = 0; i < cropContent.size(); i++) {
                            if (cropContent.get(i).contains("// THIS IMPORT")) {
                                cropContent.addAll(i, importList);
                                break;
                            }
                        }

                        cropContent.removeIf(s -> s.contains("// THIS IMPORT") || s.contains("// THIS COPY"));

                        String path = mHookFilesHashMap.get(file);
                        FileHelper.write(path, cropContent);
                        log("成功生成并写入！写入目标文件: " + path);
                    }
                    return "DONE";
                });

                mFutureTaskList.add(futureTask);
                new Thread(futureTask).start();
            }
        });

        waitDone();
    }

    private static void waitDone() {
        while (!mFutureTaskList.isEmpty()) {
            mFutureTaskList.removeIf(FutureTask::isDone);
            sleep(150);
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
