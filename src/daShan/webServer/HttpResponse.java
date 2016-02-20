package daShan.webServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author 99darshan （大山）2015272160011
 * 
 * This class receives the HTTPRequest and 
 * echoes the respective content back to the client
 *
 */
public class HttpResponse{
	
	WebServerGui callingGui;
	OutputStream output;
	HttpRequest httpRequest;
	private final int BUFFER_SIZE = 1024;
	private static final String CRLF = "\r\n";
	
 	// constructor
	public HttpResponse(WebServerGui callingGui, OutputStream output){
		this.callingGui = callingGui;
		this.output = output;
	}
	
	public void setHttpRequest(HttpRequest httpRequest) throws IOException{
		this.httpRequest = httpRequest;
		int responseCode = sendStaticResource(); 
		if(responseCode!=0){
			returnErrorMessage(responseCode);
		}
	}
	
	/**
	 * 
	 * @return errorCode
	 * This method finds the location of the file requested by URI
	 * and echoes the content back to the client.
	 * 
	 * if any error occurs, it displays the error message
	 *
	 * 
	 */
	public int sendStaticResource() throws IOException{
		// Set up the standard return code, 0 if processed OK.
		// Any other value means an error has occurred.
		int errorCode = 0;
		FileInputStream fis = null;
		byte[] bytes        = new byte[BUFFER_SIZE];
		
		try{
			// locate the requested file by concatenating the webroot 
			// folder specified in the GUI and the requested URI to form a complete URI
			File requestedfile  = new File(callingGui.getDocRoot(), httpRequest.getURI());
			if (requestedfile.exists()) 
			{		
				// read the selected file
					fis    = new FileInputStream(requestedfile);
					int ch = fis.read(bytes, 0, BUFFER_SIZE);
					while (ch != -1) 
					{
						// output data via outputStream
							output.write(bytes, 0, ch);
							ch = fis.read(bytes, 0, BUFFER_SIZE);
					}
			}
			else{
				// File not found 
				errorCode = 404;
			}
		}
		catch(Exception e){
			// thrown if cannot instantiate a File object
			System.out.println(e.toString() );
		}
		finally{
			if (fis != null){
				fis.close();
			}
		}
		return errorCode;
	} // END of sendStaticResource method
	
	/**
	 * 
	 * @param errorCode
	 * display the error message respective to the errorCode back to the client
	 * @throws IOException 
	 */
	
	public void returnErrorMessage(int errorCode) throws IOException{
		String errorNumber;
		String errorDetail;
		switch (errorCode)
		{
				case 404:
						errorNumber = "HTTP/1.1 404 File Not Found "+CRLF;
						errorDetail = "<h1>WebServer is reporting an error with your request.</h1><h2>Error 404 - File Not Found</h2>";
						break;
				case 501:
						errorNumber = "HTTP/1.1 501 Method Not Supported " +CRLF;
						errorDetail = "<h1>WebServer is reporting an error with your request.</h1><h2>Error 501 - Requested Method is not supported by this HTTP Server</h2>";
						break;
				default:
						errorNumber = "HTTP/1.1 Unknown Error Number " +CRLF;
						errorDetail = "<h1>WebServer is reporting an error with your request.</h1><h2>Sorry, Server has encountered an unexpected error</h2>";
						break;
		}
		String errorMessage = errorNumber +
		"Content-Type: text/html"+ CRLF +
		"Content-Length: " + errorDetail.length() + CRLF +
		CRLF + errorDetail;
		output.write(errorMessage.getBytes());
		
	} // Endof returnErrorMessage method	
} // END of HttpResponse class