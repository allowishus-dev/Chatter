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

public class ClientSockets {
	private BufferedReader inFromServer;
	private DataOutputStream outToServer;
    private Socket clientSocket;

	private boolean isStreamReady;
    
    public ClientSockets() {
    	
    	isStreamReady = false;
    	
    }
    
    public Socket getSocket() {
		return clientSocket;
	}
    
    // returns boolean depending on success    
    public boolean writeToServer(String sentence) {
    	try {
    		if (clientSocket.isConnected()) {
    			
    			// add \n to make readLine() react in the server
    			sentence += "\n";
    			// write message to socket as bytes
    			outToServer.write(sentence.getBytes());
    			
    		}
    		else return false;
    	} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
    	return true;
    }
    
    public void connectToServer(String address, int port) throws ConnectException, UnknownHostException, IllegalArgumentException, IOException {
        	
    	System.out.println("Looking for server ...");
    	
    	// create a new socket using the attributes address and port
		clientSocket = new Socket(address, port);
		// create an data output stream to connect to socket 
		outToServer = new DataOutputStream(clientSocket.getOutputStream());

        System.out.println("Found server");
   
    }
    
    // make buffered reader available to gui
    public BufferedReader readChat() {
    	
    	if (clientSocket.isConnected()) {
    		try {
    			// if stream not established    			
    			if (!isStreamReady()) {
    				
    				// .. create a new one
    				inFromServer = new BufferedReader(
    						new InputStreamReader(clientSocket.getInputStream()));
    				setStreamReady(true);
    			}
    		
		} catch (IOException e) {
			e.printStackTrace();
			// if streaming fails return nothing 
			return null;
		}
    	
    		return inFromServer;
    	}
    	
    	// if not connected return nothing
    	return null;
    }

	public boolean isStreamReady() {
		return isStreamReady;
	}

	public void setStreamReady(boolean isSteamReady) {
		this.isStreamReady = isSteamReady;
	}
}