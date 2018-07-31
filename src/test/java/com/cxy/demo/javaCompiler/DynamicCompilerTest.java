package com.cxy.demo.javaCompiler;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DynamicCompilerTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void compile_classPathResources() throws Exception {
		StringBuffer sourceCode = new StringBuffer();

		sourceCode.append("package com.cxy.demo.test;\n");
		sourceCode.append("import com.cxy.demo.droolsDemo.pojo.SimpleKV;\n");
		sourceCode.append("public class HelloClass {\n");
		sourceCode.append("   public static SimpleKV hello() { return new SimpleKV(\"key\", \"a test value\"); }");
		sourceCode.append("}");

		Class<?> helloClass = DynamicCompiler.newInstance("src/main/resources/libs").compile("com.cxy.demo.test.HelloClass", sourceCode.toString());
		Assert.assertNotNull(helloClass);
		Method method = helloClass.getDeclaredMethod("hello", new Class<?>[] {});
		Object object = method.invoke(null, new Object[] {});
		Assert.assertEquals(object.getClass().getName(), "com.cxy.demo.droolsDemo.pojo.SimpleKV");
		System.out.println(object);
	}
	
	@Test
	public void compile_WhenTypical() throws Exception {
		StringBuffer sourceCode = new StringBuffer();

		sourceCode.append("package com.cxy.demo.test;\n");
		sourceCode.append("public class HelloClass {\n");
		sourceCode.append("   public String hello() { return \"hello\"; }");
		sourceCode.append("}");

		Class<?> helloClass = DynamicCompiler.newInstance().compile("com.cxy.demo.test.HelloClass", sourceCode.toString());
		Assert.assertNotNull(helloClass);
		Assert.assertEquals(1, helloClass.getDeclaredMethods().length);
	}

	@Test
	public void compileAll_WhenTypical() throws Exception {
		String cls1 = "public class A{ public B b() { return new B(); }}";
		String cls2 = "public class B{ public String toString() { return \"B!\"; }}";

		Map<String, Class<?>> compiled = DynamicCompiler.newInstance().addSource("A", cls1).addSource("B", cls2).compileAll();

		Assert.assertNotNull(compiled.get("A"));
		Assert.assertNotNull(compiled.get("B"));

		Class<?> aClass = compiled.get("A");
		Object a = aClass.newInstance();
		Assert.assertEquals("B!", aClass.getMethod("b").invoke(a).toString());
	}

	@Test
	public void compile_WhenSourceContainsInnerClasses() throws Exception {
		StringBuffer sourceCode = new StringBuffer();

		sourceCode.append("package com.cxy.demo.test;\n");
		sourceCode.append("public class HelloClass {\n");
		sourceCode.append("   private static class InnerHelloWorld { int inner; }\n");
		sourceCode.append("   public String hello() { return \"hello\"; }");
		sourceCode.append("}");

		Class<?> helloClass = DynamicCompiler.newInstance().compile("com.cxy.demo.test.HelloClass", sourceCode.toString());
		Assert.assertNotNull(helloClass);
		Assert.assertEquals(1, helloClass.getDeclaredMethods().length);
	}

	@Test
	public void compile_whenError() throws Exception {
		thrown.expect(CompilationException.class);
		thrown.expectMessage("Unable to compile the source");
		StringBuffer sourceCode = new StringBuffer();

		sourceCode.append("package com.cxy.demo.test;\n");
		sourceCode.append("public classHelloClass {\n");
		sourceCode.append("   public String hello() { return \"hello\"; }");
		sourceCode.append("}");
		DynamicCompiler.newInstance().compile("com.cxy.demo.test.HelloClass", sourceCode.toString());
	}

	@Test
	public void compile_WhenFailOnWarnings() throws Exception {
		thrown.expect(CompilationException.class);
		StringBuffer sourceCode = new StringBuffer();

		sourceCode.append("package com.cxy.demo.test;\n");
		sourceCode.append("public class HelloClass {\n");
		sourceCode.append("   public java.util.List<String> hello() { return new java.util.ArrayList(); }");
		sourceCode.append("}");
		DynamicCompiler.newInstance().compile("com.cxy.demo.test.HelloClass", sourceCode.toString());
	}

	@Test
	public void compile_WhenIgnoreWarnings() throws Exception {
		StringBuffer sourceCode = new StringBuffer();

		sourceCode.append("package com.cxy.demo.test;\n");
		sourceCode.append("public class HelloClass {\n");
		sourceCode.append("   public java.util.List<String> hello() { return new java.util.ArrayList(); }");
		sourceCode.append("}");
		Class<?> helloClass = DynamicCompiler.newInstance().ignoreWarnings().compile("com.cxy.demo.test.HelloClass", sourceCode.toString());
		List<?> res = (List<?>) helloClass.getMethod("hello").invoke(helloClass.newInstance());
		Assert.assertEquals(0, res.size());
	}

	@Test
	public void compile_WhenWarningsAndErrors() throws Exception {
		thrown.expect(CompilationException.class);
		StringBuffer sourceCode = new StringBuffer();

		sourceCode.append("package com.cxy.demo.test;\n");
		sourceCode.append("public class HelloClass extends xxx {\n");
		sourceCode.append("   public java.util.List<String> hello() { return new java.util.ArrayList(); }");
		sourceCode.append("}");
		try {
			DynamicCompiler.newInstance().compile("com.cxy.demo.test.HelloClass", sourceCode.toString());
		} catch (Exception e) {
			throw e;
		}
	}
}
