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
package com.hchen.processor;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * 注解处理
 *
 * @author 焕晨HChen
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("com.hchen.processor.HookEntrance")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class HookEntranceProcessor extends AbstractProcessor {
    boolean isProcessed = false;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (isProcessed) return true;
        isProcessed = true;
        try (Writer writer = processingEnv.getFiler().createSourceFile("com.hchen.appretention.hook.EntranceMap").openWriter()) {
            writer.write("""
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

                 * Copyright (C) 2023-2024 HChenX
                 */
                package com.hchen.appretention.hook;

                import java.util.HashMap;

                /**
                 * 注解处理器自动生成的 Map 图
                 *
                 * @author 焕晨HChen
                 */
                public class EntranceMap {
                    public String mTargetBrand;
                    public String mTargetPackage;
                    public int mTargetSdk;
                    public float mTargetOS;

                    public EntranceMap(String targetBrand, String targetPackage, int targetSdk, float targetOS){
                        this.mTargetBrand = targetBrand;
                        this.mTargetPackage = targetPackage;
                        this.mTargetSdk = targetSdk;
                        this.mTargetOS = targetOS;
                    }

                    public static HashMap<String, EntranceMap> get() {
                        HashMap<String, EntranceMap> dataMap = new HashMap<>();
                """);

            roundEnv.getElementsAnnotatedWith(HookEntrance.class).forEach(new Consumer<Element>() {
                @Override
                public void accept(Element element) {
                    String fullClassName = null;
                    if (element instanceof TypeElement typeElement) {
                        fullClassName = typeElement.getQualifiedName().toString();
                        if (fullClassName == null) {
                            throw new RuntimeException("E: Full class name is null!!");
                        }
                    }
                    HookEntrance hookEntrance = element.getAnnotation(HookEntrance.class);
                    String targetBrand = hookEntrance.targetBrand();
                    String targetPackage = hookEntrance.targetPackage();
                    int targetSdk = hookEntrance.targetSdk();
                    float targetOS = hookEntrance.targetOS();
                    try {
                        writer.write("        ");
                        writer.write("dataMap.put(\"" + fullClassName + "\", new EntranceMap(\"" + targetBrand + "\", "
                            + "\"" + targetPackage + "\"" + ", " + targetSdk + ", " + targetOS + "f));\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            writer.write("""
                        return dataMap;
                    }

                }
                """);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
