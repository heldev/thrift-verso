package io.github.heldev.verso.preprocessor;

import com.twitter.scrooge.frontend.Importer$;
import com.twitter.scrooge.frontend.ThriftParser;
import io.github.heldev.verso.preprocessor.preprocessors.VersoConverterPreprocessor;
import io.github.heldev.verso.preprocessor.preprocessors.VersoEgressPreprocessor;
import io.github.heldev.verso.preprocessor.preprocessors.VersoIngressPreprocessor;
import scala.collection.concurrent.TrieMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AnnotationPreprocessor extends AbstractProcessor {

    private ProcessingEnvironment processingEnvironment;
    private ThriftParser thriftParser;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.processingEnvironment = processingEnv;

        thriftParser = new ThriftParser(Importer$.MODULE$.apply(
                "/Users/hennadii/GitHub/thrift-verso/preprocessor/src/main/resources"),
                true,
                false,
                false,
                new TrieMap<>(),
                Logger.getGlobal());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
        	new VersoConverterPreprocessor(processingEnvironment).findConverters(roundEnv);
            new VersoIngressPreprocessor(processingEnvironment, thriftParser).generateReaders(roundEnv);
            new VersoEgressPreprocessor(processingEnvironment, thriftParser).generateWriters(roundEnv);
//            new VersoServerPreprocessor(processingEnvironment).generateProcessors(roundEnv);
            return true;
        } catch (Exception e) {
            processingEnv
                    .getMessager()
                    .printMessage(Diagnostic.Kind.ERROR, Stream.of(e.getStackTrace()).map(Object::toString).collect(Collectors.joining("\n")));
            return false; //todo remove me
        }
    }



    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {

        return Set.of(VersoIngress.class, VersoEgress.class, VersoServer.class, VersoConverter.class)
                .stream()
                .map(Class::getCanonicalName)
                .collect(Collectors.toUnmodifiableSet());
    }
}
