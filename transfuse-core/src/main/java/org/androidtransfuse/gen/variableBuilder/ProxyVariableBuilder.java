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
package org.androidtransfuse.gen.variableBuilder;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import org.androidtransfuse.gen.InjectionBuilderContext;
import org.androidtransfuse.gen.UniqueVariableNamer;
import org.androidtransfuse.model.InjectionNode;

import javax.inject.Inject;

/**
 * @author John Ericksen
 */
public class ProxyVariableBuilder {

    private final UniqueVariableNamer variableNamer;

    @Inject
    public ProxyVariableBuilder(UniqueVariableNamer variableNamer) {
        this.variableNamer = variableNamer;
    }

    public JExpression buildProxyInstance(InjectionBuilderContext context, InjectionNode injectionNode, JClass proxy) {
        return context.getBlock().decl(proxy, variableNamer.generateName(injectionNode), JExpr._new(proxy));
    }
}
