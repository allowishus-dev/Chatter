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
	private String adress = "127.0.0.1";
	private int port = 664;
	private static Runnable gc;
	private Thread thread;
	private JScrollBar verticalScrollBar;
	private ActionListener connectActionListener;
	private static MainView frame;
	private JTextField textFieldAdress;
	private JTextField textFieldPort;
	private Font consoleFont;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frame = new MainView();
					frame.setMinimumSize(new Dimension(650, 300));
					frame.setVisible(true);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainView() {
		setTitle("Chatter");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 650, 300);
		
		addWindowListener( new WindowAdapter() {
		    public void windowOpened( WindowEvent e ){
		    	textFieldAlias.requestFocus();
		    }
		});		
		
		addComponentListener(new ComponentListener(){
		    @Override
		    public void componentResized(ComponentEvent e) {
		    	int width = frame.getWidth();
		    	int height = frame.getHeight();
		    	
		    	if (width >= 650) {		    	 
		    		textAreaChat.setColumns((int) (width / 7.1) - 6);
		    	}
		    	
		    	if (height >= 300) {
		    		textAreaChat.setRows((height / 16) - 10);	
		    	}
		    	
	    		if (width < 1140) contentPane.setDividerLocation(100);
		    	else contentPane.setDividerLocation(60);
	    		
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
		
		connectActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (!enteredChat) {
					// connect
					adress = textFieldAdress.getText();
					port = Integer.parseInt(textFieldPort.getText());
					
					clientSockets = new ClientSockets();
					connected = clientSockets.connectToServer(adress, port);
					if (connected) {
						textAreaChat.append("Connected to " + adress + "\n");
						alias = textFieldAlias.getText();
						enteredChat = clientSockets.writeToServer("C" + alias);
						if (alias.isEmpty()) {
							alias ="guest";
							textFieldAlias.setText("guest");
						}
						
						if (enteredChat) {
							textAreaChat.append("Entered chat as " + alias + "\n");
							buttonConnect.setText("Disconnect");
							textFieldAdress.setEditable(false);
							textFieldPort.setEditable(false);
							textFieldAlias.setEditable(false);
							textFieldMessage.setEditable(true);
							textFieldMessage.requestFocus();
							gc = new GetChat(clientSockets, textAreaChat, verticalScrollBar);
							thread = new Thread(gc);
							thread.start();
						}
						else textAreaChat.append("Could not enter chat as " + alias + "\n");
					}
					else textAreaChat.append("Connection to " + adress + " failed\n");
				}
				else {
					// disconnect
					thread.stop();
					gc = null;
					textAreaChat.append("Left chat as " + alias + "\n");
					buttonConnect.setText("Connect");
					textFieldAlias.setText("");
					textFieldAdress.setEditable(true);
					textFieldPort.setEditable(true);
					textFieldAlias.setEditable(true);
					textFieldAlias.requestFocus();
					textFieldMessage.setText("");
					textFieldMessage.setEditable(false);					
					
					enteredChat = false;
					connected = !clientSockets.writeToServer("D");
				}
			}
		};
		
		consoleFont = new Font("Monospaced", Font.PLAIN, 11);

		JPanel top = new JPanel();
		JPanel bottom = new JPanel();
		
		contentPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,top,bottom);
		
		textAreaChat = new JTextArea();
		textAreaChat.setFont(consoleFont);
		textAreaChat.setEditable(false);
		textAreaChat.setRows(10);
		textAreaChat.setColumns(85);
		
		JPanel panelChatContainer = new JPanel();
		bottom.add(panelChatContainer);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(textAreaChat);
		panelChatContainer.add(scrollPane);
		verticalScrollBar = scrollPane.getVerticalScrollBar();
		
		JPanel panelAdressContainer = new JPanel();
		top.add(panelAdressContainer);
		panelAdressContainer.setLayout(new BorderLayout(0, 0));
		
		JLabel lblAdress = new JLabel("IP adress");
		panelAdressContainer.add(lblAdress, BorderLayout.NORTH);
		
		textFieldAdress = new JTextField(adress);
		textFieldAdress.setColumns(10);
		panelAdressContainer.add(textFieldAdress, BorderLayout.SOUTH);
		
		
		JPanel panelPortContainer = new JPanel();
		top.add(panelPortContainer);
		panelPortContainer.setLayout(new BorderLayout(0, 0));
		
		JLabel lblPort = new JLabel("Port");
		panelPortContainer.add(lblPort, BorderLayout.NORTH);
		
		textFieldPort = new JTextField(""+port);
		textFieldPort.setColumns(10);
		panelPortContainer.add(textFieldPort, BorderLayout.SOUTH);		

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
		
		JPanel panelMessageContainer = new JPanel();
		panelMessageContainer.setLayout(new BorderLayout(0, 0));
		top.add(panelMessageContainer);
		
		JLabel lblMessage = new JLabel("Message");		
		panelMessageContainer.add(lblMessage, BorderLayout.NORTH);
		
		textFieldMessage = new JTextField();
		textFieldMessage.setFont(consoleFont);
		textFieldMessage.setColumns(85);
		textFieldMessage.setEditable(false);
		textFieldMessage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				sentence = textFieldMessage.getText();
				if (connected && !sentence.isEmpty()) {
					clientSockets.writeToServer("M" + sentence);
					textFieldMessage.setText("");
				}
			}
		});
			
		panelMessageContainer.add(textFieldMessage, BorderLayout.SOUTH);		
		
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

class GetChat implements Runnable {
	private ClientSockets cs;
	private JTextArea ta;
	private JScrollBar verticalScrollBar;
	
	public GetChat(ClientSockets cs, JTextArea ta, JScrollBar verticalScrollBar) {
		this.cs = cs;
		this.ta = ta;
		this.verticalScrollBar = verticalScrollBar;
	}

	@Override
	public void run() {
		try {
			while (true) {
				String s = cs.readChat().readLine();
				if (!s.isEmpty()) {
					ta.append(s + "\n");
					verticalScrollBar.setValue(verticalScrollBar.getMaximum());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
}
