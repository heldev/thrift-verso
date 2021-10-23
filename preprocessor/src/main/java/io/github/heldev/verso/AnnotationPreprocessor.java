package io.github.heldev.verso;

import com.twitter.scrooge.frontend.Importer$;
import com.twitter.scrooge.frontend.ThriftParser;
import io.github.heldev.verso.interfaces.VersoConverter;
import io.github.heldev.verso.interfaces.VersoEgress;
import io.github.heldev.verso.interfaces.VersoIngress;
import io.github.heldev.verso.interfaces.VersoServer;
import io.github.heldev.verso.preprocessors.VersoConverterPreprocessor;
import io.github.heldev.verso.preprocessors.VersoEgressPreprocessor;
import io.github.heldev.verso.preprocessors.VersoIngressPreprocessor;
import io.github.heldev.verso.preprocessors.reader.ReaderBuilder;
import io.github.heldev.verso.preprocessors.reader.ReaderRenderer;
import io.github.heldev.verso.stronglytyped.Converters;
import scala.collection.concurrent.TrieMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

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
			//todo process converters once
			Converters converters = new VersoConverterPreprocessor(processingEnvironment).findConverters(roundEnv);

			new ReaderBuilder(converters, processingEnvironment, thriftParser).generateReaders(roundEnv).stream()
					.map(new ReaderRenderer()::render)
					.forEach(file -> {
						try {
							file.writeTo(processingEnvironment.getFiler());
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					});
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

		return Stream.of(VersoIngress.class, VersoEgress.class, VersoServer.class, VersoConverter.class)
				.map(Class::getCanonicalName)
				.collect(toSet());
	}
}
