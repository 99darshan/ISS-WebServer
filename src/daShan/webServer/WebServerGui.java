package daShan.webServer;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

/**######
 * 
 * @author 99darshan  （大山）2015272160011
 *  This class provides the Graphical User Interface(GUI) for
 *  the web server
 *
 */
public class WebServerGui extends JFrame {
	
	private static final long serialVersionUID = 1L; 
	
	private String docRoot;
	private String fileToServeLocation;
	private int port;
	private boolean isServerRunning;
	WebServer myWebServer;
	
	
	
	// instance variables for GUI component
	private JPanel upperPanel;
	private JLabel portLabel;
	private JTextField portField;
	private JButton chooseDocButton;
	private JLabel  choosenDocLabel;
	private JFileChooser docFileChooser;
	private JLabel serverStatusLabel;
	private JLabel currentServerStatusLabel;
	private JButton startServerButton;
	private JButton stopServerButton;
	
	private JPanel lowerPanel;
	private JTextArea displayMsgTextArea;
	
	private static WebServerGui myGui;
	
	//main
	public static void main(String[] args) {
		myGui = new WebServerGui();
 	}

	// constructor
	public WebServerGui() {
		
		initComponents();
		// create Thread and start Thread using Thread pools
		myWebServer = new WebServer();
		ExecutorService executor = Executors.newCachedThreadPool();
		executor.execute(myWebServer);	
	} 
		
	
	// this method sets properties and Event Handlers to GUI components
	private void initComponents() {
		upperPanel = new JPanel(new GridLayout(4, 2,10,10));
		portLabel = new JLabel("Port", JLabel.CENTER);
		portLabel.setFont(new Font("Georgia", Font.BOLD, 18));
		portField = new JTextField("80"); // default port is 80
		chooseDocButton = new JButton("Load Document");
		chooseDocButton.setFont(new Font("Georgia", Font.BOLD, 14));
		choosenDocLabel = new JLabel(); // display the path of file choosen from docFilechooser
		docFileChooser = new JFileChooser();
		// set current directory of file chooser using relative path to project root
		docFileChooser.setCurrentDirectory(new File("webRoot")); 
		
		serverStatusLabel = new JLabel("Server Status", JLabel.CENTER);
		serverStatusLabel.setFont(new Font("Georgia", Font.BOLD, 18));
		
		// label to display whether server is running or not
		currentServerStatusLabel = new JLabel("",JLabel.CENTER);
		currentServerStatusLabel.setFont(new Font("Georgia", Font.ITALIC, 18));
		
		startServerButton = new JButton("start server");
		startServerButton.setFont(new Font("Georgia", Font.BOLD, 14));
		stopServerButton = new JButton("stop server");
		stopServerButton.setFont(new Font("Georgia", Font.BOLD, 14));

		lowerPanel = new JPanel(new GridLayout(1, 1));
		displayMsgTextArea = new JTextArea();
		displayMsgTextArea.setEditable(false);
		displayMsgTextArea.setFont(new Font("Georgia",Font.LAYOUT_LEFT_TO_RIGHT, 15));
		
		/**
		 * add Event Handlers to Components
		**/
		
		// display JFileChooser window on click of chooseDocButton
		chooseDocButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				docFileChooser.showDialog(new JFrame(), "choose your webpage");
				// getSelectedFile() returns File object
				// getAbsolutePath is a method of File class
				// returns String with path of the file
				fileToServeLocation = docFileChooser.getSelectedFile().getAbsolutePath();
				// getParentFile is a method of File class that returns the string of path of parent folder
				// of the file shown by getAbsolutePath()
				docRoot = docFileChooser.getSelectedFile().getParentFile().getAbsolutePath();
				choosenDocLabel.setText(fileToServeLocation); 
			}
			
		});
		
		// Event Handler for startserver Button click
		startServerButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				// parseInt to convert string value of number to integer
				port = Integer.parseInt(portField.getText());
				
				// if docRoot directory exists and port number is valid then startServer
				if(isValidPort(port) && doesFileExists()){	
					
						myWebServer.startWebServer(myGui,port,docRoot);
						if(myWebServer.getStatus()){
							updateDisplayMsgTextArea("\n\n Server is Running!! \n "
									+ "Please enter http://localhost:"+port+"/" + docFileChooser.getSelectedFile().getName()+" on browser");
							// disable some swing Components 
							toggleComponents(true);
							
						}else{
							updateDisplayMsgTextArea("error, server cannot start,"
									+ " is the chosen Port already in use?\n");
							toggleComponents(false);
						}
					
				} // end of isValidPort and doesFileExists if  block
				
			} 
			
		});
		
		// Event Handler for stopServer Button Click
		stopServerButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				myWebServer.stopServer();
				if(!myWebServer.getStatus()){
					toggleComponents(false);
					updateDisplayMsgTextArea("\n\nServer Disconnected!! \n "
							+ "Please input port Number and select Root document and start Server Again");
				}
				else
					updateDisplayMsgTextArea("Error in disconnecting server");
				
			}
			
		});
		
		
		// setBorders on lowerPanel and upperPanel
		upperPanel.setBorder(new TitledBorder(new LineBorder(Color.orange), ""));
		lowerPanel.setBorder(new TitledBorder(new LineBorder(Color.blue), "Server Status Messages",2,0, new Font("Georgia",Font.BOLD,20)));
		
		// add components to upperPanel and lowerPanel by order 
		upperPanel.add(portLabel);
		upperPanel.add(portField);
		upperPanel.add(chooseDocButton);
		upperPanel.add(choosenDocLabel);
		upperPanel.add(serverStatusLabel);
		upperPanel.add(currentServerStatusLabel);
		upperPanel.add(startServerButton);
		upperPanel.add(stopServerButton);
		lowerPanel.add(displayMsgTextArea);
		// add a new JScrollPane() to lower panel
		// which accepts display msg text area as parameter and make it scrollable
		lowerPanel.add(new JScrollPane(displayMsgTextArea));
		
		// add Panel to Frame
		this.add(upperPanel);
		this.add(lowerPanel);
		
		// set JFrame properties
		this.setLayout(new GridLayout(2,1,5,5));
		this.setTitle("Da Shan - WebServer ");
		this.setSize(700,500);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);	
	} // end of initComponents
	
	// method to update display status message
	public void updateDisplayMsgTextArea(String msg){
		displayMsgTextArea.append(msg);
	}
	
	public String getDocRoot(){
		return docRoot;
	}
	
	// This method enables or disables some JComponents
	// based on whether server is running or not
	private void toggleComponents(boolean isServerOn){
		isServerRunning = isServerOn;
		if(isServerRunning){
			portField.setEditable(false);
			chooseDocButton.setEnabled(false);
			startServerButton.setEnabled(false);
			stopServerButton.setEnabled(true);
			currentServerStatusLabel.setText("Server Running !!");
			currentServerStatusLabel.setForeground(Color.GREEN);

		}
		else{
			stopServerButton.setEnabled(false);
			portField.setEditable(true);
			chooseDocButton.setEnabled(true);
			startServerButton.setEnabled(true);
			currentServerStatusLabel.setText("Server Disconnected !!");
			currentServerStatusLabel.setForeground(Color.RED);
		}	
			
	} // end of toogleComponents method
	
	private boolean isValidPort(int portNum){
		port = portNum;
		if(port == 80 || port == 8080 || port>1020 && port <65535)  
			return true;
		else 
			updateDisplayMsgTextArea("\n\nPlease Enter valid Port Number!! \n"
					+ "valid ports are: 80, 8080 and ports within range (1024, 65535)");
			portField.requestFocus();
			toggleComponents(false);
			return false;		
	}
	
	private boolean doesFileExists(){
		boolean exists = new File(docRoot).exists();
		if(exists)
			return true;
		else{
			updateDisplayMsgTextArea("\n\n Document Root folder no longer exists, "
					+ "please choose a new one or re-create the folder specified as Document root");
			return false;
		}
			
	}
	
} // end of WebServerGui class