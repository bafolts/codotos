package codotos.tags;


import codotos.utils.CompilerUtils;
import codotos.Constants;

//import java.io.InputStream;
import java.net.URL;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;

	
public class TagLoader extends ClassLoader {
	
	
	public TagLoader(ClassLoader oParentLoader){
		super(oParentLoader);
		
	}
	
	public final Class findClass(String sFullClassName) throws java.lang.ClassNotFoundException {
		
		try{
			
			if(sFullClassName.startsWith("codotos.tags.generated") || sFullClassName.startsWith("codotos.templates.generated")){
			
				return this.createClass(sFullClassName);
			
			}else{
				
				throw new java.lang.ClassNotFoundException(sFullClassName);
			
			}
		
		} catch (java.lang.Exception ex) {
			
			throw new java.lang.ClassNotFoundException(sFullClassName);
			
		}
		
	}

	protected final Class setClass(String className, byte[] classData) {
		return this.defineClass(className, classData, 0, classData.length);
	}
	
	protected final Class createClass(String sFullClassName,String sFileLocation) throws java.io.FileNotFoundException, java.io.IOException {
	
		FileInputStream fin = new FileInputStream(sFileLocation);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		byte buf[] = new byte[1024];
		
		for(int i = 0; (i = fin.read(buf)) != -1; )
			baos.write(buf, 0, i);
		
		fin.close();
		baos.close();
		byte[] classBytes = baos.toByteArray();
		
		return this.setClass(sFullClassName, classBytes);
	
	}
	
	public final Class createClass(String sFullClassName) throws java.io.FileNotFoundException, java.io.IOException {
		
		return this.createClass(sFullClassName,CompilerUtils.getCompiledClassFileLocation(sFullClassName));
	
	}
	
	public final void refreshClass(String sFullClassName) throws java.io.FileNotFoundException, java.io.IOException {
		
		// TODO -???
		System.out.println("Refresh '"+ sFullClassName +"'");
		this.createClass(sFullClassName);
	
	}
	
	
}