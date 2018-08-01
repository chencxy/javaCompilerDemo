package com.cxy.demo.javaCompiler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;

public class JarEntryJavaFileObject implements JavaFileObject{
	
	private URL entryUrl;
	private String name;

	public JarEntryJavaFileObject(URL url, String name) throws URISyntaxException {
		this.entryUrl = url;
		this.name = name.substring(name.lastIndexOf("/")+1);
	}
	
	@Override public URI
    toUri() { 
		try {
			return entryUrl.toURI();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} 
		return null;
    }

    @Override public String
    getName() { return name; }

    @Override public InputStream
    openInputStream() throws IOException { 
    	return entryUrl.openStream(); 
    }

    @Override public Kind
    getKind() { 
    	return Kind.CLASS; 
    }

    @Override public OutputStream openOutputStream()                             { throw new UnsupportedOperationException(); }
    @Override public Reader       openReader(boolean ignoreEncodingErrors)       { throw new UnsupportedOperationException(); }
    @Override public CharSequence getCharContent(boolean ignoreEncodingErrors)   { throw new UnsupportedOperationException(); }
    @Override public Writer       openWriter()                                   { throw new UnsupportedOperationException(); }
    @Override public long         getLastModified()                              { throw new UnsupportedOperationException(); }
    @Override public boolean      delete()                                       { throw new UnsupportedOperationException(); }
    @Override public boolean      isNameCompatible(String simpleName, Kind kind) { throw new UnsupportedOperationException(); }
    @Override public NestingKind  getNestingKind()                               { throw new UnsupportedOperationException(); }
    @Override public Modifier     getAccessLevel()                               { throw new UnsupportedOperationException(); }
    
    @Override public String
    toString() { return name; }
}
