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
	private ArrayList<Observer> observers = new ArrayList<Observer>();
	private static String clientSentence;
	private ServerSocket welcomeSocket;
	
	public ChatServer() throws Exception {

		int threadNumber = 0;
	
		welcomeSocket = new ServerSocket(664);
		  
	    while(true) {
	    	System.out.println("Waiting for external socket with request ...\n\n");
			Socket connectionSocket = welcomeSocket.accept(); 
			System.out.println("Found client");
			
			Runnable socketThread = new SocketThread(connectionSocket, this, threadNumber);
			new Thread(socketThread).start();
			
			System.out.println("Creating thread (" + threadNumber + ") for " + connectionSocket.getInetAddress() + 
					"\nActive thread count: " + Thread.activeCount() + "\n");
			threadNumber++;
	
	    } 
	}

	public static void main(String args[]) throws Exception { 
		new ChatServer();
	   	
    }

	public void register(Observer o) {
		observers.add(o);		
	}

	public void remove(Observer o) {
		observers.remove(o);		
	}

	public void notifyObservers() {
		for (Observer obs : observers) {
			obs.update(clientSentence);
		}		
	}
	
	public void recievedMessage(String clientSentence) {
		ChatServer.clientSentence = clientSentence;
		notifyObservers();
	}
}

