import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class ChatClient {
	public static int port1;
	public static int port2;
	int port3;
	
	ExecutorService executor = Executors.newFixedThreadPool(1);
	
	Socket client_socket;
	ServerSocket server_socket;
	Socket file_transfer_socket;
	PrintWriter out;
	BufferedReader in;
	BufferedReader userInput;
	DataInputStream input;
	DataOutputStream output;
	
	public static void main(String [] args) {
		try{
			port1 = Integer.parseInt(args[3]);
			port2 = Integer.parseInt(args[1]);
		}
		catch (Exception e)  {

		}
		ChatClient client = new ChatClient(port1, port2);
		client.execute();
	}
	public ChatClient(int s, int l) {
		port1 = s;
		port2 = l;
		try{
			client_socket = new Socket("localhost", port1);
			out = new PrintWriter(client_socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));
			userInput = new BufferedReader(new InputStreamReader(System.in));
		}
		catch(Exception e) {	
			
		}
	}
	
	public void execute() {
		ExecutorService threads = Executors.newFixedThreadPool(3); 
		threads.submit(this::read);
		threads.submit(this::write);
		threads.submit(this::writeFiles);
		threads.shutdown();
	}
	
	public void read() {
		try{
			String message;
			while((message = in.readLine()) != null) {
				if(message.startsWith("-f")) {
					String fileName = message.replaceAll("-f","");			
					String fp = in.readLine();
					port3 = Integer.parseInt(fp);			
					executor.submit(() -> readFiles(fileName, port3));
				}
				else {
					System.out.println(message);
				}
			}
			try {
				client_socket.shutdownOutput();
				client_socket.close();
				System.exit(0);
			}
			catch(Exception e) {
				
			}
		}
		catch(Exception e){
			
		}
	}

	public void write() {
		try{
			String message;
			String name = null;
			while((message = userInput.readLine()) != null) {
				if(name == null) {
					name = message; 
					out.println(message);
					out.println(port2);
				}
				else if(message.equals("f")) {
					System.out.println("Who owns the file?");
					String fileOwner = userInput.readLine();
					System.out.println("Which file do you want?");
					String file = userInput.readLine();
					out.println("-f" + fileOwner);
					out.println("-f" + file);	
				}
				else if(message.equals("m")) {
					System.out.println("Enter your message:");
					message = userInput.readLine();
					out.println(message);
				}
				else if(message.equals("x")) {
					break;
				} 
				System.out.println("Enter an option ('m', 'f', 'x'):\r\n" + 
							" (M)essage (send)\r\n" + 
							" (F)ile (request)\r\n" + 
							"e(X)it");
			}
			try{
				client_socket.shutdownOutput();
				client_socket.close();
				System.exit(0);
			}
			catch(Exception e) {
				
			}
		}
		catch(Exception e){
			
		}
	}
	
	public void readFiles(String fName, int f) {
		try{ 
			file_transfer_socket = new Socket("localhost", f);
			input = new DataInputStream(file_transfer_socket.getInputStream());
			output = new DataOutputStream(file_transfer_socket.getOutputStream());		
			output.writeUTF(fName);	
			FileOutputStream fos = new FileOutputStream(fName);	
			int n;	
			byte [] buffer = new byte[1500];	
			while((n = input.read(buffer)) != -1) {
				fos.write(buffer, 0, n);
			}
			fos.close();
			file_transfer_socket.close();
		}
		catch(Exception e) {
			
		}
	}
	
	public void writeFiles() {
		try{
			server_socket = new ServerSocket(port2);
			while(true) {
				file_transfer_socket = server_socket.accept();
				input = new DataInputStream(file_transfer_socket.getInputStream());
				output = new DataOutputStream(file_transfer_socket.getOutputStream());		
				String fName = input.readUTF();
				File f = new File(fName);	
				if(!f.exists() || !f.canRead() || f.length() == 0) {
					System.out.println("file doesn't exist");
					file_transfer_socket.close();
					continue;
				}
				
				FileInputStream fileInput = new FileInputStream(f);
				int n;
				byte[] buffer = new byte[1500];
				while((n = fileInput.read(buffer)) != -1) {
					output.write(buffer, 0, n);
				}
				
				fileInput.close();
				file_transfer_socket.close();
			}
		}
		catch(Exception e) {
			
		}
	}
}