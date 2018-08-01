package com.cxy.demo.javaCompiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

/**
 * 
 * @author cxy
 *
 * dynamic compile java source file and define a Class<?>
 */
public class DynamicCompiler {
	private JavaCompiler javac;
	private Iterable<String> options;
	boolean ignoreWarnings = false;
	private String classPath;

	private Map<String, StringJavaFileObject> sourceCodes = new HashMap<String, StringJavaFileObject>();

	public static DynamicCompiler newInstance() {
		return new DynamicCompiler();
	}
	
	public static DynamicCompiler newInstance(String classPath) {
		return new DynamicCompiler(classPath);
	}

	private DynamicCompiler() {
		this.javac = ToolProvider.getSystemJavaCompiler();
	}
	
	private DynamicCompiler(String classPath) {
		this.javac = ToolProvider.getSystemJavaCompiler();
		this.classPath = classPath;
	}

	public DynamicCompiler useOptions(String... options) {
		this.options = Arrays.asList(options);
		return this;
	}

	public DynamicCompiler ignoreWarnings() {
		ignoreWarnings = true;
		return this;
	}

	public Map<String, Class<?>> compileAll() throws Exception {
		if (sourceCodes.size() == 0) {
			throw new RuntimeException("No source code to compile");
		}
		Collection<SimpleJavaFileObject> compilationUnits = new ArrayList<SimpleJavaFileObject>(sourceCodes.values());
		
		DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
		DynamicClassLoader classLoader = new DynamicClassLoader(getClass().getClassLoader(), classPath);
		DynamicJavaFileManager fileManager = new DynamicJavaFileManager(javac.getStandardFileManager(null, null, null), classLoader);
		JavaCompiler.CompilationTask task = javac.getTask(null, fileManager, collector, options, null, compilationUnits);
		boolean result = task.call();
		if (!result || collector.getDiagnostics().size() > 0) {
			StringBuffer exceptionMsg = new StringBuffer();
			exceptionMsg.append("Unable to compile the source");
			boolean hasWarnings = false;
			boolean hasErrors = false;
			for (Diagnostic<? extends JavaFileObject> d : collector.getDiagnostics()) {
				switch (d.getKind()) {
				case NOTE:
				case MANDATORY_WARNING:
				case WARNING:
					hasWarnings = true;
					break;
				case OTHER:
				case ERROR:
				default:
					hasErrors = true;
					break;
				}
				exceptionMsg.append("\n").append("[kind=").append(d.getKind());
				exceptionMsg.append(", ").append("line=").append(d.getLineNumber());
				exceptionMsg.append(", ").append("message=").append(d.getMessage(Locale.US)).append("]");
			}
			if (hasWarnings && !ignoreWarnings || hasErrors) {
				throw new CompilationException(exceptionMsg.toString());
			}
		}

		Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
		for (String className : sourceCodes.keySet()) {
			classes.put(className, classLoader.loadClass(className));
		}
		return classes;
	}

	/**
	 * Compile single source
	 *
	 * @param className
	 * @param sourceCode
	 * @return
	 * @throws Exception
	 */
	public Class<?> compile(String className, String sourceCode) throws Exception {
		return addSource(className, sourceCode).compileAll().get(className);
	}

	/**
	 * Add source code to the compiler
	 *
	 * @param className
	 * @param sourceCode
	 * @return
	 * @throws Exception
	 * @see {@link #compileAll()}
	 */
	public DynamicCompiler addSource(String className, String sourceCode) throws Exception {
		sourceCodes.put(className, new StringJavaFileObject(className, sourceCode));
		return this;
	}
}
