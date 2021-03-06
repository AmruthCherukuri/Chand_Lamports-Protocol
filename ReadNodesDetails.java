import java.io.*;

public class ReadNodesDetails {

	public static BeginProgram getMainData(String name) throws IOException{
		String fileData = null;
		String directory = System.getProperty("user.dir");
		BeginProgram mainSystem = new BeginProgram();
		String file = directory+"/"+name;
		int initialNode = 0;
		int currNodeCount = 0,note = 0;
		try {
	FileReader fileReader = new FileReader(file);
		BufferedReader fileBuffer = new BufferedReader(fileReader);
		while((fileData = fileBuffer.readLine()) != null) {
			if(!(fileData.length() != 0))
				continue;
			
				if(fileData.startsWith("#")){
					continue;
				}
				else{
					if(fileData.contains("#")){
						String[] splitIn = fileData.split("#.*$");
						String[] splitIn1 = splitIn[0].split("\\s+");
						if(splitIn1.length == 6 && note == 0){							mainSystem.sendWait = Integer.parseInt(splitIn1[3]);
						mainSystem.maxMessages = Integer.parseInt(splitIn1[5]);
					mainSystem.maxActive = Integer.parseInt(splitIn1[2]);
			mainSystem.nodeCount = Integer.parseInt(splitIn1[0]);
							mainSystem.snapShotInterval = Integer.parseInt(splitIn1[4]);
					mainSystem.minActive = Integer.parseInt(splitIn1[1]);
							note++;
					mainSystem.spanMatrix = new int[mainSystem.nodeCount][mainSystem.nodeCount];
						}
						else if(note == 1 && currNodeCount < mainSystem.nodeCount)
						{							
					mainSystem.nodes.add(new Node(Integer.parseInt(splitIn1[0]),splitIn1[1],Integer.parseInt(splitIn1[2])));						currNodeCount++;
							if(currNodeCount == mainSystem.nodeCount){
					note = 2;
							}
						}
				else if(note == 2){
				matrixEntry(splitIn1,mainSystem, initialNode);
							initialNode++;
				}					}
				else {
				String[] splitIn = fileData.split("\\s+");					if(note == 0 && splitIn.length == 6){
				mainSystem.nodeCount = Integer.parseInt(splitIn[0]);
					mainSystem.minActive = Integer.parseInt(splitIn[1]);
			mainSystem.maxActive = Integer.parseInt(splitIn[2]);
		mainSystem.sendWait = Integer.parseInt(splitIn[3]);
	mainSystem.snapShotInterval = Integer.parseInt(splitIn[4]);
mainSystem.maxMessages = Integer.parseInt(splitIn[5]);
				note++;
			mainSystem.spanMatrix = new int[mainSystem.nodeCount][mainSystem.nodeCount];
						}
				else if(note == 1 && currNodeCount < mainSystem.nodeCount)
			{
		mainSystem.nodes.add(new Node(Integer.parseInt(splitIn[0]),splitIn[1],Integer.parseInt(splitIn[2])));
	currNodeCount++;
			if(currNodeCount == mainSystem.nodeCount){
					note = 2;
				}
						}
			else if(note == 2){
			matrixEntry(splitIn,mainSystem,initialNode);
				initialNode++;
		}
		}
		}
		}
			
		fileBuffer.close();  		}
		catch(FileNotFoundException ex) {
			System.out.println("Cannot get File to Open '" +file + "'");                
		}
		catch(IOException ex) {
			System.out.println(" File Cannot be Read'" + file + "'");                  
		}
	for(int row=0;row<mainSystem.nodeCount;row++){
	for(int column=0;column<mainSystem.nodeCount;column++){
	if(mainSystem.spanMatrix[row][column] == 1){ mainSystem.spanMatrix[column][row] = 1;			}
		}
		}
	return mainSystem;
	}

	static void matrixEntry(String[] splitIn, BeginProgram mainSystem,int initialNode) {
	for(String given:splitIn){
		mainSystem.spanMatrix[initialNode][Integer.parseInt(given)] = 1;
	}
	}


}


