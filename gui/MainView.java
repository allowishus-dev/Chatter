/*
                 _/  _/                               _/              
          _/    _/  _/   _/              _/   _/     _/              _/     
           _/  _/  _/  _/ _/  _/    _/      _/      _/_/           _/    
          _/  _/  _/  _/ _/  _/    _/  _/  _/      _/  _/  _/ _/  _/           
         _/  _/  _/  _/ _/  _/    _/  _/  _/      _/  _/  _/ _/  _/       
      _/_/  _/  _/  _/ _/  _/ _/ _/  _/    _/    _/  _/  _/ _/    _/      
    _/ _/  _/  _/  _/ _/  _/ _/ _/  _/      _/  _/  _/  _/ _/      _/       
     _/     _/  _/  _/     _/ _/   _/   _/_/   _/  _/    _/    _/_/
 
 */

package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JScrollBar;
import javax.swing.JTextField;
import javax.swing.JSplitPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import sockets.ClientSockets;
import java.awt.Font;
import java.io.IOException;

public class MainView extends JFrame {

	private static final long serialVersionUID = 1L;
	private JSplitPane contentPane;
	private JTextField textFieldAlias;
	private JTextField textFieldMessage;
	private static JTextArea textAreaChat;
	private JButton buttonConnect;
	private boolean connected = false;
	private boolean enteredChat = false;
	private String sentence;
	private static ClientSockets clientSockets;
	private String alias = "";
	private String address = "127.0.0.1";
	private int port = 664;
	private static GetChat gc;
	private Thread thread;
	private JScrollBar verticalScrollBar;
	private ActionListener connectActionListener;
	private static MainView frame;
	private JTextField textFieldAddress;
	private JTextField textFieldPort;
	private Font consoleFont;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		// run when ready
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frame = new MainView();
					
					// set minimum that the frame can be resized to
					frame.setMinimumSize(new Dimension(650, 300));
					frame.setVisible(true);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	// create the frame
	public MainView() {
		setTitle("Chatter");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 650, 300);
		
		// move focus of text cursor to alias text field
		addWindowListener( new WindowAdapter() {
		    public void windowOpened(WindowEvent e){
		    	textFieldAlias.requestFocus();
		    }
		});		
		
		// when resizing frame then resize columns and rows in the chat text area accordingly
		addComponentListener(new ComponentListener(){
		    @Override
		    public void componentResized(ComponentEvent e) {
		    	int width = frame.getWidth();
		    	int height = frame.getHeight();
		    	
		    	// set columns
		    	if (width >= 650) {		    	 
		    		textAreaChat.setColumns((int) (width / 7.1) - 6);
		    	}
		    	
		    	// set rows
		    	if (height >= 300) {
		    		textAreaChat.setRows((height / 16) - 10);	
		    	}
		    	
		    	// reset split pane divider according to width
	    		if (width < 1140) contentPane.setDividerLocation(100);
		    	else contentPane.setDividerLocation(60);
	    		
	    		// pull vertical scroll bar to the bottom of the chat text area 
	    		verticalScrollBar.setValue(verticalScrollBar.getMaximum());
		    }

			@Override
			public void componentHidden(ComponentEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void componentMoved(ComponentEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void componentShown(ComponentEvent arg0) {
				
			}
		});
		
		// common action listener for connecting 
		connectActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (!enteredChat) {
					// connect
					
					// get address and port from text fields
					address = textFieldAddress.getText();
					port = Integer.parseInt(textFieldPort.getText());
					
					// create an instance to get sockets from
					clientSockets = new ClientSockets();
					
					// try to connect
					connected = clientSockets.connectToServer(address, port);
					
					// if connect succeeds 
					if (connected) {
						textAreaChat.append("Connected to " + address + "\n");
						alias = textFieldAlias.getText();
						
						// send C and alias to enter chat room
						enteredChat = clientSockets.writeToServer("C" + alias);
						
						// if alias is not set, set to guest
						if (alias.isEmpty()) {
							alias ="guest";
							textFieldAlias.setText("guest");
						}
						
						// if enter chat succeeds
						if (enteredChat) {
							textAreaChat.append("Entered chat as " + alias + "\n");
							
							// change to disconnect button
							buttonConnect.setText("Disconnect");
							
							// make message text field editable and the connection text fields not 
							textFieldAddress.setEditable(false);
							textFieldPort.setEditable(false);
							textFieldAlias.setEditable(false);
							textFieldMessage.setEditable(true);
							
							// move focus of text cursor to message text field
							textFieldMessage.requestFocus();
							
							// create instance of handler for chat updates
							gc = new GetChat(clientSockets, textAreaChat, verticalScrollBar);
							
							// start handler in new thread
							new Thread((Runnable) gc).start();
						}
						// enter chat fails
						else textAreaChat.append("Could not enter chat as " + alias + "\n");
					}
					// connection to chat fails
					else textAreaChat.append("Connection to " + address + " failed\n");
				}
				// if connected and in chat room
				else {
					// disconnect
					
					// end update loop
					gc.setConnected(false);
					gc = null;
					textAreaChat.append("Left chat as " + alias + "\n");
					buttonConnect.setText("Connect");
					
					// clear alias text field
					textFieldAlias.setText("");
					
					// make connection text fields editable and message text field not
					textFieldAddress.setEditable(true);
					textFieldPort.setEditable(true);
					textFieldAlias.setEditable(true);
					textFieldMessage.setEditable(false);
					
					// move focus of text cursor to alias text field
					textFieldAlias.requestFocus();
					
					// clear message text field
					textFieldMessage.setText("");										
					
					enteredChat = false;
					
					// tell server to disconnect client
					connected = !clientSockets.writeToServer("D");
				}
			}
		};
		
