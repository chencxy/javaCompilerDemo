package com.cxy.demo.javaCompiler;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

/**
 * 
 * @author cxy
 *
 * a {@link JavaFileManager} to manage compiled {@link JavaFileObject} and dependent {@link JavaFileObject}
 */
public class DynamicJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

	private DynamicClassLoader cl;

	protected DynamicJavaFileManager(JavaFileManager fileManager, DynamicClassLoader cl) {
		super(fileManager);
		this.cl = cl;
	}
	
	@Override
	public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String className,
			JavaFileObject.Kind kind, FileObject sibling) throws IOException {

		try {
			CompiledBinaryJavaFileObject innerClass = new CompiledBinaryJavaFileObject(className);
			cl.addCode(innerClass);
			return innerClass;
		} catch (Exception e) {
			throw new RuntimeException("Error while creating in-memory output file for " + className, e);
		}
	}

	public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse)
			throws IOException {
		Iterable<JavaFileObject> iterable = super.list(location, packageName, kinds, recurse);
		Iterator<JavaFileObject> it = iterable.iterator();
		Collection<JavaFileObject> collection = cl.getThirdPartJavaFileObjects();
		while(it.hasNext()) {
			JavaFileObject object = it.next();
			collection.add(object);
		}
		return collection;
	}
}
