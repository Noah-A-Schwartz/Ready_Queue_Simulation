/* CPU class */
class CPU {
   private String id;
   private int countdown=0;
   private Process process;
   private int RTcompleted = 0;        //this CPUs RT completed
   private int NRTcompleted = 0;       //this CPUs NRT completed
   private int worked = 0;
   private int CPUidle = 0;
   private int Qquantum = 0;
   private int PContSwOverhead = 0;
   private int Assigned_P_CS = 0;   //counter for new process to CPU == context switch
   private int CPUCS = 0;           //counter for Context switch overhead time
   
   //OBJECT CONSTRUCTOR
   public CPU(String myname, int Overhead) {//constructor
      id = myname;
      PContSwOverhead = Overhead;
   }
   
   //METHODS
   public int CPUtick(int clocktick) {
      if ((process != null) && (process.getStatus() == 0)) {
         countdown--;
         process.remaintime--;  //one MS of burst completed
         Qquantum--; //MLFQ allocated quantum allowed for scheduling
         worked++; //counting MS CPU was busy working.  Idle time is duration minus worked
         process.hello(); //for trace output status check of process
         if (countdown == 0) {
            if (process.type() == 'R') RTcompleted++;
            else NRTcompleted++;
            process.setStatus(1); 
         }//endif coundown
         else if (Qquantum == 0) {
            process.setStatus(2);  //Quantum complete, but not burst, need to drop down a Q
         }
         if (countdown < 0) System.out.println("LT zero!!:");  //error detection/checking, should not happen
       }//endif process != Null
       else if ((process != null) && (process.getStatus() < 0)) { 
         CPUCS++;                   //count CPU CS overhead time
         process.setStatus(process.getStatus() +1);      //decrease CS remaining overhead time for process
       }
       else { CPUidle++;}  //count idle.  Idel+worked = simduration
       if (process == null) return -1;
       else return process.getStatus();
   }//end of method CPUtick
   
   public void newProcess(Process newp, int Qquant) {  //process gets a new CPU, which can run just one at a time
      if (newp!=null) {System.out.print("      " + id + " Scheduling =="); newp.hello();
         process = newp;
         countdown = newp.remaintime;//newp.bursttime;
         Qquantum = Qquant;
         Assigned_P_CS++;  //add one to count of new processes assigned to this CPU (context switches)
         process.setStatus(PContSwOverhead);
      }
      else {//System.out.print("      " + id + "null");
         process = null;
         countdown = 0;
         Qquantum = 0;
      }
      //System.out.println("countdown =" + countdown);
   }//end of newProcess
   
   public boolean empty() { 
      if (process == null)  return true;
      else return (process.getStatus() == 1); //process comleted, show as empty OK to reschedule
   }
   
   public boolean Qexceeded() {
      if ((process != null) && (process.getStatus() == 2)) return true;
      else return false;
   }
   
   public int CPUPstatus() {
      if (process != null) return process.getStatus();
      else return -1;
   }
   
   public Process CPUgiveP() {
      return process;
   }
   
   public int GiveCS() { //return Assigned_P_CS; }
      return CPUCS;}
   public int GiveCPUWorked() { //return Assigned_P_CS; }
      return worked;}
   public int GiveCPUIdle() { //return Assigned_P_CS; }
      return CPUidle;}
   public int report() { 
      if (process !=null) System.out.print(id + "(p=" + process.getStatus() + ")"); //if not null then working on one proces
      else System.out.print(id + "(" + "nll" + ")");
      System.out.print("  RTCompeted:" + RTcompleted + "\t\t   NRTcompleted:" + NRTcompleted + 
         "\t\tTotComp:" + (RTcompleted+NRTcompleted) + "\t\tCPUidle:" + CPUidle + "\t\tWorked:" + worked);
      System.out.printf("\t\tUtil%%%.2f", ((float)worked/(worked+CPUidle)*100) ); 
      System.out.println("\tNewPContextSws: " + Assigned_P_CS + "\t\tCSOverhead: " + CPUCS);
      return (RTcompleted + NRTcompleted);
   }
}//end of class 

