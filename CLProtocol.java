import java.util.*;
import java.io.*;



public class CLProtocol { 
   
	public static void logMessage(int channelNo,ApplMessage m, BeginProgram primary) {
		synchronized(primary){
 
			if(!(primary.chnlStates.get(channelNo).isEmpty()) && primary.markerRcvd.get(channelNo) != true){
				primary.chnlStates.get(channelNo).add(m);
			}

			else if((primary.chnlStates.get(channelNo).isEmpty()) && primary.markerRcvd.get(channelNo) != true){
				ArrayList<ApplMessage> msgs = primary.chnlStates.get(channelNo);
				msgs.add(m);
				primary.chnlStates.put(channelNo, msgs);
			}
		}
	}

	public static void sendMarkerMessage(BeginProgram primary, int channelNo){
		
		synchronized(primary){
			if(primary.colour == Colour.BLUE){
	primary.markerRcvd.put(channelNo, true);
		primary.colour = Colour.RED;
	primary.myState.active = primary.active;
	primary.myState.vectorClock = primary.vectorClock;
primary.myState.nodeId = primary.id;

		int[] vectorClockCopy = new int[primary.myState.vectorClock.length];
		for(int i=0;i<vectorClockCopy.length;i++){ vectorClockCopy[i] = primary.myState.vectorClock[i]; 				}
primary.output.add(vectorClockCopy);

		primary.entry = 1;
	for(int i : primary.totalNeighbours){
					MarkerMsg m = new MarkerMsg();					m.nodeId = primary.id;
					ObjectOutputStream objectOutputStream = primary.outputStream.get(i);
					try {
						objectOutputStream.writeObject(m);
					} catch (IOException exception) {	System.out.println(exception.getMessage()); }
				}
		if((primary.totalNeighbours.length == 1) && (primary.id!=0)){		primary.colour = Colour.BLUE;		
		primary.myState.chnlStates = primary.chnlStates;
					int parentId = SpanningTree.getParentId(primary.id);
					primary.entry = 0;
					
				ObjectOutputStream objectOutputStream = primary.outputStream.get(parentId);

					try {
			objectOutputStream.writeObject(primary.myState);
					} catch (IOException excep) { System.out.println(excep.getMessage());			}
		primary.init(primary);
				}


			}
	else if(primary.colour == Colour.RED){
		primary.markerRcvd.put(channelNo, true);
	int iterator=0;
	
				while(primary.markerRcvd.get(primary.totalNeighbours[iterator]) == true && iterator<primary.totalNeighbours.length  )
				{

					iterator++;
				}
				
				if(primary.id != 0 && primary.totalNeighbours.length ==  iterator  ){
									
					primary.entry = 0;
					
					primary.myState.chnlStates = primary.chnlStates;

					primary.colour = Colour.BLUE;
					int parentId = SpanningTree.getParentId(primary.id);
				
					ObjectOutputStream objectOutputStream = primary.outputStream.get(parentId);

					try {				objectOutputStream.writeObject(primary.myState);
					} catch (Exception exec) {
						System.out.println(exec.getMessage());
					}
					primary.init(primary);
				}
				if(primary.id == 0 && primary.totalNeighbours.length == iterator ){ primary.entry = 0;
					primary.myState.chnlStates = primary.chnlStates;
			primary.colour = Colour.BLUE;			primary.stateMsgs.put(primary.id, primary.myState);
				
					
	}

	}
	}
	}

	public static void sendCompleteMessage(BeginProgram primary) {
		synchronized(primary){
			new OutputFileMaker(primary).fileWriting();
			for(int s : primary.totalNeighbours){
				CompleteMessage m = new CompleteMessage();
				ObjectOutputStream objectOutputStream = primary.outputStream.get(s);
				try {
					objectOutputStream.writeObject(m);
				} catch (IOException ex) {
					System.out.println(ex.getMessage());
				}
			}			
			System.exit(0);
		}
	}	 
	public static boolean processStateMessages(BeginProgram primary, StateMsg msg) throws InterruptedException {
		int iterator=0,q=0,r=0;
		synchronized(primary){
	
			while(r<primary.graphNodes.length && primary.graphNodes[r] == true){
				r++;
			}
			 
			if(r == primary.graphNodes.length){
				
				for(iterator=0;iterator<primary.stateMsgs.size();iterator++){
				 
			
					if(primary.stateMsgs.get(iterator).active == true){
						return true;
					}
				}
			 
				if(iterator == primary.nodeCount){
					 
					for(q=0;q<primary.nodeCount;q++){
					 
					
						StateMsg value = primary.stateMsgs.get(q);
						for(ArrayList<ApplMessage> g:value.chnlStates.values()){
							if(!g.isEmpty()){

								return true;
							}
						}
					}
				}

				if(q == primary.nodeCount){					
					sendCompleteMessage(primary);
					return false;
				}
			}
		}
		return false;
	}


	public static void frwdToParent(BeginProgram primary, StateMsg stateMsg) {
		synchronized(primary){
			int parentId = SpanningTree.getParentId(primary.id);
			ObjectOutputStream objectOutputStream = primary.outputStream.get(parentId);
			try {
				objectOutputStream.writeObject(stateMsg);
			} catch (Exception excep) {
				System.out.println(excep.getMessage());
			}
		}
	}

	
	public static void startSnapshotProtocol(BeginProgram primary) {
		synchronized(primary){
			primary.graphNodes[primary.id] = true;
			
			sendMarkerMessage(primary,primary.id);
		}
	}
}


class OutputFileMaker {
	BeginProgram primary;
	public void fileWriting() {
		String fileName = BeginProgram.resultFileName+"-"+primary.id+".out";
		synchronized(primary.output){
			try 
			{
				File file = null;
				BufferedWriter bufferedWriter= null;
				FileWriter fileWriter=null;
				file = new File(fileName);
				if(file.exists()){
					fileWriter = new FileWriter(file,true);
				}
				else
				{
					fileWriter = new FileWriter(file);
				}
				if(fileWriter !=null){
				 bufferedWriter = new BufferedWriter(fileWriter);
				}
				for(int outPutSize=0;outPutSize<primary.output.size();outPutSize++){
					for(int line:primary.output.get(outPutSize)){
						bufferedWriter.write(line+" ");
						
					}
				if(outPutSize<(primary.output.size()-1)){
	            bufferedWriter.write("\n");
					}
				}
				bufferedWriter.close();
				primary.output.clear();	}
			catch(Exception ex) {
				System.out.println(ex.getMessage());
				System.out.println("Error writing to file '" + fileName + "'");
				
}
}
}
public OutputFileMaker(BeginProgram primary) {
		this.primary = primary;
	}
}
