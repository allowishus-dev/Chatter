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


package sockets;

import java.io.*; 
import java.net.*; 
import java.util.ArrayList;

public class ChatServer {
	// array list to store clients connected to the chat room
	private ArrayList<Observer> observers = new ArrayList<Observer>();
	private static String clientSentence;
	private ServerSocket welcomeSocket;
	
	public ChatServer() throws Exception {

		int threadNumber = 0;
	
		// create instance of the server socket at port 664 
		welcomeSocket = new ServerSocket(664);
		  
	    while(true) {
	    	System.out.println("Waiting for external socket with request ...\n\n");

	    	// the server socket listens after incoming clients. when one connects it is transfered to a new socket
			Socket connectionSocket = welcomeSocket.accept(); 
			System.out.println("Found client");

			// a new SocketThread is created as a Runnable. the newly connected socket, this instance of ChatServer and the thread number is sent to the constructor of SocketThread 
			Runnable socketThread = new SocketThread(connectionSocket, this, threadNumber);

			// the SocketThread is started in a new thread
			new Thread(socketThread).start();
			
			System.out.println("Creating thread (" + threadNumber + ") for " + connectionSocket.getInetAddress() + 
					"\nActive thread count: " + Thread.activeCount() + "\n");
			// the new thread is counted
			threadNumber++;
	
	    } 
	}

	public static void main(String args[]) throws Exception { 
		new ChatServer();
	   	
    }

	// method for the observer pattern
	public void register(Observer o) {
		observers.add(o);		
	}

	public void remove(Observer o) {
		observers.remove(o);		
	}

	public void notifyObservers() {
		// runs through the list of connected clients and runs an update in each one
		for (Observer obs : observers) {
			obs.update(clientSentence);
		}		
	}
	
	public void recievedMessage(String clientSentence) {
		// update the internal sentence holder
		ChatServer.clientSentence = clientSentence;
		// call to update all connected clients
		notifyObservers();
	}
}

//_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/

// can instantiated as either an observer and a runnable as it needs to be run in a thread and added to the observable ChatServer
class SocketThread implements Observer, Runnable {
	private static int threadNumber;
	private static ChatServer chatServer;
	private Socket connectionSocket;
	private DataOutputStream  outToClient;	
	private boolean connected = false;

	public SocketThread(Socket connectionSocket, ChatServer chatServer, int threadNumber) throws IOException {
		this.connectionSocket = connectionSocket;
		SocketThread.setThreadNumber(threadNumber);
		SocketThread.chatServer = chatServer;
		outToClient = new DataOutputStream(connectionSocket.getOutputStream());
		
	}

