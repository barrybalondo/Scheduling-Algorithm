import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Scanner;


class Node{

	public int jobid;
	Node next;

	Node(){
		next = null;
	}

	Node(int jobid){
		this.jobid = jobid;
		next = null;
	}
}

class Hash{

	Node[] table;

	Hash(int size){
		table = new Node[size];
		for (int i = 0; i < size; i++){
            table[i] = new Node();
		}
	}

	public void put(int jobParent, int job) {
		Node walker = table[job];
		Node newNode = new Node(jobParent);

		while(walker.next != null){
			walker = walker.next;
		}
		walker.next = newNode;
	}

	public int parentCount(int job) {
		Node walker = table[job].next;
		int count = 0;
		while(walker != null){
			count++;
			walker = walker.next;
		}
		return count;
	}

	public void removeParent(int job) {
		for(int i = 1; i < table.length; i++){
			Node walker = table[i];
			Node prevEntry = null;
			while(walker.next != null && walker.jobid != job){
				prevEntry = walker;
				walker = walker.next;
			}
			 if (walker.jobid == job) {
                 if (prevEntry == null)
                      table[i] = walker.next;
                 else
                      prevEntry.next = walker.next;
			 }
		}
	}

}

public class Project7 {

	static Scanner jobTimes;
	static Scanner dependencyPairs;
	static PrintWriter outFile;

	// Data structure
	static int  schedulingTable[][];     // record the schedule
	static int  openList[];              // max size is the number of total jobs
	static Hash inputDependencyGraph;    // size is the total number of distinct jobs in the graph
	static int  processJob[];            // keep track of what job is being processing
	static int  processTime[];           // keep track of the time remaining of the job
	static int  parentCount[];           // to keep track of number of parents of each job ( 0 means no parent )
	static int  jobTime[];               // to store the Job's time requirement
	static int  jobDone[];               // to keep track what jobs remain in the graph
	static int  markJob[];               // to keep track that the node is marked


