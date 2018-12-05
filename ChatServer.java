import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.*;

public class ChatServer {
	public static int port1;
	
	static ExecutorService executor;
	
	static ServerSocket server_socket;
	static Map<Socket, PrintWriter> socketPrintWriterMap;
	static Map<String, String> clientToPortMap = new HashMap<String, String>();
	
	public static void main(String [] args) {
		try {
			port1 = Integer.parseInt(args[0]);
			socketPrintWriterMap = new HashMap<>();
			executor = Executors.newFixedThreadPool(16);
			server_socket = new ServerSocket(port1);
		}
		catch (Exception e) {
			
		}
		
		accept();
		executor.shutdownNow();
	}
	
	public static void accept() {
		try {
			while(true) {
				Socket client_socket = server_socket.accept();
				PrintWriter output = new PrintWriter(client_socket.getOutputStream(), true);
				socketPrintWriterMap.put(client_socket, output);
			    executor.submit(() -> handle(client_socket));
			}
		}
		catch(Exception e) {	
			
		}
	}
		 
	public static void handle(Socket client_socket) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));	
			String message;
			String port2;
			String name = null;
			socketPrintWriterMap.get(client_socket).println("What is your name?");			
			while((message = in.readLine()) != null) {
				if(name == null) {
					name = message;				
					port2 = (in.readLine());
					clientToPortMap.put(name, port2);
				} 
				else if(message.startsWith("-f")) {
					String owner = message.replaceAll("-f","");
					String file = in.readLine();
					socketPrintWriterMap.get(client_socket).println(file);
					socketPrintWriterMap.get(client_socket).println(clientToPortMap.get(owner));
				} else {
					for(Map.Entry<Socket, PrintWriter> e : socketPrintWriterMap.entrySet()) {
						if(!(e.getKey() == client_socket))
							e.getValue().println(name + ": " + message);	
					}
				}
		    }
			clientToPortMap.remove(name);
			socketPrintWriterMap.remove(client_socket);
		}
		catch(Exception e) {
			
		}
	}
}