	@Override
	public void run() {
		
		try {
			// a new InputHandler is created as a Runnable. this SocketThread and the ChatServer sent in, is sent to the constructor of InputHandler
			Runnable ih = new InputHandler(chatServer, this);
			
			// the InputHandler is started in a new thread
			new Thread(ih).start();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
	
	public Socket getSocket() {
		return connectionSocket;
	}

	public void setSocket(Socket connectionSocket) {
		this.connectionSocket = connectionSocket;
	}

	// part of the observer pattern. update is called in all connected SocketThreads when a new message is received from a client 
	@Override
	public void update(String sentence) {
	
		try {
			// write the message to the socket connected to the client
			outToClient.writeBytes(sentence + "\r\n");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public static int getThreadNumber() {
		return threadNumber;
	}

	public static void setThreadNumber(int threadNumber) {
		SocketThread.threadNumber = threadNumber;
	}
}
   
//_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/

class InputHandler implements Runnable {
	private BufferedReader inFromClient;
	private String clientSentence;
	private static ChatServer chatServer;
	private SocketThread socketThread;
	private String clientAdress;
	private boolean connected;
	private int threadNumber;
	private int face;
	private String alias = "guest";
	private String[] faces = {"(._.)","('-')","(0_0)","(o_o)","(O_O)","(-_-)",
							  "('~')","(*_*)","(ø_ø)","(#_#)","(Ø_Ø)","(+_+)",
							  "(`_´)","[#_#]","[*_*]","[-_-]","{._.}","{'_'}",
							  "/(O,O)\\"};


	public InputHandler(ChatServer chatServer, SocketThread socketThread) throws IOException {
		InputHandler.chatServer = chatServer;
		this.socketThread = socketThread;
		threadNumber = SocketThread.getThreadNumber();
		clientAdress = socketThread.getSocket().getInetAddress().toString().substring(1);
		inFromClient = new BufferedReader(
			new InputStreamReader(socketThread.getSocket().getInputStream()));
		setConnected(true);

		// when the number of face is set by threadNumber modulus the amount of faces, the list just recycles when a threadNumber is higher than amount of faces 
		face = (int) threadNumber % faces.length;
	}

	@Override
	public void run() {
		try {
	
			while (isConnected()) {

				// server console status
				System.out.println("Reading incoming data ...");

				// listening to input
				clientSentence = inFromClient.readLine();
				
				// server console status
				System.out.println("Recieved : " + clientSentence);
				
				// if received message starts with a C it means that the connected client want to enter the chat room
				if (clientSentence.startsWith("C") && !socketThread.isConnected()) {
					
					// add this thread as an observer to be updated when a client send a new message to the chat server
					chatServer.register((Observer) socketThread);
					
					// cut off the C character, to have the client alias left
					String s = clientSentence.substring(1);
					
					// if no client alias is added this will be skipped and alias will be guest 
					if (!s.equals("")) {
						alias = s; 
					}
					
					// server console status
					System.out.println("SERVER: " + alias + "@" + clientAdress + " just entered the chat");
					
					// this call does an update to all connected clients and sends this string to their sockets
					// tells other connected clients that this one has connected
					chatServer.recievedMessage("FROM SERVER: " + alias + "@" + clientAdress + " just entered the chat");
					socketThread.setConnected(true);
					
				}
				// if the received message starts with a M it means that the connected client has send a message to the chat room 
				else if (clientSentence.startsWith("M") && socketThread.isConnected()) {
					
					// cut off the M character, to have the message left
					clientSentence = clientSentence.substring(1);
					
					// easter egg feature that prints ASCII art of a troll meme 
					if (clientSentence.startsWith("troll")) clientSentence += "\n" + troll();
					
					// this call does an update to all connected clients and sends this string to their sockets
					// a avatar face is add to the message
					chatServer.recievedMessage("[" + alias + "@" + clientAdress + "]  " + faces[face] + "  ~( " + clientSentence + " )");
					
					
				}
				// if the received message starts with a D it means that the connected client wants to disconnect
				else if (clientSentence.startsWith("D") && socketThread.isConnected()) {
					// server console status
					System.out.println("Threads before disconnect : " + Thread.activeCount());
					System.out.println("SERVER: " +  alias + "@" + clientAdress + " just left the chat");

					// tells other connected clients that this one has disconnected
					chatServer.recievedMessage("FROM SERVER: " +  alias + "@" + clientAdress + " just left the chat");
					
					// killing input handler
					System.out.println("killing input handler");
					setConnected(false);
					
					// stop getting messages from other clients in chat room
					System.out.println("stop getting messages from others in chat");
					chatServer.remove((Observer) socketThread);
					
					// closing client socket
					System.out.println("closing client socket");
					socketThread.getSocket().close();
					socketThread.setSocket(null);
					socketThread.setConnected(false);
					
					System.out.println("Threads after disconnect : " + Thread.activeCount());
				}				
			}
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// ASCII art for easter egg
	private String troll() {
		return "                                   .....'',;;::cccllllllllllllcccc:::;;,,,''...'',,'..\n                            ..';cldkO00KXNNNNXXXKK000OOkkkkkxxxxxddoooddddddxxxxkkkkOO0XXKx:.\n                      .':ok0KXXXNXK0kxolc:;;,,,,,,,,,,,;;,,,''''''',,''..              .'lOXKd'\n                 .,lx00Oxl:,'............''''''...................    ...,;;'.             .oKXd.\n              .ckKKkc'...'',:::;,'.........'',;;::::;,'..........'',;;;,'.. .';;'.           'kNKc.\n           .:kXXk:.    ..       ..................          .............,:c:'...;:'.         .dNNx.\n          :0NKd,          .....''',,,,''..               ',...........',,,'',,::,...,,.        .dNNx.\n         .xXd.         .:;'..         ..,'             .;,.               ...,,'';;'. ...       .oNNo\n         .0K.         .;.              ;'              ';                      .'...'.           .oXX:\n        .oNO.         .                 ,.              .     ..',::ccc:;,..     ..                lXX:\n       .dNX:               ......       ;.                'cxOKK0OXWWWWWWWNX0kc.                    :KXd.\n     .l0N0;             ;d0KKKKKXK0ko:...              .l0X0xc,...lXWWWWWWWWKO0Kx'                   ,ONKo.\n   .lKNKl...'......'. .dXWN0kkk0NWWWWWN0o.            :KN0;.  .,cokXWWNNNNWNKkxONK: .,:c:.      .';;;;:lk0XXx;\n  :KN0l';ll:'.         .,:lodxxkO00KXNWWWX000k.       oXNx;:okKX0kdl:::;'',;coxkkd, ...'. ...'''.......',:lxKO:.\n oNNk,;c,'',.                      ...;xNNOc,.         ,d0X0xc,.     .dOd,           ..;dOKXK00000Ox:.   ..''dKO,\n'KW0,:,.,:..,oxkkkdl;'.                'KK'              ..           .dXX0o:'....,:oOXNN0d;.'. ..,lOKd.   .. ;KXl.\n;XNd,;  ;. l00kxoooxKXKx:..ld:         ;KK'                             .:dkO000000Okxl;.   c0;      :KK;   .  ;XXc\n'XXdc.  :. ..    '' 'kNNNKKKk,      .,dKNO.                                   ....       .'c0NO'      :X0.  ,.  xN0.\n.kNOc'  ,.      .00. ..''...      .l0X0d;.             'dOkxo;...                    .;okKXK0KNXx;.   .0X:  ,.  lNX'\n ,KKdl  .c,    .dNK,            .;xXWKc.                .;:coOXO,,'.......       .,lx0XXOo;...oNWNXKk:.'KX;  '   dNX.\n  :XXkc'....  .dNWXl        .';l0NXNKl.          ,lxkkkxo' .cK0.          ..;lx0XNX0xc.     ,0Nx'.','.kXo  .,  ,KNx.\n   cXXd,,;:, .oXWNNKo'    .'..  .'.'dKk;        .cooollox;.xXXl     ..,cdOKXXX00NXc.      'oKWK'     ;k:  .l. ,0Nk.\n    cXNx.  . ,KWX0NNNXOl'.           .o0Ooldk;            .:c;.':lxOKKK0xo:,.. ;XX:   .,lOXWWXd.      . .':,.lKXd.\n     lXNo    cXWWWXooNWNXKko;'..       .lk0x;       ...,:ldk0KXNNOo:,..       ,OWNOxO0KXXNWNO,        ....'l0Xk,\n     .dNK.   oNWWNo.cXK;;oOXNNXK0kxdolllllooooddxk00KKKK0kdoc:c0No        .'ckXWWWNXkc,;kNKl.          .,kXXk,\n      'KXc  .dNWWX;.xNk.  .kNO::lodxkOXWN0OkxdlcxNKl,..        oN0'..,:ox0XNWWNNWXo.  ,ONO'           .o0Xk;\n      .ONo    oNWWN0xXWK, .oNKc       .ONx.      ;X0.          .:XNKKNNWWWWNKkl;kNk. .cKXo.           .ON0;\n      .xNd   cNWWWWWWWWKOkKNXxl:,'...;0Xo'.....'lXK;...',:lxk0KNWWWWNNKOd:..   lXKclON0:            .xNk.\n      .dXd   ;XWWWWWWWWWWWWWWWWWWNNNNNWWNNNNNNNNNWWNNNNNNWWWWWNXKNNk;..        .dNWWXd.             cXO.\n      .xXo   .ONWNWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWNNK0ko:'..OXo          'l0NXx,              :KK,\n      .OXc    :XNk0NWXKNWWWWWWWWWWWWWWWWWWWWWNNNX00NNx:'..       lXKc.     'lONN0l.              .oXK:\n      .KX;    .dNKoON0;lXNkcld0NXo::cd0NNO:;,,'.. .0Xc            lXXo..'l0NNKd,.              .c0Nk,\n      :XK.     .xNX0NKc.cXXl  ;KXl    .dN0.       .0No            .xNXOKNXOo,.               .l0Xk;.\n     .dXk.      .lKWN0d::OWK;  lXXc    .OX:       .ONx.     . .,cdk0XNXOd;.   .'''....;c:'..;xKXx,\n     .0No         .:dOKNNNWNKOxkXWXo:,,;ONk;,,,,,;c0NXOxxkO0XXNXKOdc,.  ..;::,...;lol;..:xKXOl.\n     ,XX:             ..';cldxkOO0KKKXXXXXXXXXXKKKKK00Okxdol:;'..   .';::,..':llc,..'lkKXkc.\n     :NX'    .     ''            ..................             .,;:;,',;ccc;'..'lkKX0d;.\n     lNK.   .;      ,lc,.         ................        ..,,;;;;;;:::,....,lkKX0d:.\n    .oN0.    .'.      .;ccc;,'....              ....'',;;;;;;;;;;'..   .;oOXX0d:.\n    .dN0.      .;;,..       ....                ..''''''''....     .:dOKKko;.\n     lNK'         ..,;::;;,'.........................           .;d0X0kc'.\n     .xXO'                                                 .;oOK0x:.\n      .cKKo.                                    .,:oxkkkxk0K0xc'.\n        .oKKkc,.                         .';cok0XNNNX0Oxoc,.\n          .;d0XX0kdlc:;,,,',,,;;:clodkO0KK0Okdl:,'..\n              .,coxO0KXXXXXXXKK0OOxdoc:,..\n";			
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}
}

//_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/

// simple observer interface
interface Observer {
	
	public void update(String transaction);

}