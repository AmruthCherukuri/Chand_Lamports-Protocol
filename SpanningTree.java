import java.util.*;


class NodeQ{
	int node;
	int nodeLevel;
	
	public NodeQ(int id, int nodeLevel) {
		this.node = id;
		this.nodeLevel = nodeLevel;
	}
}
   public class SpanningTree
   {
	
static int[] parentIds;
static 	void spanningTree(int[][] spanMatrix){
boolean[] traversed = new boolean[spanMatrix.length];
	parentIds = new int[spanMatrix.length];
	Queue<NodeQ> nodeQueue = new LinkedList<NodeQ>();
		nodeQueue.add(new NodeQ(0,0));		traversed[0] = true;
	parentIds[0] = 0;
	while(!nodeQueue.isEmpty()){
	NodeQ u = nodeQueue.remove();
		for(int i=0;i<spanMatrix[u.node].length;i++){
		if(spanMatrix[u.node][i] == 1 && traversed[i] == false){
	 traversed[i] = true;
	    nodeQueue.add(new NodeQ(i,u.nodeLevel+1));
	SpanningTree.parentIds[i] = u.node;
	}
	}
	}
	}
public static int getParentId(int id) {		return parentIds[id];}

}
