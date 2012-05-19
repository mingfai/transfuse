package org.androidtransfuse.analysis.adapter;

import javax.lang.model.element.TypeElement;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Element specific AST Type
 *
 * @author John Ericksen
 */
public class ASTElementType extends ASTElementBase implements ASTType {

    private TypeElement typeElement;
    private Collection<ASTMethod> methods;
    private Collection<ASTConstructor> constructors;
    private Collection<ASTField> fields;
    private Collection<ASTType> interfaces;
    private ASTType superClass;

    public ASTElementType(TypeElement typeElement, Collection<ASTConstructor> constructors, Collection<ASTMethod> methods, Collection<ASTField> fields, ASTType superClass, Collection<ASTType> interfaces, Collection<ASTAnnotation> annotations) {
        super(typeElement, annotations);
        this.typeElement = typeElement;
        this.constructors = constructors;
        this.methods = methods;
        this.fields = fields;
        this.superClass = superClass;
        this.interfaces = interfaces;
    }

    @Override
    public String getName() {
        return typeElement.getQualifiedName().toString();
    }


    @Override
    public Collection<ASTMethod> getMethods() {
        return methods;
    }

    @Override
    public Collection<ASTField> getFields() {
        return fields;
    }

    @Override
    public Collection<ASTConstructor> getConstructors() {
        return constructors;
    }

    @Override
    public boolean isConcreteClass() {
        return typeElement.getKind().isClass();
    }

    @Override
    public ASTType getSuperClass() {
        return superClass;
    }

    @Override
    public Collection<ASTType> getInterfaces() {
        return interfaces;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ASTType)) {
            return false;
        }

        ASTType that = (ASTType) o;

        if (typeElement != null ? !getName().equals(that.getName()) : that.getName() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return typeElement != null ? getName().hashCode() : 0;
    }

    @Override
    public List<ASTType> getGenericParameters() {
        return Collections.emptyList();
    }

    @Override
    public boolean inheritsFrom(ASTType type) {
        return inherits(this, type, true, true);
    }

    @Override
    public boolean extendsFrom(ASTType type) {
        return inherits(this, type, false, true);
    }

    @Override
    public boolean implementsFrom(ASTType type) {
        return inherits(this, type, true, false);
    }

    private boolean inherits(ASTType astType, ASTType inheritable, boolean implement, boolean extend) {
        if (astType == null) {
            return false;
        }
        if (astType.equals(inheritable)) {
            return true;
        }
        if (implement) {
            for (ASTType typeInterfaces : astType.getInterfaces()) {
                if (inherits(typeInterfaces, inheritable, implement, extend)) {
                    return true;
                }
            }
        }
        return extend && inherits(astType.getSuperClass(), inheritable, implement, extend);
    }
}