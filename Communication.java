import java.util.*;
import java.io.*; 

@SuppressWarnings("serial")
public class Communication implements Serializable {
	BeginProgram m = new BeginProgram();
	int n = m.nodeCount;
}

@SuppressWarnings("serial")
class StateMsg extends Communication implements Serializable{
	int[] vectorClock;
	HashMap<Integer,ArrayList<ApplMessage>> chnlStates;
	boolean active;
	int nodeId;
	
}

@SuppressWarnings("serial")
class MarkerMsg extends Communication implements Serializable{
	int nodeId;
	String msg = "mark";
}

@SuppressWarnings("serial")

class ApplMessage extends Communication implements Serializable{
	int[] vectorClock;
	int nodeId;
	String msg = "Bye";
}

@SuppressWarnings("serial")
class CompleteMessage extends Communication implements Serializable{
	String msg = "finish";
}