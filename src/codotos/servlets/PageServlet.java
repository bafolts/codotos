package codotos.servlets;

import codotos.Constants;
import codotos.context.Context;
import codotos.tags.TagLoader;
import codotos.tags.Tag;
import codotos.config.ConfigManager;
import codotos.pages.PageManager;
import codotos.controllers.SinglePageController;

import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;
import java.io.File;


public class PageServlet extends HttpServlet {

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
		
		SinglePageController oController = new SinglePageController();
			oController.setContext(oContext);
			
		oContext.setController(oController);
		
		try {
		
			// Request the specific Page Object from the page manager
			Tag oPage = PageManager.get(oContext.getRequest().getServletPath().substring(1),oContext);
			
			// Set the context for the page
			oPage.setContext(oContext);
			
			// Execute the Page Object & output data to the user via response object
			oPage.display();
		
			// codotos.exceptions.TagInterpreterException
			// codotos.exceptions.TagCompilerException
			// codotos.exceptions.TagRuntimeException
		
		// Error occured while compiling page and/or tags
		}catch(codotos.exceptions.TagCompilerException e){
		
			javax.servlet.ServletException oException = new javax.servlet.ServletException("Error occured while compiling page and/or tags");
			oException.initCause(e);
			throw oException;
		
		// Error occured while parsing page and/or tags
		}catch(codotos.exceptions.TagInterpreterException e){
		
			javax.servlet.ServletException oException = new javax.servlet.ServletException("Error occured while parsing page and/or tags");
			oException.initCause(e);
			throw oException;
		
		// Error occured while instantiating and/or executing page and/or tags
		}catch(codotos.exceptions.TagRuntimeException e){
		
			javax.servlet.ServletException oException = new javax.servlet.ServletException("Error occured while instantiating and/or executing page and/or tags");
			oException.initCause(e);
			throw oException;
		
		}		
		
		//oResponseWriter.println("Hi.");
		oResponseWriter.close();
		
    }

}
