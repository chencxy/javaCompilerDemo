package com.cxy.demo.javaCompiler;

import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.net.URI;

/**
 * 
 * @author cxy
 *
 * a SimpleJavaFileObject of source code to compile
 */
public class StringJavaFileObject extends SimpleJavaFileObject {
	private String contents = null;
	private String className;

	public StringJavaFileObject(String className, String contents) throws Exception {
		super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
		this.contents = contents;
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

	public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
		return contents;
	}
}
