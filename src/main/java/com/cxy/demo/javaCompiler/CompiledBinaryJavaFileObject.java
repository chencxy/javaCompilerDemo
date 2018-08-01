package com.cxy.demo.javaCompiler;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

/**
 * 
 * @author cxy 2018-08-01
 *
 * a SimpleJavaFileObject to store compiled .class file binary data
 * no persistence , put binary data to a  ByteArrayOutputStream instead 
 * provide a getByteCode method for classloader to define a class 
 */
public class CompiledBinaryJavaFileObject extends SimpleJavaFileObject {
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private String className;

    public CompiledBinaryJavaFileObject(String className) throws Exception {
        super(new URI(className), Kind.CLASS);
        this.className = className;
    }
    
    public String getClassName() {
		return className;
	}

    @Override
    public OutputStream openOutputStream() throws IOException {
        return baos;
    }

    public byte[] getByteCode() {
        return baos.toByteArray();
    }
}
