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
package org.androidtransfuse.adapter;

/**
 * Abstract Syntax Tree Field node
 *
 * @author John Ericksen
 */
public interface ASTField extends ASTBase {

    /**
     * Supplies the type of the field represented.
     *
     * @return field type
     */
    ASTType getASTType();

    /**
     * Supplies the access modifier for this field
     *
     * @return field access modifier
     */
    ASTAccessModifier getAccessModifier();

    /**
     * Supplies the constant value for this field (if one exists)
     * <p/>
     * IE: static String value = "constant"
     *
     * @return constant field values
     */
    Object getConstantValue();
}
