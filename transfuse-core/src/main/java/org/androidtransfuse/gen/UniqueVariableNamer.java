/**
 * Copyright 2013 John Ericksen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.androidtransfuse.gen;

import com.sun.codemodel.JType;
import org.androidtransfuse.adapter.ASTType;
import org.androidtransfuse.model.InjectionNode;

import javax.inject.Singleton;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates a unique name for the given type.
 *
 * @author John Ericksen
 */
@Singleton
public class UniqueVariableNamer {

    private final ConcurrentMap<String, AtomicInteger> nameMap = new ConcurrentHashMap<String, AtomicInteger>();

    public String generateName(Class clazz) {
        return generateName(clazz.getName(), true);
    }

    public String generateName(ASTType astType) {
        return generateName(astType.getName(), true);
    }

    public String generateName(JType definedClass) {
        return generateName(definedClass.fullName(), true);
    }

    public String generateName(InjectionNode injectionNode) {
        return generateName(injectionNode.getClassName(), true);
    }

    public String generateName(String name){
        return generateName(name, true);
    }

    public String generateClassName(Class clazz) {
        return generateName(clazz.getName(), false);
    }

    public String generateClassName(ASTType astType) {
        return generateName(astType.getName(), false);
    }

    public String generateClassName(JType definedClass) {
        return generateName(definedClass.fullName(), false);
    }

    public String generateClassName(InjectionNode injectionNode) {
        return generateName(injectionNode.getClassName(), false);
    }

    public String generateClassName(String name){
        return generateName(name, false);
    }

    private String generateName(String fullClassName, boolean lowerFirst) {

        //remove array notation
        String sanitizedFullClassName = fullClassName.replaceAll("\\[\\]", "");

        String className = sanitizedFullClassName;
        if (fullClassName.contains(".")) {
            className = sanitizedFullClassName.substring(sanitizedFullClassName.lastIndexOf('.') + 1);
        }

        // build class name with the following format:
        // <lower case> + classname + _ + #
        StringBuilder builder = new StringBuilder();

        if(lowerFirst){
            if (!className.isEmpty()) {
                builder.append(Character.toLowerCase(className.charAt(0)));
            }
            if (className.length() > 1) {
                builder.append(className.substring(1));
            }
        }
        else{
            builder.append(className);
        }

        String nameRoot = builder.toString();

        builder.append('$');
        builder.append(nullSafeIterGet(nameRoot));
        return builder.toString();
    }

    private int nullSafeIterGet(String name){
        AtomicInteger result = nameMap.get(name);
        if (result == null) {
            AtomicInteger value = new AtomicInteger();
            result = nameMap.putIfAbsent(name, value);
            if (result == null) {
                result = value;
            }
        }

        return result.getAndIncrement();
    }
}
