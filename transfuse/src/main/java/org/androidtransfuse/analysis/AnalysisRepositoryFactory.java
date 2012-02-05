package org.androidtransfuse.analysis;

import org.androidtransfuse.analysis.astAnalyzer.AOPProxyAnalyzer;
import org.androidtransfuse.analysis.astAnalyzer.InjectionAnalyzer;
import org.androidtransfuse.analysis.astAnalyzer.MethodCallbackAnalysis;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * @author John Ericksen
 */
public class AnalysisRepositoryFactory {

    private Provider<AOPProxyAnalyzer> aopProxyAnalyzerProvider;
    private Provider<InjectionAnalyzer> injectionAnalyzerProvider;
    private Provider<MethodCallbackAnalysis> methodCallbackAnalysisProvider;

    @Inject
    public AnalysisRepositoryFactory(Provider<AOPProxyAnalyzer> aopProxyAnalyzerProvider, Provider<InjectionAnalyzer> injectionAnalyzerProvider, Provider<MethodCallbackAnalysis> methodCallbackAnalysisProvider) {
        this.aopProxyAnalyzerProvider = aopProxyAnalyzerProvider;
        this.injectionAnalyzerProvider = injectionAnalyzerProvider;
        this.methodCallbackAnalysisProvider = methodCallbackAnalysisProvider;
    }

    public AnalysisRepository buildAnalysisRepository() {
        AnalysisRepository analysisRepository = new AnalysisRepository();

        analysisRepository.addAnalysis(aopProxyAnalyzerProvider.get());
        analysisRepository.addAnalysis(injectionAnalyzerProvider.get());
        analysisRepository.addAnalysis(methodCallbackAnalysisProvider.get());

        return analysisRepository;
    }
}