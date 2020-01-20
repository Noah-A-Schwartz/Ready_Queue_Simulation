// Process Class
class Process {
   public int bursttime;
   public int remaintime;
   private String id;
   public char Ptype;
   private int Status = 0; //0=executing; 1=burst complete; 2=I/O, 9=ContextSwtchOverhead
   private int Queued = -1;
   public int ArriveT = 0;
   public int Nice = 0;
   //Constructor for new process
   public Process(String inID, int time, char type, int Queue, int clock) {
      id = inID;
      bursttime = time;
      remaintime = time;
      ArriveT = clock;
      Ptype = type;
      Status = 0;
      Queued = Queue;
      Nice = 0;
   }
   public void assignQueue(int Q) {
      Queued = Q;
      //Status = 9;  //for Context Switch Overhead within CPU
   }
   public int isQueued() {
      return Queued;
   }
   public void hello() {
      System.out.println("  " + id + " time " + bursttime + " remaintime:" + remaintime + " T:" + Ptype + " Nice: " + Nice + " Queued" + Queued);
   }
   public char type() {
      return Ptype;
   }
   public void setStatus(int status) {
      Status = status;
   }
   public int giveArriveT() {return ArriveT;}
   public int getStatus() { return Status;}
   public int getNice() { return Nice;}
   public void setNice(int nice) {   Nice = nice;   }
}//end of class Process
