/**
 * Project 5
 * @author Jared Rassbach, jrassbac, LM1
 * @author Travis Kovacic, tdkovaci, LM1
 */
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class SafeWalkServer extends ServerSocket implements Runnable {
	public ServerSocket ss;
	public ArrayList<String[]> clientRequests = new ArrayList<String[]>();
	// Arraylist for clients. Allows for us to mess with individual and past
	// connections
	public ArrayList<Socket> clients = new ArrayList<Socket>();
	
	public String[] locations = new String[] {"CL50","EE","LWSN","PMU","PUSH","*"};

	public SafeWalkServer(int port) throws IOException {
		ss = new ServerSocket(port);
	}

	public SafeWalkServer() throws IOException {
		ss = new ServerSocket(0);
	}

	public void run() {

		do {
			// Socket client;
			try {
				System.out.println("Waiting for client on port "
						+ ss.getLocalPort() + "...");
				Socket client = ss.accept();

				// Adds the connection to the client arraylist
				//clients.add(client);

				// Opens ability to output to client, and opens input from the
				// client

				DataOutputStream dos = new DataOutputStream(
						client.getOutputStream());
				dos.flush();
				InputStream is = new DataInputStream(client.getInputStream());
				BufferedReader br = new BufferedReader(
						new InputStreamReader(is));

				// Command or request sent by the client
				String input = br.readLine();

				char[] inputTest = input.toCharArray();

				// Check for commands

				if (inputTest[0] == ':') {

					if (input.equals(":RESET")) {
						String resetMessage = "ERROR: connection reset\n";

						// Loop through all previous client connections (which
						// are on hold) and close them.

						// Keeps the current client connection alive to continue
						// the reset command
						// and give success command.
						for (int i = 0; i < clients.size(); i++) {
							// Sends the reset message to the client and flushes
							// to ensure it is sent
							// Closes connection of all previous clients.
							DataOutputStream temp = new DataOutputStream(
									clients.get(i).getOutputStream());
							temp.writeUTF(resetMessage);
							temp.flush();
							temp.close();
							clients.get(i).close();
						}

						// Responds with success message
						dos.writeUTF("RESPONSE: success\n");
						dos.flush();
						// Closes the client that originated the reset command.
						client.close();
						clientRequests.clear();
						clients.clear();

					} else if (input.equals(":SHUTDOWN")) {
						String resetMessage = "ERROR: connection reset\n";

						// Closes all connections to clients
						for (int i = 0; i < clients.size(); i++) {
							DataOutputStream temp = new DataOutputStream(
									clients.get(i).getOutputStream());
							temp.writeUTF(resetMessage);
							temp.flush();
							temp.close();
							clients.get(i).close();
						}

						dos.writeUTF("RESPONSE: success\n");
						dos.flush();
						
						clientRequests.clear();
						clients.clear();

						client.close();
						// Closes steams and resources
						dos.close();
						br.close();
						// Exits run loop of the server, closing it
						break;

					} else {
						// Check to see if the input even has commas
						String[] pendingRequestCommand = input.split(",");

						if (pendingRequestCommand[0].equals(":PENDING_REQUESTS")) {
							
							String task = pendingRequestCommand[1];
							String from = pendingRequestCommand[2];
							String to = pendingRequestCommand[3];
							
							if(task.equals("*")){
								
								String message = "";
								
								for(int i = 0; i < clientRequests.size(); i++){
									if(i!=clientRequests.size()-1){
									message += (Arrays.toString(clientRequests.get(i))) + ", ";
									} else {
										message += (Arrays.toString(clientRequests.get(i)));
									}
								}
								
								dos.writeUTF("[" + message + "]");
								
								client.close();
								
							} else {
								int counter = 0;
								if (from.equals("*") && to.equals("*")) {
									String m = "RESPONSE: # of pending requests = ";
									for (int i = 0; i<clients.size(); i++){
										counter++;
									}
									dos.writeUTF(m + counter + "\n");
									dos.flush();
									client.close();
								}
								else if (!from.equals("*") && to.equals("*")) {
									String m = "RESPONSE: # of pending requests from " + from + " = ";
									for (int i = 0; i<clients.size(); i++){
										if (from.equals(clientRequests.get(i)[1])){
											counter++;
										}
									}	
									dos.writeUTF(m + counter + "\n");
									dos.flush();
									client.close();
								}else if (from.equals("*") && !to.equals("*")) {//from== * and to== something not *
									String m = "RESPONSE: # of pending requests to " + to + " = ";
									for (int i = 0; i<clients.size(); i++){
										if (to.equals(clientRequests.get(i)[2])){
											counter++;
										}
									}	
									dos.writeUTF(m + counter + "\n");
									dos.flush();
									client.close();
								}else {
									dos.writeUTF("Invalid input\n");
									dos.flush();
									client.close();
								}
								
								
							}

						} else {
							// Responds with error message
							dos.writeUTF("ERROR: invalid command\n");
							dos.flush();
							client.close();
						}
					}

					// Splits the command

				} else {

					// Deal with normal request messages

					int commaCounter = 0;

					for (int i = 0; i < inputTest.length; i++) {

						if (inputTest[i] == ',') {
							commaCounter++;
						}
					}

					if (commaCounter == 2) {
						// Takes input and adds it to the arraylist for later
						// usage
						String[] info = new String[3];
						String[] temp = input.split(",");
						
						for(int i = 0; i<temp.length; i++){
							info[i]=temp[i];
						}

						// Splices the info string array into individual strings
						// for easier usage
						String name = info[0];
						String from = info[1];
						String to = info[2];
						
						if(info[2]==null){
							dos.writeUTF("ERROR: invalid request\n");
							dos.flush();
							client.close();
							continue;
						}
						
						int fromCounter = 0;
						int toCounter = 0;
						
						for(int i = 0; i < locations.length; i++){
							
							if(from.equals(locations[i])){
								fromCounter++;
							}
							
							if(to.equals(locations[i])){
								toCounter++;
							}
							
						}
						
						if(from.equals("*") || to.equals(from) || name.equals("") || from.equals("") || to.equals("") || fromCounter!=1 || toCounter!=1){
							dos.writeUTF("ERROR: invalid request\n");
							dos.flush();
							client.close();
							continue;
						}
						
						if (clientRequests.size() == 0) {
							clientRequests.add(info);
							// Adds the connection to the client arraylist
							clients.add(client);
						} else {
							for (int i = 0; i < clientRequests.size(); i++) {
								// Tests to find a 'from' match with past connections
								
								if (from.equals(clientRequests.get(i)[1])) {
									// Tests to finda 'to' match with past connections
									if ((to.equals(clientRequests.get(i)[2]) || (to.equals("*") && !clientRequests.get(i)[2].equals("*")) || (clientRequests.get(i)[2].equals("*")) && !to.equals("*"))) {
										if ( to.equals("*") && clientRequests.get(i)[2].equals("*")) {
											
											clientRequests.add(info);
											// Adds the connection to the client arraylist
											clients.add(client);
											break;
											
										} else {
										
											String pastClientInput = spaceRemover(Arrays.toString(clientRequests.get(i)));
											// Writes to current client
											dos.writeUTF("RESPONSE: " + pastClientInput);
											dos.flush();
											clientRequests.remove(i);
											
											// Writes to past client
											dos = new DataOutputStream(clients.get(i).getOutputStream());
											dos.flush();
											dos.writeUTF("RESPONSE: "+input);
											dos.flush();
											
											// Closes last client connection and removes them from arraylist
											clients.get(i).close();
											clients.remove(i);
											
											// Closes current client connection
											client.close();
										}
										
										
									} else {
										clientRequests.add(info);
										// Adds the connection to the client arraylist
										clients.add(client);
										break;
									}
									
								} else {
									clientRequests.add(info);
									// Adds the connection to the client arraylist
									clients.add(client);
									break;
								}
								
							}
						
						}

					} else {
						dos.writeUTF("ERROR: invalid request\n");
						dos.flush();
						client.close();
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		} while (true);

	}
	
	public String spaceRemover(String s){
		
		String noBracket = s.substring(1, s.length()-1);
		
		String whiteRemoved = noBracket.replaceAll("\\s+", "");
		
		return whiteRemoved;
		
	}

	public static boolean isPortValid(String port) {
		int low = 1025;
		int hi = 65535;
		int numPort = 0;
		try {
			numPort = Integer.parseInt(port);
		} catch (NumberFormatException e) {
			System.out.println("Port not Vaild. Using free port 8888.");
		}

		if (numPort < low || numPort > hi)
			return false;
		else
			return true;

	}

	public static void main(String[] args) {
		SafeWalkServer sws = null;

		if (args.length > 0 && isPortValid(args[0])) {
			int port = Integer.parseInt(args[0]);
			try {
				sws = new SafeWalkServer(port);
			} catch (IOException e) {
				System.out.println("IOException was thrown.");
			}

		} else {
			try {
				sws = new SafeWalkServer();
			} catch (IOException e) {
				System.out.println("IOException was thrown.");
			}
		}
		Thread t = new Thread(sws);
		t.start();

		// Client c = new Client("localhost",sws.getLocalPort(),":RESET", true);

		// sws.run();
		// c.run();

	}

}
