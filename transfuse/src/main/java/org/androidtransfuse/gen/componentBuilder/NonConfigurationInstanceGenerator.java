package org.androidtransfuse.gen.componentBuilder;

import android.os.Bundle;
import com.sun.codemodel.*;
import org.androidtransfuse.analysis.TransfuseAnalysisException;
import org.androidtransfuse.analysis.adapter.ASTClassFactory;
import org.androidtransfuse.analysis.astAnalyzer.AOPProxyAspect;
import org.androidtransfuse.analysis.astAnalyzer.NonConfigurationAspect;
import org.androidtransfuse.gen.InvocationBuilder;
import org.androidtransfuse.gen.UniqueVariableNamer;
import org.androidtransfuse.gen.variableBuilder.TypedExpressionFactory;
import org.androidtransfuse.model.ComponentDescriptor;
import org.androidtransfuse.model.FieldInjectionPoint;
import org.androidtransfuse.model.InjectionNode;
import org.androidtransfuse.model.TypedExpression;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author John Ericksen
 */
public class NonConfigurationInstanceGenerator implements ExpressionVariableDependentGenerator {

    private ASTClassFactory astClassFactory;
    private UniqueVariableNamer namer;
    private JCodeModel codeModel;
    private InvocationBuilder invocationBuilder;
    private TypedExpressionFactory typeExpressionFactory;

    @Inject
    public NonConfigurationInstanceGenerator(ASTClassFactory astClassFactory, UniqueVariableNamer namer, JCodeModel codeModel, InvocationBuilder invocationBuilder, TypedExpressionFactory typeExpressionFactory) {
        this.astClassFactory = astClassFactory;
        this.namer = namer;
        this.codeModel = codeModel;
        this.invocationBuilder = invocationBuilder;
        this.typeExpressionFactory = typeExpressionFactory;
    }

    @Override
    public void generate(JDefinedClass definedClass, MethodDescriptor methodDescriptor, Map<InjectionNode, TypedExpression> expressionMap, ComponentDescriptor descriptor) {

        try{
            List<InjectionNode> nonConfigurationComponents = new ArrayList<InjectionNode>();
            for (Map.Entry<InjectionNode, TypedExpression> expressionEntry : expressionMap.entrySet()) {

                if(expressionEntry.getKey().containsAspect(NonConfigurationAspect.class)){
                    nonConfigurationComponents.add(expressionEntry.getKey());
                }

            }

            if(!nonConfigurationComponents.isEmpty()){

                //generate holder type
                JDefinedClass nonConfigurationInstance = definedClass._class(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, namer.generateClassName("NonConfigurationInstance"));

                JMethod constructor = nonConfigurationInstance.constructor(JMod.PRIVATE);
                Map<FieldInjectionPoint, JVar> constructorParamMap = new HashMap<FieldInjectionPoint, JVar>();
                Map<FieldInjectionPoint, JFieldVar> fieldMap = new HashMap<FieldInjectionPoint, JFieldVar>();
                for (InjectionNode injectionNode : nonConfigurationComponents) {
                    NonConfigurationAspect aspect = injectionNode.getAspect(NonConfigurationAspect.class);
                    for (FieldInjectionPoint fieldInjectionPoint : aspect.getFields()) {
                        JClass fieldNodeType = codeModel.ref(fieldInjectionPoint.getInjectionNode().getASTType().getName());
                        JVar param = constructor.param(fieldNodeType, namer.generateName(fieldInjectionPoint.getInjectionNode()));
                        constructorParamMap.put(fieldInjectionPoint, param);
                        JFieldVar field = nonConfigurationInstance.field(JMod.PRIVATE, fieldNodeType, namer.generateName(injectionNode));
                        constructor.body().assign(field, param);
                        fieldMap.put(fieldInjectionPoint, field);
                    }
                }
                //add on create init
                //super.getLastNonConfigurationInstance()
                JBlock body = methodDescriptor.getMethod().body();
                JVar bodyVar = body.decl(nonConfigurationInstance, namer.generateName(nonConfigurationInstance), JExpr.cast(nonConfigurationInstance, JExpr.invoke("getLastNonConfigurationInstance")));
                JBlock conditional = body._if(bodyVar.ne(JExpr._null()))._then();

                //assign variables
                for (InjectionNode nonConfigurationComponent : nonConfigurationComponents) {

                    //todo:figure out a better way to do this
                    int proxyLevel = 0;
                    if (nonConfigurationComponent.containsAspect(AOPProxyAspect.class)) {
                        proxyLevel = 1;
                    }

                    NonConfigurationAspect aspect = nonConfigurationComponent.getAspect(NonConfigurationAspect.class);
                    for (FieldInjectionPoint nonConfigurationField : aspect.getFields()) {
                        TypedExpression fieldExpression = typeExpressionFactory.build(nonConfigurationField.getInjectionNode().getASTType(), JExpr.ref(bodyVar, fieldMap.get(nonConfigurationField)));
                        conditional.add(
                                invocationBuilder.buildFieldSet(fieldExpression,
                                        nonConfigurationField,
                                        expressionMap.get(nonConfigurationComponent).getExpression(),
                                        nonConfigurationField.getSubclassLevel() + proxyLevel));
                    }
                }



                methodDescriptor.getTypeMap().get(astClassFactory.buildASTClassType(Bundle.class));

                //add to onRetainNonConfigurationInstance
                JMethod onNonConfigInst = definedClass.method(JMod.PUBLIC, Object.class, "onRetainNonConfigurationInstance");

                JBlock methodBody = onNonConfigInst.body();

                JVar instanceDecl = methodBody.decl(nonConfigurationInstance, namer.generateName(nonConfigurationInstance));

                JInvocation construction = JExpr._new(nonConfigurationInstance);
                methodBody.assign(instanceDecl, construction);

                for (InjectionNode injectionNode : nonConfigurationComponents) {

                    //todo:figure out a better way to do this
                    int proxyLevel = 0;
                    if (injectionNode.containsAspect(AOPProxyAspect.class)) {
                        proxyLevel = 1;
                    }

                    NonConfigurationAspect aspect = injectionNode.getAspect(NonConfigurationAspect.class);
                    for (FieldInjectionPoint fieldInjectionPoint : aspect.getFields()) {
                        construction.arg(invocationBuilder.buildFieldGet(
                                fieldInjectionPoint.getInjectionNode().getASTType(),
                                expressionMap.get(injectionNode).getExpression(),
                                fieldInjectionPoint.getName(),
                                fieldInjectionPoint.getAccessModifier(),
                                fieldInjectionPoint.getSubclassLevel() + proxyLevel));
                    }
                }

                methodBody._return(instanceDecl);


            }
        } catch (JClassAlreadyExistsException e) {
            throw new TransfuseAnalysisException("Class already defined", e);
        } catch (ClassNotFoundException e) {
            throw new TransfuseAnalysisException("Unable to file class", e);
        }
    }
}