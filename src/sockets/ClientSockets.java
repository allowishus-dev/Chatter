/*
                  █▄ █▄                ▀    ▄█ █              ▄█            
              ▄▄  ▀█▄ █▓  ▄▄▄ ▀▄    ▄█ █▄ ▄█▀  █▄▄▄ ▀▄      ▄█▀             
             ▄▄▄█▄ █▓ █▓ █▓  █ ██ ▄ █▓ ▓█ █▓   █ ▀██ █   █  █▓              
         ██ ██ ▀█▓ █  █▀ █▓ ▄▀  █▄██▀ ▄█   ▀▀▄ █  ▓█ ██ ▓█   ▀▀▄ ██         
         ██▄ ▀▀▄█▀  ▀  ▀  ▀▀ ▄█▄ ▀ ▀ ▄▄▄ ▀▄▄█▀ ▀  ██ ▀█▄█▀ ▀▄▄█▀ ██         
         █████▄▄▄▄███████████████████████▄▄▄▄▄███ ▀ ▄▄▄▄▄▄█▄▄▄▄▄███         
         ██████████████████████████████████████████████████████████         
         
 */

package sockets;

import java.io.*; 
import java.net.*; 

public class ClientSockets {
    private String sentence;
	private BufferedReader inFromServer;
	private DataOutputStream outToServer;
    private Socket clientSocket;
    private boolean isStreamReady;
    
    public ClientSockets() {
    	
    	isStreamReady = false;
    	
    }
//
//    private
//
//
//        
//        try {
//			
//			Runnable ih = new ClientInputHandler(clientSocket);
//			
//			new Thread(ih).start();
//			
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//        
//        while (true) {
//	
//	        if (sentence == "quit") {
//	        	clientSocket.close();
//	        	break;
//	        }
//        }
//    }
//    
//    inFromServer = new BufferedReader(new
//        	InputStreamReader(getSocket().getInputStream()));
    
    public boolean writeToServer(String sentence) {
    	try {
    		if (clientSocket.isConnected()) {
    			
//    			byte[] bytes = new byte[sentence.length()];
//    			bytes = sentence.getBytes();
    			sentence += "\n";
    			outToServer.write(sentence.getBytes());
    			
    		}
    		else return false;
    	} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
    	return true;
    }
    
    public boolean connectToServer(String adress, int port) {
        try {
        	
        	System.out.println("Looking for server ...");
			clientSocket = new Socket(adress, port);
			outToServer = new DataOutputStream(clientSocket.getOutputStream());
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		} catch (ConnectException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        System.out.println("Found server");
        
		return true;
    }
    
    public BufferedReader readChat() {
    	
    	if (clientSocket.isConnected()) {
    		try {
    			if (!isStreamReady()) {
    		
    				inFromServer = new BufferedReader(
    						new InputStreamReader(clientSocket.getInputStream()));
    				setStreamReady(true);
    			}
    		
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
    	
    		return inFromServer;
    	}
    	
    	return null;
    }

	public boolean isStreamReady() {
		return isStreamReady;
	}

	public void setStreamReady(boolean isSteamReady) {
		this.isStreamReady = isSteamReady;
	}
}