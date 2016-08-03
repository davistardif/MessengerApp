import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.ArrayList;

//IMPORTANT FOR USAGE: Start the server before the client to connect properly
public class MessengerApp extends JFrame {
	public static void main (String[] args) {
		MessengerApp app = new MessengerApp(); //Starts the gui and all other processes from there
		app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	//components for gui
	private JTextField inputTextField;
	private JTextArea chatLog;
	private JButton serverMode;
	private JButton clientMode;
	private JButton fullLog;
	
	//network components
	private Socket sock;
	private PrintStream outStream;
	private BufferedReader inStream;
	
	//message storage and constants
	private static ArrayList<String> logList = new ArrayList<String>();
	private static final int CHAT_DISP_AMT = 20;
	private final String CONNECT_TO = "localhost"; //should be hostname or IP of user running as server
	private final int PORT = 400;
	
	//gui setup/init
	public MessengerApp() {
		super("Messenger App"); //title
		setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS)); //vertical layout
		inputTextField = new JTextField(20);
		chatLog = new JTextArea(getChatLog(),25, 50);
		chatLog.setEditable(false);
		add(chatLog);
		add(inputTextField);
		serverMode = new JButton("Start as server");
		clientMode = new JButton("Start as client");
		fullLog = new JButton("View full chat log");
		add(serverMode);
		add(clientMode);
		add(fullLog);
		Handler handler = new Handler(); //triggers when text entry is focus and user hits enter
		ButtonHandler buttonHandler = new ButtonHandler(); //triggers when btns are clicked
		serverMode.addActionListener(buttonHandler);
		clientMode.addActionListener(buttonHandler);
		fullLog.addActionListener(buttonHandler);
		inputTextField.addActionListener(handler);
		this.setSize(800,600);
		setVisible(true);
	}
	//text sending action handler
	private class Handler implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			String outMsg = inputTextField.getText();
			try{
				outStream.println("Them: " + outMsg); //send msg over tcp
			}
			catch (Exception err) {
				System.err.println("Failed to send message\n" + err);
			}
			logList.add(0, "You: " + outMsg); //add message to log
			inputTextField.setText("");
			chatLog.setText(getChatLog());
		}
	}
	
	private class ButtonHandler implements ActionListener{
		public void actionPerformed(ActionEvent e){
			//determine which btn triggered action
			if (e.getSource() == serverMode){
				serverMode.setEnabled(false);
				clientMode.setEnabled(false);
				ServerInit si = new ServerInit();
				si.start();
			}
			else if (e.getSource() == clientMode){
				serverMode.setEnabled(false);
				clientMode.setEnabled(false);
				ClientInit ci = new ClientInit();
				ci.start();
			}
			else if (e.getSource() == fullLog){ //displays full log in a new window
				String fullText = "";
				for (int i = logList.size()- 1; i >= 0; i--)
					fullText += logList.get(i) + "\n";
				JDialog jd = new JDialog();
				jd.setTitle("Full Chat Log");
				JTextArea textBox = new JTextArea();
				textBox.setText(fullText);
				jd.setVisible(true);
				textBox.setEditable(false);
				JScrollPane scroll = new JScrollPane(textBox); //adds scrollbar
				jd.add(scroll);
				jd.setSize(400,600);
			}

		}
	}
	//thread for running as server (to accomodate while-true loop that receives msgs)
	class ServerInit extends Thread{
		public void run() {
			try{
				initServer();
				runServer();
			}
			catch (Exception err) {System.err.println(err);}
		}
		//setup server connection
		private void initServer() throws Exception{
			JOptionPane.showMessageDialog(null, "Starting as server");
			logList.add(0, "Connecting...");
			chatLog.setText(getChatLog());
			ServerSocket srv = new ServerSocket(PORT);
			sock = srv.accept();
			outStream = new PrintStream(sock.getOutputStream());
			inStream = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			logList.add(0, "Connected!");
			chatLog.setText(getChatLog());

		}
		//constantly checks for new msgs in its own thread
		private void runServer() throws Exception {
			while(true)
			{
				String input = inStream.readLine();
				if (input != null){
					logList.add(0, input);
					chatLog.setText(getChatLog());
				}
			}
		}
	}
	//thread for running as client
	class ClientInit extends Thread {
		public void run() {
			try{
				initClient();
				runClient();
			}
			catch (Exception err) {System.err.println(err);}
		}
		//establish connection
		private void initClient() throws Exception{
			JOptionPane.showMessageDialog(null, "Starting as client");
			logList.add(0, "Connecting...");
			chatLog.setText(getChatLog());
			sock = new Socket(CONNECT_TO, PORT);
			outStream = new PrintStream(sock.getOutputStream());
			inStream = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			logList.add(0, "Connected!");
			chatLog.setText(getChatLog());
		}
		//check for new msgs constantly in its own thread
		private void runClient() throws Exception{
			while(true){
				String input = inStream.readLine();
				if (input != null){
					logList.add(0, input);
					chatLog.setText(getChatLog());
				}
			}
		}
	}
	
	//returns most recent messages formatted to display in TextArea
	private static String getChatLog() {
		String log = "Chat Log:\n\n";
		int start;
		if (logList.size() < CHAT_DISP_AMT) start = logList.size() - 1;
		else start = CHAT_DISP_AMT - 1;
		for (int i = start; i >= 0; i--)
			log += logList.get(i) + "\n";
		return log;
	}
}