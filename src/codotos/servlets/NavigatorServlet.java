package codotos.servlets;

import codotos.Constants;
import codotos.navigation.Navigator;
import codotos.context.Context;
import codotos.tags.TagLoader;
import codotos.templates.TemplateBundleManager;
import codotos.config.ConfigManager;

import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;
import java.io.File;


public class NavigatorServlet extends HttpServlet {

	private static final long serialVersionUID = 1;
	
	
	public void init() throws javax.servlet.ServletException {
	
		// todo: initialize a logger?
		// this.getServletConfig().getServletContext().log();
		
	}
	
	
	public void doPost(HttpServletRequest oRequest, HttpServletResponse oResponse) throws javax.servlet.ServletException, java.io.IOException {
	
		this.handle(oRequest, oResponse);
	
	}
	
	
	public void doGet(HttpServletRequest oRequest, HttpServletResponse oResponse) throws javax.servlet.ServletException, java.io.IOException {
	
		this.handle(oRequest, oResponse);
	
	}
	
	
	public void handle(HttpServletRequest oRequest, HttpServletResponse oResponse) throws javax.servlet.ServletException, java.io.IOException {
		
		// TODO - content-type should come from the .pg file?
		oResponse.setContentType("text/html");
		
		PrintWriter oResponseWriter = oResponse.getWriter();
		
		// Generate a class loader
		TagLoader oGeneratedClassLoader = new TagLoader(this.getClass().getClassLoader());
		
		// Create a context object
		Context oContext = new Context();
		oContext.setGeneratedClassLoader(oGeneratedClassLoader);
		oContext.setRequest(oRequest);
		oContext.setResponse(oResponse);		
		
		// Ensure the navigator is loaded and up-to-date
		try{
			
			Navigator.load();
			
			// Use the navigator
			try{
			
				// If we did not find any navigation matches... allow the actual request to go through
				Navigator.navigate(oContext);
				
			}catch(codotos.exceptions.NavigatorRuntimeException e){
				
				javax.servlet.ServletException oException = new javax.servlet.ServletException("Error occured while attempting to navigate");
				oException.initCause(e);
				throw oException;
			
			}
			
		}catch(codotos.exceptions.NavigatorMapInterpreterException e){
			
			javax.servlet.ServletException oException = new javax.servlet.ServletException("Error occured while interpreting the navigator configuration");
			oException.initCause(e);
			throw oException;
		
		}
		
		oResponseWriter.close();
		
    }

}
