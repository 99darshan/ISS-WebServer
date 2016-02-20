package daShan.webServer;

import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * @author 99darshan （大山）2015272160011
 * 
 * This class parse the incoming message in to constituent
 * elements of the request.
 * 
 *
 */
public class HttpRequest {

	public enum RequestType {
		GET, HEAD, POST
	};

	private static final String CRLF = "\r\n"; // String that holds carriage
												// return and line feed

	private InputStream input;
	private RequestType requestType;
	private String requestString;
	private String requestURI;
	private String requestProtocol;
	private String requestHostName;
	private int errorcode = 0; // errorcode 0 signifies no error in parsing html request
	
	// NOTE : StringBuffer is mutable sequence of characters
	// like Strings but can be modified
	// Similar to StringBuilder but StringBuffer can be synchronized
	StringBuffer requestStringBuffer = new StringBuffer(2048);

	// constructor
	public HttpRequest(InputStream in) {
		input = in;
		parse();
	}
	
	/** This method reads a set of characters from the socket
	 	i.e. extract the HTTP request from the socket 
	 **/
	private void parse() 
	{
		int i;
		byte[] buffer = new byte[2048];

		try 
		{
			// read(byte[] b) method of InputStream
			// Read some number of bytes from InputStream and store them in byte buffer array
			// returns the int number of characters read
			i = input.read(buffer);
		}
		catch (IOException e) 
		{
				e.printStackTrace();
				i = -1;
		}
		
		// append the characters stored in buffer array to String Buffer
		for (int j=0; j<i; j++) 
		{
				requestStringBuffer.append((char) buffer[j]);
		}
		// convert stringBuffer toString and store it in String variable
		requestString = requestStringBuffer.toString();  // TODO change HTTPRequest to requestString
		System.out.println(requestStringBuffer.toString()); //TODO
	}
	public String getRequestString()
	{
		return requestString; // TODO method not required
	}

	
	/**
	 * This method parse the entire HttpRequest
	 * 
	 * a typical http request looks like:
	 * 
	 * GET /index.html HTTP/1.1
	 * Host: localhost
	 * Connection: keep-alive
	 * Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,
	 * 
	 * This method extracts request type, URI and protocol and host name from this requestString
	 * 
	*/
	public void parseRequest() throws IOException {
	
		//System.out.println("read request Line: " + requestString); 
		
		// get index of firstSpace in http request firstline e.g GET /index.html HTTP/1.0
		int firstSpaceIndex = requestString.indexOf(" ");

		// get the substring from index zero and index firstSpaceIndex
		// this subString represents requestType
		if (firstSpaceIndex != -1 && firstSpaceIndex > 0) {
			String typeOfRequest = requestString.substring(0, firstSpaceIndex); 
			
			if (typeOfRequest.startsWith("GET"))
				setRequestType(RequestType.GET);

			else if (typeOfRequest.startsWith("HEAD"))
				setRequestType(RequestType.HEAD);

			else if (typeOfRequest.startsWith("POST"))
				setRequestType(RequestType.POST);

			// Locate second space in request, signifies the end of the
			// requested resource
			// indexOf(String search, int startFrom)
			int secSpaceIndex = requestString.indexOf(" ", firstSpaceIndex + 1);

			if (secSpaceIndex > firstSpaceIndex + 1) {
				// Extract the subString containing Uniform Resource Indentifier
				// (URI)
				setURI(requestString.substring(firstSpaceIndex + 1, secSpaceIndex));

				// look for first CRLF, which is at the end of first line of
				// request after the protocol
				int FirstCRLFIndex = requestString.indexOf(CRLF, secSpaceIndex);

				if (FirstCRLFIndex > secSpaceIndex + 1) {
					// Extract subString containing protocol
					setProtocol(requestString.substring(secSpaceIndex+1, FirstCRLFIndex));
				
					// second CRLF is at the end of second line of request which consists of hostname 
					int SecondCRLFIndex = requestString.indexOf(CRLF, FirstCRLFIndex+1);
					if(SecondCRLFIndex > FirstCRLFIndex+1){
						// Extract subString containing hostName
						setHostName(requestString.substring(FirstCRLFIndex+1, SecondCRLFIndex));
					}
					else {
						errorcode = 4; // Error in extracting hostname and port
					}
					
				}
				else {
					errorcode = 3; // Error in extracting protocol information
				}	
			}
			else {
				errorcode = 2; // Error in extracting requestURI
			}
		}
		else{
			errorcode = 1; // Error in extracting requestType
		}

	} // END of ParseRequest method
	
	public String getErrorMessage()
	{
		String errormessage;
		switch(errorcode)
		{
			case 1:
				errormessage="Parse Error 01 - Could not extract request Method";
				break;
			case 2:
				errormessage="Parse Error 02 - Error extracting URI from Request";
				break;
			case 3:
				errormessage="Parse Error 03 - Error extracting Protocol from Request";
				break;				
			case 4:
				errormessage="Parse Error 04 - Error extracting hostname and port";
				break;
			default:
				errormessage="Parse Error 00 - Unknown Error";
				break;				
		}
		return errormessage;
	}
	
	public int checkForError(){
		return errorcode;
	}

	private void setHostName(String hostName) {
		this.requestHostName = hostName;
		
	}
	
	public String getHostName(){
		return requestHostName;
	}

	private void setProtocol(String reqProtocol) {
		this.requestProtocol = reqProtocol;
	}
	
	public String getProtocol(){
		return requestProtocol;
	}

	private void setURI(String URIstring) {
		this.requestURI = URIstring;

	}
	
	public String getURI() {
		return requestURI;
	}

	private void setRequestType(RequestType requestType) {
		this.requestType = requestType;
	}
	
	public RequestType getRequestType(){
		return requestType;
	}
	
	// TODO checkForError method
	// TODO displayErrorMessage method
}