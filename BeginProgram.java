import java.util.*;
import java.io.*;
import java.net.*;

@SuppressWarnings("serial")
public class BeginProgram implements Serializable  {
	static String resultFileName;
	int id;
	int nodeCount,minActive,maxActive,sendWait,snapShotInterval,maxMessages;
	int totalMessagesSent = 0;
	boolean active=false;
	int[][] spanMatrix;
	int[] vectorClock;
	int[] totalNeighbours;
	boolean blockApplicationMessage = false;
	public Colour colour = Colour.BLUE;
	int entry=0;
	boolean begining = true;
	String configurationFileName;
    boolean[] graphNodes;
	StateMsg myState;
	
	HashMap<Integer,ObjectOutputStream> outputStream = new HashMap<Integer,ObjectOutputStream>();
	
	HashMap<Integer,StateMsg> stateMsgs;	
	
	ArrayList<Node> nodes = new ArrayList<Node>();
	
	HashMap<Integer,ArrayList<ApplMessage>> chnlStates;
	
	HashMap<Integer,Node> nodeStore = new HashMap<Integer,Node>();
	
	HashMap<Integer,Boolean> markerRcvd;
	
	HashMap<Integer,Socket> nodesChannels = new HashMap<Integer,Socket>();
	
	ArrayList<int[]> output = new ArrayList<int[]>();


	public static void main(String[] args) throws IOException, InterruptedException {
		
		BeginProgram primary = ReadNodesDetails.getMainData(args[1]);
		
		primary.id = Integer.parseInt(args[0]);
		int currentNode = primary.id;
		
		primary.configurationFileName = args[1];
		BeginProgram.resultFileName = primary.configurationFileName.substring(0, primary.configurationFileName.lastIndexOf('.'));
		
		SpanningTree.spanningTree(primary.spanMatrix);
		
		for(int i=0;i<primary.nodes.size();i++){
			primary.nodeStore.put(primary.nodes.get(i).nodeId, primary.nodes.get(i));
		}
		 
		int serverPort = primary.nodes.get(primary.id).port;
		
		ServerSocket listening = new ServerSocket(serverPort);
		Thread.sleep(10000);
		
		for(int i=0;i<primary.nodeCount;i++){
			
			if(primary.spanMatrix[currentNode][i] == 1){
												String hostName = primary.nodeStore.get(i).host;
				
				int port = primary.nodeStore.get(i).port;
												InetAddress address = InetAddress.getByName(hostName);
												Socket client = new Socket(address,port);
			
				primary.nodesChannels.put(i, client);
		
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(client.getOutputStream());
				primary.outputStream.put(i, objectOutputStream);		
			}
		}

		 
		Set<Integer> keys = primary.nodesChannels.keySet();
		primary.totalNeighbours = new int[keys.size()];
		int index = 0;
		for(Integer element : keys) primary.totalNeighbours[index++] = element.intValue();
		
		primary.vectorClock = new int[primary.nodeCount];

		
		primary.init(primary);

		
		if(currentNode == 0){
			primary.active = true;
			
			new ProtocolThread(primary).start();		
			new SendMessagesThread(primary).start();
		}
		try {
			while (true) {
				 
				Socket soc = listening.accept();
				
				new ClientThread(soc,primary).start();
			}
		}
		finally {
			listening.close();
		}
	}


	void sendMessages() throws InterruptedException{

		
		int numMsgs = 1;
		int sendWait = 0;
		synchronized(this){
			numMsgs = this.generateRandomNumber(this.maxActive,this.minActive);
			
			if(numMsgs == 0){
				numMsgs = this.generateRandomNumber(this.maxActive,this.minActive + 1);
			}
			sendWait = this.sendWait;
		}
		
		for(int i=0;i<numMsgs;i++){
			synchronized(this){
			
				int neighbourIndex = this.generateRandomNumber(this.totalNeighbours.length-1,0);
				int currentNeighbour = this.totalNeighbours[neighbourIndex];

				if(this.active == true){
					
					ApplMessage msg = new ApplMessage(); 
					
					this.vectorClock[this.id]++;
					msg.vectorClock = new int[this.vectorClock.length];
					System.arraycopy( this.vectorClock, 0, msg.vectorClock, 0, this.vectorClock.length );
					msg.nodeId = this.id;
					
					try {
						ObjectOutputStream objectOutputStream = this.outputStream.get(currentNeighbour);
						objectOutputStream.writeObject(msg);	
						objectOutputStream.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}	
					totalMessagesSent++;
				}
			}
			try {
				Thread.sleep(sendWait);
			} catch (InterruptedException e) {
				System.out.println("Error in EmitMessages");
			}
		}
		synchronized(this){
			
			this.active = false;
		}

	}

	

