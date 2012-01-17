package org.androidrobotics;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import org.androidrobotics.analysis.*;
import org.androidrobotics.analysis.adapter.ASTMethod;
import org.androidrobotics.analysis.adapter.ASTType;
import org.androidrobotics.annotations.Bind;
import org.androidrobotics.annotations.BindInterceptor;
import org.androidrobotics.annotations.BindProvider;
import org.androidrobotics.annotations.RoboticsModule;
import org.androidrobotics.gen.ActivityGenerator;
import org.androidrobotics.gen.InjectionNodeBuilderRepository;
import org.androidrobotics.gen.VariableBuilderRepositoryFactory;
import org.androidrobotics.gen.variableBuilder.VariableInjectionBuilderFactory;
import org.androidrobotics.model.ActivityDescriptor;
import org.androidrobotics.model.manifest.Manifest;
import org.androidrobotics.model.r.RResource;
import org.androidrobotics.util.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Collection;

/**
 * @author John Ericksen
 */
@Singleton
public class RoboticsProcessor {

    private ActivityGenerator activityGenerator;
    private JCodeModel codeModel;
    private Logger logger;
    private VariableInjectionBuilderFactory variableInjectionBuilderFactory;
    private AnalysisRepositoryFactory analysisRepositoryFactory;
    private InjectionNodeBuilderRepository injectionNodeBuilders;
    private ActivityAnalysis activityAnalysis;
    private AOPRepository aopRepository;
    private Manifest manifest;
    private RResource rResource;

    @Inject
    public RoboticsProcessor(ActivityGenerator activityGenerator,
                             JCodeModel codeModel,
                             Logger logger,
                             VariableBuilderRepositoryFactory variableBuilderRepositoryProvider,
                             VariableInjectionBuilderFactory variableInjectionBuilderFactory,
                             AnalysisRepositoryFactory analysisRepositoryFactory,
                             ActivityAnalysis activityAnalysis,
                             AOPRepositoryProvider aopRepositoryProvider) {
        this.activityGenerator = activityGenerator;
        this.codeModel = codeModel;
        this.logger = logger;
        this.variableInjectionBuilderFactory = variableInjectionBuilderFactory;
        this.analysisRepositoryFactory = analysisRepositoryFactory;
        this.activityAnalysis = activityAnalysis;

        aopRepository = aopRepositoryProvider.get();
        injectionNodeBuilders = variableBuilderRepositoryProvider.buildRepository();
    }

    public void processModuleElements(Collection<? extends ASTType> astTypes) {

        for (ASTType astType : astTypes) {
            if (astType.isAnnotated(RoboticsModule.class)) {

                for (ASTMethod astMethod : astType.getMethods()) {
                    if (astMethod.isAnnotated(Bind.class)) {
                        ASTType superType = astMethod.getReturnType();

                        if (astMethod.getParameters().size() == 1) {
                            ASTType implType = astMethod.getParameters().get(0).getASTType();

                            injectionNodeBuilders.put(superType.getName(),
                                    variableInjectionBuilderFactory.buildVariableInjectionNodeBuilder(implType));
                        } else {
                            //todo: throw exception
                        }
                    }

                    if (astMethod.isAnnotated(BindInterceptor.class)) {
                        ASTType interceptor = astMethod.getReturnType();

                        if (astMethod.getParameters().size() == 1) {
                            ASTType annotationType = astMethod.getParameters().get(0).getASTType();

                            aopRepository.put(annotationType, interceptor);
                        } else {
                            //todo: throw exception
                        }
                    }

                    if (astMethod.isAnnotated(BindProvider.class)) {
                        ASTType providedType = astMethod.getReturnType();

                        if (astMethod.getParameters().size() == 1) {
                            ASTType annotationType = astMethod.getParameters().get(0).getASTType();

                            injectionNodeBuilders.put(providedType.getName(),
                                    variableInjectionBuilderFactory.buildProviderInjectionNodeBuilder(annotationType));

                            aopRepository.put(annotationType, providedType);
                        } else {
                            //todo: throw exception
                        }
                    }
                }
            }
        }
    }

    public void processRootElement(Collection<? extends ASTType> astTypes) {

        AnalysisRepository analysisRepository = analysisRepositoryFactory.buildAnalysisRepository();

        for (ASTType astType : astTypes) {

            ActivityDescriptor activityDescriptor = activityAnalysis.analyzeElement(astType, analysisRepository, injectionNodeBuilders, aopRepository);

            if (activityDescriptor != null) {
                try {
                    activityGenerator.generate(activityDescriptor, rResource);
                } catch (IOException e) {
                    logger.error("IOException while generating activity", e);
                } catch (JClassAlreadyExistsException e) {
                    logger.error("JClassAlreadyExistsException while generating activity", e);
                } catch (ClassNotFoundException e) {
                    logger.error("ClassNotFoundException while generating activity", e);
                }
            }
        }
    }

    public void writeSource(CodeWriter codeWriter, CodeWriter resourceWriter) {

        try {
            codeModel.build(
                    codeWriter,
                    resourceWriter);

        } catch (IOException e) {
            logger.error("Error while writing source files", e);
        }
    }

    public void processManifest(Manifest manifest) {
        this.manifest = manifest;
    }

    public void processR(RResource rResource) {
        this.rResource = rResource;
    }

    public Manifest getManifest() {
        return manifest;
    }
}
