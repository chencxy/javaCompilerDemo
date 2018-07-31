package com.cxy.demo.javaCompiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.file.RelativePath.RelativeFile;
import com.sun.tools.javac.file.ZipFileIndex;
import com.sun.tools.javac.file.ZipFileIndexArchive;
import com.sun.tools.javac.file.ZipFileIndexCache;
import com.sun.tools.javac.util.Context;

public class DynamicJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {

	private Collection<JavaFileObject> thirdPartJavaFileObjects = new ArrayList<>();
	private DynamicClassLoader cl;

	protected DynamicJavaFileManager(JavaFileManager fileManager, DynamicClassLoader cl, String classPath) {
		super(fileManager);
		this.cl = cl;
		if(classPath != null && !"".equals(classPath)) {
			try {
				File path = new File(classPath);
				if(path.isDirectory()){
					for(File f : path.listFiles()){
						if(!f.getName().endsWith(".jar"))
							continue;
						JarInputStream jis = new JarInputStream(new FileInputStream(f));
						JarEntry entry = null;
					    while ((entry = jis.getNextJarEntry()) != null) {
					        if (!entry.isDirectory() && !entry.getName().endsWith(".java")) {
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
					        }
					    }
					    jis.close();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location, String className,
			JavaFileObject.Kind kind, FileObject sibling) throws IOException {

		try {
			CompiledCode innerClass = new CompiledCode(className);
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
		Collection<JavaFileObject> collection = thirdPartJavaFileObjects;
		while(it.hasNext()) {
			JavaFileObject object = it.next();
			collection.add(object);
		}
		return collection;
	}
	
	public String inferBinaryName(Location location, JavaFileObject file) {
        return fileManager.inferBinaryName(location, file);
    }
}
