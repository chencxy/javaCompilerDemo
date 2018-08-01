package com.cxy.demo.javaCompiler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;

import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.file.RelativePath.RelativeFile;
import com.sun.tools.javac.file.ZipFileIndex;
import com.sun.tools.javac.file.ZipFileIndexArchive;
import com.sun.tools.javac.file.ZipFileIndexCache;
import com.sun.tools.javac.util.Context;


/**
 * 
 * @author cxy
 * 
 * a classloader to load class dynamically from classpath, maybe I can transform it to load from a collation of JarInputStream or jar File, just the same
 * load {@link DynamicCompiler} just have compiled classes as well
 * 
 * provide a collation of {@link JavaFileObject}(actually {@link ZipFileIndexArchive.ZipFileIndexFileObject}) from third part jar(in classpath) for {@link DynamicCompiler} 
 * to use when compilation units depends on other classes
 *
 * thoughts for using {@link ZipFileIndexArchive.ZipFileIndexFileObject}, not {@link ArchiveFileObject} like eclipse :
 * 		{@link JavaCompiler.CompilationTask} throws IllegalArgumentException when any of the given compilation units are of other kind than {@link javax.tools.JavaFileObject.Kind.SOURCE}
 * 
 * Attention from {@link ZipFileIndexArchive} : This is NOT part of any supported API. If you write code that depends on this, you do so at your own risk. 
 * 												This code and its internal interfaces are subject to change or deletion without notice.
 */
public class DynamicClassLoader extends ClassLoader {

	private Map<String, CompiledBinaryJavaFileObject> customCompiledCode = new HashMap<>();
	private Map<String, byte[]> jarCompiledCode = new HashMap<>();
	private Collection<JavaFileObject> thirdPartJavaFileObjects = new ArrayList<>();

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
				            
				            RelativeFile TEST_ENTRY_NAME = new RelativeFile(entry.getName());
							ZipFileIndexCache zfic = ZipFileIndexCache.getSharedInstance();
						    ZipFileIndex zfi = zfic.getZipFileIndex(f, null, false, null, false);
						    Context context = new Context();
						    JavacFileManager fm = new JavacFileManager(context, false, null);
						    ZipFileIndexArchive zfia = new ZipFileIndexArchive(fm, zfi);
						    JavaFileObject jfo =
						            zfia.getFileObject(TEST_ENTRY_NAME.dirname(),
						                                   TEST_ENTRY_NAME.basename());
						    thirdPartJavaFileObjects.add(jfo);
						    
//						    JavaFileObject object = jarEntryToJavaFileObject(f, entry);
//						    if(object != null)
//						    	thirdPartJavaFileObjects.add(object);
				        }
				    }
				    jis.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	private JavaFileObject jarEntryToJavaFileObject(File jarFile, JarEntry entry) {
		try {
			URL url = new URL("jar", null, jarFile.getAbsolutePath().replaceAll("\\\\", "/") + "!/" + entry.getName());
			return new JarEntryJavaFileObject(url, entry.getName());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String convertResourcePathToClassName(final String pName) {
        return pName.replaceAll( ".java$|.class$", "" ).replace( '/', '.' );
    }
	
	public void addCode(CompiledBinaryJavaFileObject cc) {
		customCompiledCode.put(cc.getName(), cc);
	}
	
	public Collection<JavaFileObject> getThirdPartJavaFileObjects() {
		return thirdPartJavaFileObjects;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		byte[] jarCompiledByte = jarCompiledCode.get(name);
		if(jarCompiledByte != null) {
			return defineClass(name, jarCompiledByte, 0, jarCompiledByte.length);
		}
		CompiledBinaryJavaFileObject cc = customCompiledCode.get(name);
		if (cc == null) {
			return super.findClass(name);
		}
		byte[] byteCode = cc.getByteCode();
		return defineClass(name, byteCode, 0, byteCode.length);
	}
}
