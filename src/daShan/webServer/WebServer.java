package daShan.webServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import daShan.webServer.HttpRequest.RequestType;

/**
 * 
 * @author 99darshan （大山）2015272160011
 * 
 * This class defines the server that listens to specified port for 
 * incoming network connections. The message received are http message
 * which server parse and returns the requested response. 
 * 
 * The server runs on a separate thread so it can be handled from GUI interface. 
 * WebServer has to have its own thread otherwise the GUI would become unresponsive
 *  whilst the server is listening for incoming connections.
 *
 */

public class WebServer implements Runnable{
	
	private int portNum;
	private String docRoot;
	WebServerGui callingGui;
	// we use volatile keyword to prevent variable caching
	// this prevents a new thread from ignoring the changed value of serverStatus
	private volatile boolean serverStatus; 
	
	private ServerSocket webServerSocket;
	
	// constructor
	public WebServer(){
		// set server to be not running on startup
		serverStatus = false;
	}
	
	@Override
	public void run() {
		// continue loop while waiting for request
		while(true){
			Socket webSocket = null;
			InputStream input = null;
			OutputStream output = null;
			try {
				// synchronized() means this section of code is synchronized with 
				// other block of code for thread management purpose
				// synchronized() enables a thread to see the changes made to 
				// shared variable by other threads
				
				// while the server is turned off, enter a wait state
				synchronized(this){
					while(!getStatus()){
						wait();
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// At this point server is on, and has been woken from wait state
			// now start listening for incoming connections
			try {
				// accept() method returns a Socket object
				webSocket = webServerSocket.accept();
				// the accept() method will return a value even though
				// the port is closed, in which case we have no stream to process
				// so we need to check is server is still active
				if(getStatus()){
					// get input and output streams
					// getInputStream method returns the InputStreamObject
					input = webSocket.getInputStream();
					output = webSocket.getOutputStream();
					
					HttpRequest httpRequest = new HttpRequest(input);
					httpRequest.parseRequest();
					// check for errors while parsing httpRequest String
					int parseCode = httpRequest.checkForError();
					if(parseCode > 0){
						callingGui.updateDisplayMsgTextArea("\n "+ httpRequest.getErrorMessage());
					}
					else{
							HttpResponse httpResponse = new HttpResponse(getCallingGui(), output);
							if(httpRequest.getRequestType() == RequestType.GET){
								httpResponse.setHttpRequest(httpRequest);
							}else {
								// At this stage the server only handles GET
								// requests, any otehr request therefore should
								// return a 501 method not Supported error.
								httpResponse.returnErrorMessage(501);
							}
						}
					
					} // END OF getStatus if block
				
				// close the Socket after each request
				webSocket.close();	
			
			} catch (Exception e) {
				// Exception thrown legally if socket has been closed whilst
				// accept() is open
				// i.e. if server has been stopped by the user.
				// if this isn't the case then print the stack trace
				if(!getStatus())
					System.out.println(e.getMessage());
				else
					e.printStackTrace();
				continue;
			}
			
		}
		
	} // end of run mehtod
	
	/* This method is synchronized because variables portNum and docRoot are 
	 * shared with different threads. So to prevent two threads to
	 * use them at once we use synchronized.
	 * This is a public method so that object of WebServer class 
	 * can call it from another java file */
	
	public synchronized void startWebServer(WebServerGui callingGui, int port, String doc){
		// we need to get an WebServerGui object, so that we could display the 
		// status msg to the respective Gui calling this server
		setCallingGui(callingGui);
		setPort(port);
		setDocRoot(doc);
		
		// start server, open SocketServer on specified port
		try {
			webServerSocket = new ServerSocket(portNum, 5, InetAddress.getByName("127.0.0.1"));
		} catch (IOException e) {
			e.printStackTrace();
			//IO exception here is a serious problem so quit the server entirely
			System.exit(1);
		}
		// now change the server status to true and call the run() thread
		// by notifying to wake from wait state
		this.serverStatus = true;
		serverNotify();
	
	} // end of method startWebServer
	
	/** This method notify the System that server has been requested to stop.
	 * This prevents the exception being thrown by accept() method of ServerSocket class
	 * 
	 * This method also close down the ServerSocket
	 * 
	 */
	public void stopServer(){
		
		try {
			serverStatus = false;
			webServerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public boolean getStatus() {
		return serverStatus;
	}
	
	
	private synchronized void serverNotify(){
		 //The notify() method allows the server to wake from wait() phase.
		// it also allows server to be interrupted and stopped
		// If this method were not synchronised then the server
		// would have to respond to one
		// final HTTP request before it would actually stop.
		notify();
	}
	
	/* 
	 * 
	 * method to set the GUI interface for this server, to enable
	 * controlling commands to be issued and status messages to be displayed.
	 * 
	 */
	
	private void setCallingGui(WebServerGui callerUI){
		callingGui = callerUI;
	}
	
	private WebServerGui getCallingGui(){
		return callingGui;		
	}
	
	
	private void setPort(int port){
		portNum = port;
	}
	
	private void setDocRoot(String doc){
		docRoot = doc;
	} 
	
	public String getDocRoot(String doc){
		return docRoot;
	}

} // end of class WebServer