		// common font for all text. monospaced to show ascii art correctly
		consoleFont = new Font("Monospaced", Font.PLAIN, 11);

		JPanel top = new JPanel();
		JPanel bottom = new JPanel();
		
		contentPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,top,bottom);
		
		// text area to contain input from chat room
		textAreaChat = new JTextArea();
		textAreaChat.setFont(consoleFont);
		textAreaChat.setEditable(false);
		textAreaChat.setRows(10);
		textAreaChat.setColumns(85);
		
		// scroll pane to contain chat text area
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(textAreaChat);
		bottom.add(scrollPane);
		verticalScrollBar = scrollPane.getVerticalScrollBar();
		
		// panel for address label and text field
		JPanel panelAddressContainer = new JPanel();
		top.add(panelAddressContainer);
		panelAddressContainer.setLayout(new BorderLayout(0, 0));
		
		JLabel lblAddress = new JLabel("IP address");
		panelAddressContainer.add(lblAddress, BorderLayout.NORTH);
		
		textFieldAddress = new JTextField(address);
		textFieldAddress.setColumns(10);
		panelAddressContainer.add(textFieldAddress, BorderLayout.SOUTH);
		
		// panel for port label and text field
		JPanel panelPortContainer = new JPanel();
		top.add(panelPortContainer);
		panelPortContainer.setLayout(new BorderLayout(0, 0));
		
		JLabel lblPort = new JLabel("Port");
		panelPortContainer.add(lblPort, BorderLayout.NORTH);
		
		textFieldPort = new JTextField(""+port);
		textFieldPort.setColumns(10);
		panelPortContainer.add(textFieldPort, BorderLayout.SOUTH);		

		// panel for alias label and text field
		JPanel panelAliasContainer = new JPanel();
		panelAliasContainer.setLayout(new BorderLayout(0, 0));
		top.add(panelAliasContainer);
		
		JLabel lblAlias = new JLabel("Alias");
		panelAliasContainer.add(lblAlias, BorderLayout.NORTH);
		
		textFieldAlias = new JTextField();
		textFieldAlias.setFont(consoleFont);
		textFieldAlias.setColumns(25);
		textFieldAlias.addActionListener(connectActionListener);
		panelAliasContainer.add(textFieldAlias, BorderLayout.SOUTH);
		
		buttonConnect = new JButton("Connect");
		top.add(buttonConnect);
		buttonConnect.addActionListener(connectActionListener);
		
		// panel for message label and text field
		JPanel panelMessageContainer = new JPanel();
		panelMessageContainer.setLayout(new BorderLayout(0, 0));
		top.add(panelMessageContainer);
		
		JLabel lblMessage = new JLabel("Message");		
		panelMessageContainer.add(lblMessage, BorderLayout.NORTH);
		
		textFieldMessage = new JTextField();
		textFieldMessage.setFont(consoleFont);
		textFieldMessage.setColumns(85);
		textFieldMessage.setEditable(false);
		
		// when enter or return is pressed in message text field
		textFieldMessage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				sentence = textFieldMessage.getText();
				// if socket is connected and the message isn't empty
				if (connected && !sentence.isEmpty()) {
					// write to socket. add M to tell server that it is a message and to distribute via observer
					clientSockets.writeToServer("M" + sentence);
					
					// clear text message field after sending to socket
					textFieldMessage.setText("");
				}
			}
		});
			
		panelMessageContainer.add(textFieldMessage, BorderLayout.SOUTH);		
		
		// set divider line in split pane
		contentPane.setDividerLocation(100);
		contentPane.setDividerSize(1);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
	}

	public Thread getThread() {
		return thread;
	}

	public void setThread(Thread thread) {
		this.thread = thread;
	}
}

//_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/

// handler for input from chatroom 
class GetChat implements Runnable {
	private ClientSockets cs;
	private JTextArea ta;
	private JScrollBar verticalScrollBar;
	private boolean connected;
	
	// constructor enabling manipulation of the chat text area and vertical scroll bar surrounding it
	public GetChat(ClientSockets cs, JTextArea ta, JScrollBar verticalScrollBar) {
		this.cs = cs;
		this.ta = ta;
		this.verticalScrollBar = verticalScrollBar;
		
		// wouldn't be created if not connected
		setConnected(true);
	}

	@Override
	public void run() {
		try {
			// run loop until disconnected
			while (isConnected()) {
				// look for input from buffered reader in client sockets
				String s = cs.readChat().readLine();
				
				// if input string contains letters
				if (!s.isEmpty()) {
					// ... add them to the chat text area 
					ta.append(s + "\n");
					
					// ... and scroll to the bottom
					verticalScrollBar.setValue(verticalScrollBar.getMaximum());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}
}