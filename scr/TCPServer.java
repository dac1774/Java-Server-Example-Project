/*David Chavez ID:1000771774 This program is a simple TCP server that responds
 * to http requests pointed to localhost port 8080. Codes 200, 301 and 404 are
 * implemented*/
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.Scanner; 
/*This class takes a socket, reads its buffer for commands, then generates a response.
 * This class is thread optimized
 * INPUTS: connectionSocket RETURNS: none*/
class handle_connection extends Thread{
	//init variable to hold socket data
	private Socket connectionSocket;
	//this is used to pass socket data like a function call
	public handle_connection(Socket connectionSocket) {
		this.connectionSocket = connectionSocket;
	}
	//override the Thread object's run()
	@Override
	//when We run handle_connection we get the HTTP GET request by creating in buffers and out buffers
	//the in buffer has the HTTP request, and we will push the response to the out buffers
	public void run() {
		String clientCommand, response;
		
		try {

			//in buffer
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			//out buffer
			DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			
			//parse the command string to extract what resource is being requested
			clientCommand = inFromClient.readLine();
			String[] tokens = clientCommand.split(" ");
			//note resource will be in tokens[1]
			
			String data;
			long length;
			switch(tokens[1]) {
			//normal response 200
			case "/index.html":
				//test.html is the main HTML for the page
				data = readFileToString("test.html");
				length = data.length();
				//create header response, note that data is appended to the end here
				response = "HTTP/1.1 200 Ok\r\n" + 
						"Content-Length: "+length+"\r\n" + 
						"Content-Type: html\r\n\r\n"
						+ data;
				outToClient.writeBytes(response);
				break;
			//301 response
			case "/":
				response = "HTTP/1.1 301 Moved Permanetly\r\n" + 
						"Location: http://127.0.0.1:8080/index.html\r\n\r\n";
						
				outToClient.writeBytes(response);
				break;
			//normal response 200
			case "/puppy.jpg":
				File file = new File("puppy.jpg");
				
				length = file.length();
				response = "HTTP/1.1 200 Ok\r\n" + 
						"Content-Length: "+length+"\r\n" + 
						"Content-Type: image/jpg\r\n\r\n";
				//note that here we create the header, send that, then send the raw photo data
				outToClient.writeBytes(response);
				Files.copy(file.toPath(), outToClient);
				break;
			//404 response
			default:
				//read 404 html data, then send that
				data = readFileToString("404.html");
				length = data.length();
				response = "HTTP/1.1 404 Not Found\r\n" + 
						"Content-Length: "+length+"\r\n" + 
						"Content-Type: html\r\n\r\n"
						+ data;
				outToClient.writeBytes(response);
				break;
				
			
			}
			connectionSocket.close();  //close socket for proper performance
	}
		catch (Exception e) {
			System.out.println("Error in socket thread");
		}
	}
	/* this function takes a file reads line by line into a string, then returns string
	 * mainly used to transfer html file
	 * INPUT: filepath of HTML RETURN: String containing HTML data meant to be added after
	 * HTTP header*/
	private String readFileToString(String path) {

	    File file = new File(path);
	    StringBuilder fileData = new StringBuilder((int)file.length());        

	    try {
	    	Scanner scanner = new Scanner(file);
	        while(scanner.hasNextLine()) {
	            fileData.append(scanner.nextLine() + System.lineSeparator());
	        }
	        
	        return fileData.toString();
	    }
	    catch(Exception IOException){
	    	System.out.println("Error opening scanner");
	    	return "";
	    }
	}
	
}
/*main thread, constantly listens for incoming connections, spawns a thread for each request.*/
public class TCPServer {

	public static void main(String[] args) throws Exception{
		
		ServerSocket welcomeSocket = new ServerSocket(8080);
		
		while(true) {
			Socket connectionSocket = welcomeSocket.accept();
			new handle_connection(connectionSocket).start();
			
		}
	}

}
