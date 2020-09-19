package io.github.heldev.verso.preprocessor.preprocessors;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.github.heldev.verso.preprocessor.DatumTreeReader;
import io.github.heldev.verso.preprocessor.GlobalVersoWriter;
import io.github.heldev.verso.preprocessor.VersoServer;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolUtil;
import org.apache.thrift.protocol.TType;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Set;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

public class VersoServerPreprocessor {

    private final ProcessingEnvironment processingEnvironment;
    private final Filer filer;

    public VersoServerPreprocessor(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
        filer = processingEnvironment.getFiler();
    }

    public void generateProcessors(RoundEnvironment roundEnv) {
        for (Element rootElement : roundEnv.getElementsAnnotatedWith(VersoServer.class)) {
            if (rootElement.getKind().isClass()) {
                var clazz = (TypeElement) rootElement;

                String className = clazz.getSimpleName().toString() + "$$VersoProcessor";
                var type = TypeSpec.classBuilder(className)
                        .addSuperinterface(TProcessor.class)
                        .addField(TypeName.get(clazz.asType()), "service", PRIVATE, FINAL)
                        .addMethod(MethodSpec.constructorBuilder().addParameter(TypeName.get(clazz.asType()), "service").addStatement("this.service = service").build())
                        .addMethod(buildProcessMethod())
                        .addMethod(buildReplyInvalidMethod())
                        .addMethod(buildReplyMethod())
                        .build();

                try {
                    String packageName = processingEnvironment.getElementUtils().getPackageOf(clazz).toString();

                    JavaFile.builder(packageName, type)
                            .build()
                            .writeTo(filer);

                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
    }

    private MethodSpec buildProcessMethod() {

        return MethodSpec.methodBuilder("process")
                .addAnnotation(Override.class)
                .addModifiers(PUBLIC)
                .addParameter(TProtocol.class, "in")
                .addParameter(TProtocol.class, "out")
                .addException(TException.class)
                .addStatement("var message = in.readMessageBegin()")
                .addCode("$L", "\n")
                .beginControlFlow("try")
                .beginControlFlow("if ($T.of(\"finBooksByAuthor\", \"findBooksByTitle\").contains(message.name))", Set.class)
                .addStatement("var datumTree = new $T().readStruct(in)", DatumTreeReader.class)
                .addCode("$L", "\n")
                .beginControlFlow("var result = switch (message.name)")
                .addStatement("case \"finBooksByAuthor\" -> service.find((String) datumTree.get((short) 2))")
                .addStatement("case \"findBooksByTitle\" -> service.getBook((String) datumTree.get((short) 2))")
                .addCode("$L", "\n")
                .addStatement("default -> throw new $T(\"Should never happen, probably means there is a bug in the generated code: \" + message.name)", IllegalStateException.class)
                .endControlFlow("")
                .addStatement("reply(message, result, out)")
                .nextControlFlow("else")
                .addStatement("$T.skip(in, $T.STRUCT)", TProtocolUtil.class, TType.class)
                .addStatement("replyInvalidMethod(message, out)")
                .endControlFlow()
                .nextControlFlow("finally")
                .addStatement("in.readMessageEnd()")
                .endControlFlow()
                .build();
    }

    private MethodSpec buildReplyInvalidMethod() {

        var body = "" +
                "out.writeMessageBegin(new $tMessage:T(message.name, $tMessageType:T.EXCEPTION, message.seqid));\n" +
                "new $tApplicationException:T(TApplicationException.UNKNOWN_METHOD, \"Invalid method name: '\" + message.name + \"'\").write(out);\n" +
                "out.writeMessageEnd();\n" +
                "out.getTransport().flush();";

        var bodyParameters = Map.of(
                "tMessage", TMessage.class,
                "tMessageType", TMessageType.class,
                "tApplicationException", TApplicationException.class);

        return MethodSpec.methodBuilder("replyInvalidMethod")
                .addModifiers(PRIVATE)
                .addParameter(TMessage.class, "message")
                .addParameter(TProtocol.class, "out")
                .addException(TException.class)
                .addNamedCode(body, bodyParameters)
                .build();
    }

    private MethodSpec buildReplyMethod() {

        return MethodSpec.methodBuilder("reply")
                .addParameter(TMessage.class, "message")
                .addParameter(Object.class, "result")
                .addParameter(TProtocol.class, "out")
                .addException(TException.class)
                .addStatement("out.writeMessageBegin(new $T(message.name, $T.REPLY, message.seqid))", TMessage.class, TMessageType.class)
                .addStatement("new $T().write(result, out)", GlobalVersoWriter.class)
                .addStatement("out.writeMessageEnd()")
                .addStatement("out.getTransport().flush()")
                .build();
    }

}
