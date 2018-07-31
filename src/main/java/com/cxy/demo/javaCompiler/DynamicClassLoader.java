package com.cxy.demo.javaCompiler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class DynamicClassLoader extends ClassLoader {

	private Map<String, CompiledCode> customCompiledCode = new HashMap<>();
	private Map<String, byte[]> jarCompiledCode = new HashMap<>();

	public DynamicClassLoader(ClassLoader parent) {
		super(parent);
	}
	
	public DynamicClassLoader(ClassLoader parent, String classPath) {
		super(parent);
		if(classPath != null && !"".equals(classPath))
			addClassPathResources(classPath);
	}
	
	private void addClassPathResources(String cp) {
		try {
			File path = new File(cp);
			if(path.isDirectory()){
				for(File f : path.listFiles()){
					if(!f.getName().endsWith(".jar"))
						continue;
					JarInputStream jis = new JarInputStream(new FileInputStream(f));
					JarEntry entry = null;
				    byte[] buf = new byte[1024];
				    int len = 0;
				    while ((entry = jis.getNextJarEntry()) != null) {
				        if (!entry.isDirectory() && !entry.getName().endsWith(".java")) {
				            ByteArrayOutputStream out = new ByteArrayOutputStream();
				            while ((len = jis.read(buf)) >= 0) {
				                out.write(buf, 0, len);
				            }
				            jarCompiledCode.put(convertResourcePathToClassName(entry.getName()), out.toByteArray());
				        }
				    }
				    jis.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String convertResourcePathToClassName(final String pName) {
        return pName.replaceAll( ".java$|.class$", "" ).replace( '/', '.' );
    }
	
	public void addCode(CompiledCode cc) {
		customCompiledCode.put(cc.getName(), cc);
	}
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		byte[] jarCompiledByte = jarCompiledCode.get(name);
		if(jarCompiledByte != null) {
			return defineClass(name, jarCompiledByte, 0, jarCompiledByte.length);
		}
		CompiledCode cc = customCompiledCode.get(name);
		if (cc == null) {
			return super.findClass(name);
		}
		byte[] byteCode = cc.getByteCode();
		return defineClass(name, byteCode, 0, byteCode.length);
	}
}