	public static void main(String[] args) throws FileNotFoundException{

		//Step 0:
		if(args.length < 3){

			System.out.println("input: java Project7 Data_JobTimes.txt Data_DependencyPairs.txt Schedule.txt");
			System.exit(1);

		}

		jobTimes = new Scanner(new FileReader(args[0]));
		dependencyPairs = new Scanner(new FileReader(args[1]));
		outFile = new PrintWriter(new FileOutputStream(args[2]));

		int totalJobs = jobTimes.nextInt()+1;  // total jobs
		int sumJobTime = 0; // for scheduling table size, sum of jobs  worst case
		// initialize jobTime totalJobs + 1. array[0] empty, easier tracking
		jobTime = new int[totalJobs];

		while(jobTimes.hasNext()){
			int index = jobTimes.nextInt();
			int value = jobTimes.nextInt();
			jobTime[index] = value;
			sumJobTime += value;
		}
		jobTimes.close();

		// initialize schedulingTable, openList, jobDone, processJob, processTime
		schedulingTable = new int[totalJobs][sumJobTime]; // worst case size
		openList = new int[totalJobs];
		jobDone = new int[totalJobs];
		processJob = new int[totalJobs];
		processTime = new int[totalJobs];
		markJob = new int[totalJobs]; // mark the nodes

		// initialize inputDependencyGraph distinctJobs + 1. table[0] empty, easier tracking
		inputDependencyGraph  = new Hash(dependencyPairs.nextInt()+1);
		while(dependencyPairs.hasNext()){
			int jobParent = dependencyPairs.nextInt();
			int job = dependencyPairs.nextInt();
			inputDependencyGraph.put(jobParent, job);
		}
		dependencyPairs.close();

		// initialize parentCount and put how many parents on job
		parentCount = new int[totalJobs];
		for(int i = 1; i < parentCount.length; i++){
			parentCount[i] = inputDependencyGraph.parentCount(i);
		}

		//create schedule here
		int Time = 0;

		boolean isGraphEmtpy = false;
		int maxProcessSize = 0; 	 // just used for pretty print
		while(!isGraphEmtpy){

			isGraphEmtpy = true;

			//Step 1:
			int openListIndex = 1;
			for(int i = 1; i < parentCount.length; i++){
				if( parentCount[i] == 0 && markJob[i] != 1){
					if(openList[openListIndex] != 0){
						openListIndex++;
					}
					else{
						openList[openListIndex] = i;
						markJob[i] = 1; // mark if it is done and taken in openlist
						openListIndex++;
					}
				}
			}

			//Step 2:
			int newJob = 0;
			int availProc = 0;

			for(int i = 1; i < totalJobs; i++){
				newJob = 0;
				// newJob to the first available job in openList
				// removed from open job turn back to 0
				if( openList[i] != 0 ){
					newJob = openList[i];
					openList[i] = 0;  // remove form openList

					// finds the next available process
					for(int j = 1; j < totalJobs; j++){
						if( processJob[j] <= 0 ){
						availProc = j;
						break;
						}
					}

					processJob[availProc] = newJob; // place newJob in the processJob
					processTime[availProc] = jobTime[newJob]; // place newJobs time on processTime

					// update scehduling table under avilProc

					for(int t = Time; t < jobTime[newJob]+Time; t++ ){
						schedulingTable[availProc][t] = newJob;
					}

				}

			}

			// used for pretty print
			int count = 0;
			for(int i = 0; i < processJob.length; i++){
				if( processJob[i] > 0 )
					count++;
			}
			if(maxProcessSize < count)
				maxProcessSize = count;


			//Step 3
			outFile.println("Scheduling Table: Time: " + Time );
			outFile.println("");
			for(int p = 1; p < schedulingTable.length; p++){
				if(p <= maxProcessSize){
					if( p > 9)
						outFile.print("P" + p + "  ");
					else
						outFile.print("P" + p + "  ");

				}
				for(int t = 0; t < Time; t++ ){
						if(p <= maxProcessSize){

							if(schedulingTable[p][t] > 9 )
								outFile.print(schedulingTable[p][t] + " ");
							else
								outFile.print(schedulingTable[p][t] + "  ");
						}
				}
				if(p <= maxProcessSize){
					outFile.println("");
				}
			}

			outFile.println("");
			outFile.println("Process Job:");
			printArray(processJob, outFile);
			outFile.println("");

			outFile.println("Process Time:");
			printArray(processTime, outFile);
			outFile.println("");

			outFile.println("Parent Count:");
			printArray(parentCount, outFile);
			outFile.println("");

			outFile.println("Job Time:");
			printArray(jobTime, outFile);
			outFile.println("");

			outFile.println("Job Done:");
			printArray(jobDone, outFile);
			outFile.println("");

			//Step 4
			for(int i = 1; i < processTime.length; i++){
				if(processTime[i] != 0){
					processTime[i]--;
				}
			}
			Time++;

			//Step5
			for(int i = 1; i < processJob.length; i++){
				if(processJob[i] !=0 && processTime[i] == 0 ){
					int job = processJob[i];				 // store to update jobDone
					processJob[i] = 0; 						 // delete job from processJobs
					jobDone[job] = 1;  						 // update that job is done
					inputDependencyGraph.removeParent(job);  // delete outgoing arcs
				}

			}
			// update parentCount
			for(int i = 1; i < parentCount.length; i++){
				parentCount[i] = inputDependencyGraph.parentCount(i);
			}

			//Step 6
			outFile.println("");
			outFile.println("Scheduling Table: Time: " + Time );
			outFile.println("");
			for(int p = 1; p < schedulingTable.length; p++){
				if(p <= maxProcessSize){
					if( p > 9)
						outFile.print("P" + p + " ");
					else
						outFile.print("P" + p + "  ");

				}
				for(int t = 0; t < Time; t++ ){
						if(p <= maxProcessSize){

							if(schedulingTable[p][t] > 9 )
								outFile.print(schedulingTable[p][t] + " ");
							else
								outFile.print(schedulingTable[p][t] + "  ");
						}
				}
				if(p <= maxProcessSize){
					outFile.println("");
				}
			}

			outFile.println("");
			outFile.println("Process Job:");
			printArray(processJob, outFile);
			outFile.println("");

			outFile.println("Process Time:");
			printArray(processTime, outFile);
			outFile.println("");

			outFile.println("Parent Count:");
			printArray(parentCount, outFile);
			outFile.println("");

			outFile.println("Job Time:");
			printArray(jobTime, outFile);
			outFile.println("");

			outFile.println("Job Done:");
			printArray(jobDone, outFile);
			outFile.println("");
			outFile.println("");

			// condition to stop if graph is empty
			for(int i = 1; i < jobDone.length; i++){
				if(jobDone[i] <= 0 )
					isGraphEmtpy = false;
			}
		}
		outFile.close();
	}

	public static void printArray(int array[], PrintWriter outFile ){
		for(int i = 1; i < array.length; i++)
			outFile.print(array[i] + " " );
	}

}
