// PriorityQ Class
class PriorityQ {
   int Qhead;
   int Qtail;
   int Qwait;
   int Quantum;
   int longestQwait;
   int CountBurstsScheduled;
   int myID;
  
   int Qsize = 100000;
   Process[] Queue;
    
   //Constructor for new PriorityQ
   public PriorityQ(int quantum, int ID) {
      Quantum = quantum;
      Qhead = Qtail = Qwait = longestQwait = CountBurstsScheduled = 0;
      Queue = new Process[Qsize];
      myID = ID;
   }
   public void enqueue(Process process) {
      Queue[Qtail] = process;
      Queue[Qtail].assignQueue(myID);
      Qtail++;
      if (Qtail > Qsize) Qtail = 1; //wrap the index around
      CountBurstsScheduled++;
    }
	
	
   public Process dequeue() {
      int wasHead = Qhead;
      Qhead++; if (Qhead > Qsize) Qhead = 1;//wrap the index around
      return(Queue[wasHead]);
   }
   public Process giveHead() {//need and empty test
      if (Qhead != Qtail) return(Queue[Qhead]);
      else return(null);
   }
   public int giveQuant() {return Quantum;}
   public int givelongestQwait() {return longestQwait;}
   public int setlongestQwait(int wait) {return longestQwait = wait;}
   public void report() {
	   System.out.println("PriorityQ" + myID + "\t Quantum: " + Quantum
			+ "\t head: " + Qhead + "\t Tail: " + Qtail + "\t Queued: " + Qtail + "\t Wait-time: " + Qwait + "\t Ave Wait: " + "\t Longestwait: " + longestQwait + 
			"\t Bursts Scheduled: " + CountBurstsScheduled );
   }
}//end of class Process
