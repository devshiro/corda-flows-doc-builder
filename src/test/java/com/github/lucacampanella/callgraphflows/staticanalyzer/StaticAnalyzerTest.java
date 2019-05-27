package com.github.lucacampanella.callgraphflows.staticanalyzer;

import com.github.lucacampanella.callgraphflows.staticanalyzer.testclasses.*;
import net.corda.core.flows.StartableByRPC;
import org.junit.Test;
import spoon.Launcher;
import spoon.compiler.SpoonResourceHelper;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.factory.ClassFactory;
import spoon.reflect.factory.Factory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class StaticAnalyzerTest {

    @Test
    public void findCallMethod() throws FileNotFoundException {
        ClassFactory classFactory = getFactory(Arrays.asList(ExtendingSuperclassTest.class)).Class();
        assertTrue(StaticAnalyzer.findCallMethod(classFactory.get(ExtendingSuperclassTest.class)) != null);
    }

//    @Test
//    public void testWhileForEach() throws FileNotFoundException {
//        testClass(WhileForEachTest.class);
//    }
//
//    @Test
//    public void testDoWhile() throws FileNotFoundException {
//        testClass(DoWhileTest.class);
//    }
//
//    @Test
//    public void testNestedIfs() throws FileNotFoundException {
//        testClass(NestedIfsTest.class);
//    }
//
//    @Test
//    public void testFor() throws FileNotFoundException {
//        testClass(ForTest.class);
//    }
//
//    @Test
//    public void testExtendingSuperclass() throws FileNotFoundException {
//        testClass(ExtendingSuperclassTest.class); //todo: we need to get the methods and analyze their body too
//    }
//
//
//    @Test
//    public void subFlowAnalysisTest() throws FileNotFoundException {
//        testClass(SubFlowAnalysisTest.class);
//    }
//
//    @Test
//    public void cordaSubflowsNotMatchingTest() throws FileNotFoundException {
//        testClass(CordaSubflowsNotMatchingTest.class, false);
//    }
//
//    @Test
//    public void simpleFlowTest() throws FileNotFoundException {
//        testClass(SimpleFlowTest.class);
//    }
//
//    @Test
//    public void methodInvocationTest() throws FileNotFoundException {
//        testClass(MethodInvocationTest.class);
//    }
//
//    @Test
//    public void ifFailingTest() throws FileNotFoundException {
//        testClass(IfFailingTest.class, false);
//    }

    @Test
    public void testWhileForEachStartable() throws IOException {
        testAnalyzeStartableByRPCWithClass(WhileForEachTest.class);
    }

    @Test
    public void testDoWhileStartable() throws IOException {
        testAnalyzeStartableByRPCWithClass(DoWhileTest.class);
    }

    @Test
    public void testNestedIfsStartable() throws IOException {
        testAnalyzeStartableByRPCWithClass(NestedIfsTest.class);
    }

    @Test
    public void testForStartable() throws IOException {
        testAnalyzeStartableByRPCWithClass(ForTest.class);
    }

    @Test
    public void testExtendingSuperclassStartable() throws IOException {
        testAnalyzeStartableByRPCWithClass(ExtendingSuperclassTest.class); //todo: we need to get the methods and analyze their body too
    }


    @Test
    public void subFlowAnalysisTestStartable() throws IOException {
        testAnalyzeStartableByRPCWithClass(SubFlowAnalysisTest.class);
    }

    @Test
    public void simpleFlowTestStartable() throws IOException {
        testAnalyzeStartableByRPCWithClass(SimpleFlowTest.class);
    }

    @Test
    public void methodInvocationTestStartable() throws IOException {
        testAnalyzeStartableByRPCWithClass(MethodInvocationTest.class);
    }

    @Test
    public void nestedMethodInvocationTestStartable() throws IOException {
        testAnalyzeStartableByRPCWithClass(NestedMethodInvocationsTest.class);
    }

    @Test
    public void SubFlowInitializationTestStartable() throws IOException {
        testAnalyzeStartableByRPCWithClass(SubFlowInitializationTest.class);
    }

    private void testAnalyzeStartableByRPCWithClass(Class toBeAnalyzed) throws IOException {
        final SourceClassAnalyzer analyzer = new SourceClassAnalyzer(fromClassSrcToPath(toBeAnalyzed));

        final List<CtClass> startableClasses = analyzer.getClassesByAnnotation(StartableByRPC.class);
        for (CtClass clazz : startableClasses) {
            analyzer.drawFromClass(clazz, toBeAnalyzed.getSimpleName() + ".svg");
        }
    }

//    @Test
//    public void subFlowInitiationTest() throws FileNotFoundException {
//        CtClass<?> toBeTestedClass = getFactory(Arrays.asList(SubFlowInitializationTest.class)).Class()
//                .get(SubFlowInitializationTest.class);
//        final Map<CtClass, CtClass> initiatedClassToInitiatingMap =
//                SourceClassAnalyzer.getInitiatedClassToInitiatingMap(toBeTestedClass);
//        System.out.println(initiatedClassToInitiatingMap);
//
//        initiatedClassToInitiatingMap.forEach((initiatedClass, initiatingClass) ->
//                StaticAnalyzer.checkTwoClassesAndBuildGraphs(
//                        initiatingClass, initiatedClass, "example/test/")
//        );
//    }
//
//    private void testClass(Class klass, boolean expectedRes) throws FileNotFoundException {
//        CtClass<?> toBeTestedClass = getFactory(Arrays.asList(klass)).Class().get(klass);
//        final Map<CtClass, CtClass> initiatedClassToInitiatingMap =
//                SourceClassAnalyzer.getInitiatedClassToInitiatingMap(toBeTestedClass);
//        System.out.println(initiatedClassToInitiatingMap);
//
//        initiatedClassToInitiatingMap.forEach((initiatedClass, initiatingClass) ->
//                assertEquals(expectedRes, StaticAnalyzer.checkTwoClassesAndBuildGraphs(
//                        initiatingClass, initiatedClass, "example/test/"))
//        );
//    }
//
//
//    private void testClass(Class klass) throws FileNotFoundException {
//        testClass(klass, true);
//    }

    private Factory getFactory(List<Class> classes) throws FileNotFoundException {
        Launcher spoon = new Launcher();
        Factory factory = spoon.getFactory();
        final List<String> paths = classes.stream().map(StaticAnalyzerTest::fromClassSrcToPath)
                .collect(Collectors.toList());
        spoon.createCompiler(
                factory,
                SpoonResourceHelper.resources(paths.toArray(new String[paths.size()])))
                .build();

        return factory;
    }

    private static String fromClassSrcToPath(Class klass) {
        return "./src/test/java/com/github/lucacampanella/callgraphflows/staticanalyzer/testclasses/"
                + klass.getSimpleName() + ".java";
    }
}