	void init(BeginProgram primary){
		primary.chnlStates = new HashMap<Integer,ArrayList<ApplMessage>>();
		primary.markerRcvd = new HashMap<Integer,Boolean>();
		primary.stateMsgs = new HashMap<Integer,StateMsg>();	

		Set<Integer> keys = primary.nodesChannels.keySet();
		
		for(Integer element : keys){
			ArrayList<ApplMessage> arrList = new ArrayList<ApplMessage>();
			primary.chnlStates.put(element, arrList);
		}
		
		for(Integer e: primary.totalNeighbours){
			primary.markerRcvd.put(e,false);
		}
		primary.graphNodes = new boolean[primary.nodeCount];
		primary.myState = new StateMsg();
		primary.myState.vectorClock = new int[primary.nodeCount];
	}
	int generateRandomNumber(int max,int min){
		
		Random randNum = new Random();
	
		int randNumber = randNum.nextInt((max - min) + 1) + min;
		return randNumber;
	}

}

 
class ClientThread extends Thread {
	Socket clientSocket;
	BeginProgram primary;

	public ClientThread(Socket clientSocket,BeginProgram primary) {
		this.clientSocket = clientSocket;
		this.primary = primary;
	}

	public void run() {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(clientSocket.getInputStream());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		while(true){
			try {
				Communication msg;
				msg = (Communication) ois.readObject();
	
				synchronized(primary){

					if(msg instanceof MarkerMsg){
						int channelNo = ((MarkerMsg) msg).nodeId;
						CLProtocol.sendMarkerMessage(primary,channelNo);
					}	

					else if((primary.active == false) && msg instanceof ApplMessage && 
							primary.totalMessagesSent < primary.maxMessages && primary.entry == 0){
						primary.active = true; 
						new SendMessagesThread(primary).start();
					}

					else if((primary.active == false) && (msg instanceof ApplMessage) && (primary.entry == 1)){

						int channelNo = ((ApplMessage) msg).nodeId;

						CLProtocol.logMessage(channelNo,((ApplMessage) msg) ,primary);
					}

					else if(msg instanceof StateMsg){
						if(primary.id == 0){
							
							primary.stateMsgs.put(((StateMsg)msg).nodeId,((StateMsg)msg));
							primary.graphNodes[((StateMsg) msg).nodeId] = true;
							
							if(primary.stateMsgs.size() == primary.nodeCount){
							
								boolean restartLamport = CLProtocol.processStateMessages(primary,((StateMsg)msg));
								if(restartLamport){
							
									primary.init(primary);
									
									new ProtocolThread(primary).start();	
								}								
							}
						}
						else{
							
							CLProtocol.frwdToParent(primary,((StateMsg)msg));
						}
					}
					
					else if(msg instanceof CompleteMessage){	
						
						CLProtocol.sendCompleteMessage(primary);
					}

					if(msg instanceof ApplMessage){
						
						for(int i=0;i<primary.nodeCount;i++){
							primary.vectorClock[i] = Math.max(primary.vectorClock[i], ((ApplMessage) msg).vectorClock[i]);
						}
						primary.vectorClock[primary.id]++;
						
					}
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}


 class SendMessagesThread extends Thread{

	BeginProgram primary;
	public SendMessagesThread(BeginProgram primary){
		this.primary = primary;
	}
	public void run(){
		try {
			primary.sendMessages();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

 class Node {
	int nodeId;
	String host;
	int port;
	public Node(int nodeId, String host, int port) {
		super();
		this.nodeId = nodeId;
		this.host = host;
		this.port = port;
	}
}
 
 enum Colour { RED,BLUE};
 
 class ProtocolThread extends Thread{

	BeginProgram primary;
	public ProtocolThread(BeginProgram primary){
		this.primary = primary;
	}
	public void run(){
	
		if(primary.begining){
			primary.begining = false;
		}

		else{
			try {
				Thread.sleep(primary.snapShotInterval);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		CLProtocol.startSnapshotProtocol(primary);
	}
}