//_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/

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
			
			Runnable ih = new InputHandler(chatServer, this);
			
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

	@Override
	public void update(String sentence) {
	
		try {
				
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
		face = (int) threadNumber % faces.length;
		System.out.println(face);
		clientAdress = socketThread.getSocket().getInetAddress().toString().substring(1);
		inFromClient = new BufferedReader(
			new InputStreamReader(socketThread.getSocket().getInputStream()));
	}

	@Override
	public void run() {
		try {
	
			while (true) {
			
				// listening to input
				System.out.println("Reading incoming data ...");
				clientSentence = inFromClient.readLine();
				System.out.println("Recieved : " + clientSentence);
				
				if (clientSentence.startsWith("C") && !socketThread.isConnected()) {
					
					chatServer.register((Observer) socketThread);
					String s = clientSentence.substring(1);
					if (!s.equals("")) {
						alias = s; 
					}
					System.out.println("SERVER: " + alias + "@" + clientAdress + " just entered the chat");
					chatServer.recievedMessage("FROM SERVER: " + alias + "@" + clientAdress + " just entered the chat");
					socketThread.setConnected(true);
					
				}
				else if (clientSentence.startsWith("M") && socketThread.isConnected()) {
					if (clientSentence.startsWith("Mtroll")) clientSentence += "\n" + troll();
					
					chatServer.recievedMessage("[" + alias + "@" + clientAdress + "]  " + faces[face] + "  ~( " + clientSentence.substring(1) + " )");
					
					
				}
				else if (clientSentence.startsWith("D") && socketThread.isConnected()) {
					System.out.println("Threads before disconnect : " + Thread.activeCount());
					
					System.out.println("SERVER: " +  alias + "@" + clientAdress + " just left the chat");
					chatServer.recievedMessage("FROM SERVER: " +  alias + "@" + clientAdress + " just left the chat");
										
					
					// killing input handler
					System.out.println("killing input handler");
					break;
				}				
			}
			
			// stop getting messages from others in chat
			System.out.println("stop getting messages from others in chat");
			chatServer.remove(socketThread);
			
			// closing client socket
			System.out.println("closing client socket");
			socketThread.getSocket().close();
			socketThread.setSocket(null);
			
			System.out.println("Threads after disconnect : " + Thread.activeCount());
			

		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String troll() {
		return "                                   .....'',;;::cccllllllllllllcccc:::;;,,,''...'',,'..\n                            ..';cldkO00KXNNNNXXXKK000OOkkkkkxxxxxddoooddddddxxxxkkkkOO0XXKx:.\n                      .':ok0KXXXNXK0kxolc:;;,,,,,,,,,,,;;,,,''''''',,''..              .'lOXKd'\n                 .,lx00Oxl:,'............''''''...................    ...,;;'.             .oKXd.\n              .ckKKkc'...'',:::;,'.........'',;;::::;,'..........'',;;;,'.. .';;'.           'kNKc.\n           .:kXXk:.    ..       ..................          .............,:c:'...;:'.         .dNNx.\n          :0NKd,          .....''',,,,''..               ',...........',,,'',,::,...,,.        .dNNx.\n         .xXd.         .:;'..         ..,'             .;,.               ...,,'';;'. ...       .oNNo\n         .0K.         .;.              ;'              ';                      .'...'.           .oXX:\n        .oNO.         .                 ,.              .     ..',::ccc:;,..     ..                lXX:\n       .dNX:               ......       ;.                'cxOKK0OXWWWWWWWNX0kc.                    :KXd.\n     .l0N0;             ;d0KKKKKXK0ko:...              .l0X0xc,...lXWWWWWWWWKO0Kx'                   ,ONKo.\n   .lKNKl...'......'. .dXWN0kkk0NWWWWWN0o.            :KN0;.  .,cokXWWNNNNWNKkxONK: .,:c:.      .';;;;:lk0XXx;\n  :KN0l';ll:'.         .,:lodxxkO00KXNWWWX000k.       oXNx;:okKX0kdl:::;'',;coxkkd, ...'. ...'''.......',:lxKO:.\n oNNk,;c,'',.                      ...;xNNOc,.         ,d0X0xc,.     .dOd,           ..;dOKXK00000Ox:.   ..''dKO,\n'KW0,:,.,:..,oxkkkdl;'.                'KK'              ..           .dXX0o:'....,:oOXNN0d;.'. ..,lOKd.   .. ;KXl.\n;XNd,;  ;. l00kxoooxKXKx:..ld:         ;KK'                             .:dkO000000Okxl;.   c0;      :KK;   .  ;XXc\n'XXdc.  :. ..    '' 'kNNNKKKk,      .,dKNO.                                   ....       .'c0NO'      :X0.  ,.  xN0.\n.kNOc'  ,.      .00. ..''...      .l0X0d;.             'dOkxo;...                    .;okKXK0KNXx;.   .0X:  ,.  lNX'\n ,KKdl  .c,    .dNK,            .;xXWKc.                .;:coOXO,,'.......       .,lx0XXOo;...oNWNXKk:.'KX;  '   dNX.\n  :XXkc'....  .dNWXl        .';l0NXNKl.          ,lxkkkxo' .cK0.          ..;lx0XNX0xc.     ,0Nx'.','.kXo  .,  ,KNx.\n   cXXd,,;:, .oXWNNKo'    .'..  .'.'dKk;        .cooollox;.xXXl     ..,cdOKXXX00NXc.      'oKWK'     ;k:  .l. ,0Nk.\n    cXNx.  . ,KWX0NNNXOl'.           .o0Ooldk;            .:c;.':lxOKKK0xo:,.. ;XX:   .,lOXWWXd.      . .':,.lKXd.\n     lXNo    cXWWWXooNWNXKko;'..       .lk0x;       ...,:ldk0KXNNOo:,..       ,OWNOxO0KXXNWNO,        ....'l0Xk,\n     .dNK.   oNWWNo.cXK;;oOXNNXK0kxdolllllooooddxk00KKKK0kdoc:c0No        .'ckXWWWNXkc,;kNKl.          .,kXXk,\n      'KXc  .dNWWX;.xNk.  .kNO::lodxkOXWN0OkxdlcxNKl,..        oN0'..,:ox0XNWWNNWXo.  ,ONO'           .o0Xk;\n      .ONo    oNWWN0xXWK, .oNKc       .ONx.      ;X0.          .:XNKKNNWWWWNKkl;kNk. .cKXo.           .ON0;\n      .xNd   cNWWWWWWWWKOkKNXxl:,'...;0Xo'.....'lXK;...',:lxk0KNWWWWNNKOd:..   lXKclON0:            .xNk.\n      .dXd   ;XWWWWWWWWWWWWWWWWWWNNNNNWWNNNNNNNNNWWNNNNNNWWWWWNXKNNk;..        .dNWWXd.             cXO.\n      .xXo   .ONWNWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWNNK0ko:'..OXo          'l0NXx,              :KK,\n      .OXc    :XNk0NWXKNWWWWWWWWWWWWWWWWWWWWWNNNX00NNx:'..       lXKc.     'lONN0l.              .oXK:\n      .KX;    .dNKoON0;lXNkcld0NXo::cd0NNO:;,,'.. .0Xc            lXXo..'l0NNKd,.              .c0Nk,\n      :XK.     .xNX0NKc.cXXl  ;KXl    .dN0.       .0No            .xNXOKNXOo,.               .l0Xk;.\n     .dXk.      .lKWN0d::OWK;  lXXc    .OX:       .ONx.     . .,cdk0XNXOd;.   .'''....;c:'..;xKXx,\n     .0No         .:dOKNNNWNKOxkXWXo:,,;ONk;,,,,,;c0NXOxxkO0XXNXKOdc,.  ..;::,...;lol;..:xKXOl.\n     ,XX:             ..';cldxkOO0KKKXXXXXXXXXXKKKKK00Okxdol:;'..   .';::,..':llc,..'lkKXkc.\n     :NX'    .     ''            ..................             .,;:;,',;ccc;'..'lkKX0d;.\n     lNK.   .;      ,lc,.         ................        ..,,;;;;;;:::,....,lkKX0d:.\n    .oN0.    .'.      .;ccc;,'....              ....'',;;;;;;;;;;'..   .;oOXX0d:.\n    .dN0.      .;;,..       ....                ..''''''''....     .:dOKKko;.\n     lNK'         ..,;::;;,'.........................           .;d0X0kc'.\n     .xXO'                                                 .;oOK0x:.\n      .cKKo.                                    .,:oxkkkxk0K0xc'.\n        .oKKkc,.                         .';cok0XNNNX0Oxoc,.\n          .;d0XX0kdlc:;,,,',,,;;:clodkO0KK0Okdl:,'..\n              .,coxO0KXXXXXXXKK0OOxdoc:,..\n";			
	}
}

//_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/

interface Observer {
	
	public void update(String transaction);

}