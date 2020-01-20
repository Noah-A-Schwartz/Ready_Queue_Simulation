
// MLFQ-RT & PQ-RT multicore CPU investigation  Dr. Hoganson
import java.util.Random;
import java.util.Scanner;
import java.io.*;

//import simpackage.class;

public class PQSimV1a {
 public static void main (String[] args) throws Exception {  //added throws Exception for print redirection
   int error=0;      //0=no error, anything else error
   int clock = 0;    //The simulation clock 
   int maxtime = 0;  //Duration of simulation in ms
   int CPUs = 1;     //number of CPUs in a multicore
   int ProcIDs = 0;  //Each processor has an ID number
   int RTcount=0;    //Counting the number of RT bursts created
   int NRTcount=0;   //Counting the number of NRT bursts created
   int Queues;       //Number of queues in MLFQ system
   int SchedStrat = 0;
   int Preemption = 0;  //0 means no preemption, 1 means preempt NRT for RT
   int BaseQuant = 0;  int RTQuant=0;
   int CPUstatus = 0;  //return from CPU tick:  0=more execution, 1=complete
   int Mitigation = 0;  //0 means no mitigation
   int completed = 0;      //count of bursts completed
   int TotCS = 0;          //total context switches returned from CPUs, from new bursts assigned
   int TOTworked = 0;
   int TOTidle = 0;
   int NiceLimit = 1;
   int PQBurstLimit = 10;  //The number of bursts schedule in a queue, before going to the next queue, same for all queues but Q0 & Q1
   int CSOverhead = -2;     //the number of clock ticks consumed doing CS overhead
   /*public static*/boolean verbose = false; //default is max trace messages
   String filename = new String();
   Process newProc;
   Scanner input = new Scanner(System.in);
   Random rand = new Random();

   
   //System.out.print("MLFQRTv1a: Trace Verbosity:");
   //verbose = input.nextInt();
   System.out.print("\nPQSched: PQueue with Real Time Simulation");
   System.out.print("Output filename (just return for none): .txt:  ");
   filename = input.nextLine();  
         System.out.println(" ");   
   System.out.print("Enter millisecond duration for simulation ");
   maxtime = input.nextInt();
   System.out.print("Context Switch Overhead time (cycles), 0=none: ");
   CSOverhead = input.nextInt();   CSOverhead = -CSOverhead; //for counting neg up to 0 process status
         System.out.println(" CONFIGURE CPUS OR CORES");  
   System.out.print("Number of CPUs: ");
   CPUs = input.nextInt();
   CPU CPUref[] = new CPU[CPUs];  //create array of cores or CPUs
   for (int i=0; i< CPUs; i++)
      CPUref[i] = new CPU("CPU" + i, CSOverhead);
      
   System.out.println(" CONFIGURE Priority Queue w/RT SYSTEM");          
   System.out.print("Number of PQSched (1or2 min) ");
   Queues = input.nextInt();   
   System.out.print("Enter RT Queue[0] scheduling time quantum millisec: ");
   RTQuant = input.nextInt();
   System.out.print("Enter base PriorityQ1 scheduling time quantum millisec: ");
   BaseQuant = input.nextInt();
   System.out.print("Enter Nice Value to trip up Priority (suggest 2): ");
   NiceLimit = input.nextInt();
   System.out.print("Enter Number of Bursts to schedule (same) in each priority(Q) (Eff.upto#CPUs): ");
   PQBurstLimit = input.nextInt();
   System.out.print("Pre-emption interval (clock ticks), pre-empt Q2... (0 means none): ");
   Preemption = input.nextInt();
   PQScheduler PQSched = new PQScheduler(Queues, BaseQuant, RTQuant, NiceLimit); //create a new PQSched object

         System.out.println("\n PQSched SCHEDULING MITIGATION PARAMETERS");   
   System.out.print("PQSched: RT Scheduling Strategy, # of CPUS reserved for RT (0=none reserved; 1=CPU0 reserved for RT; 2=CPU 0&1 for RT...):"); 
   SchedStrat = input.nextInt();
   System.out.print("Mitigation strategy (0==no starvation mitigation, 1+==mitigation frequency):"); //0=no mitigation, clock mod Mitigation is frequency, higher is fewer, =1means every time
   Mitigation = input.nextInt();   
         System.out.println("\n WORKLOAD PARAMETERS");  
   System.out.print("Enter Workload Sine Wave Variable Period (0-999) ms (suggest 100) ");
   int SineWave = input.nextInt(); 
   System.out.print("Enter RT process arrival probability (0-999) ms ");
   int RTProb = input.nextInt(); 
   System.out.print("Enter RT process, uniform dist range (1-X) ms: ");
   int RTSize = input.nextInt(); 
   System.out.print("Enter Non-RT Process arrival prob (0-999) in ms ");
   int ARProb = input.nextInt(); 
   double NRTProb = ARProb; ARProb--; // so that alternator will start high
   System.out.print("Enter Non-RT process, uniform dist range (1-X) ms: ");
   int NRTSize = input.nextInt(); 
   
      System.out.println("\n\n****************     Simulation Run Begins     *************************\n"); 
      
      if (filename != null && !filename.isEmpty()) {
         System.setOut(new PrintStream(new FileOutputStream("PQSimV1aTrace.txt"))); //added for print redirection to trace file
         // Echo the input parameters for record in the output trace file
         System.out.println("PQSched: Scheduling Strategy (0,1):" + SchedStrat);
         System.out.println("Mitigation strategy (0,1):" + Mitigation);
         System.out.println("Base Q0 RT scheduling time quantum:" + RTQuant);
         System.out.println("Base Q1 scheduling time quantum:" + BaseQuant);
         System.out.println("Nice Limit to trip Priority:" + NiceLimit);
         System.out.println("Millisecond duration for simulation " + maxtime);
         System.out.println("Number of CPUs: " + CPUs);
         System.out.println("Number of PQSched (1 min) " + Queues);
         System.out.println("Workload Sine Wave Variable Period (0-999) " + SineWave);
         System.out.println("RT process arrival probability (0-999) " + RTProb);
         System.out.println("RT process uniform dist range (1-X):" + RTSize);
         System.out.println("Non-RT Process arrival prob (0-999) in ms " + ARProb);
         System.out.println("Non-RT process uniform dist range (1-X):" + NRTSize);
      }
   
   // Begin main loop executing until the clock expires
   System.out.println("\n\n\n****** Run Starting ***************");
   while (error==0 && clock < maxtime) {  //This is the main simulation loop
   
      //Check for workload variability for NRT Arrival, every 'SineWave' either up or down from baseline
      //Process arrival rate may change over time, this cycles arrival rates
      //a complete "wave" is 4 quadrants 'SineWave': Up, Baseline, Down, Baseline
      //ARProb is the variable to cycle around parameter NRTProb
      if (SineWave > 0)
         if (clock%SineWave == 0) {//check every ms to see if should vary (every Sinewave interval), up or down
            if (ARProb+1 == NRTProb) ARProb = (int)(NRTProb*0.8);  //ARP is -1, so going down   //*or+??
            else if (ARProb+1 < NRTProb) ARProb = (int)(NRTProb+1);//is down, so going up
            else if (ARProb-1 == NRTProb) ARProb = (int)(NRTProb*1.2);//ARP is +1, so going up
            else if (ARProb-1 > NRTProb) ARProb = (int)(NRTProb-1); //is up, so go back down
         }//sine wave variability

      //check for new arrivals
      System.out.println("\n>>> Clock is " + clock + "  Checking for new RT bursts");
      newProc = RTArrive(ProcIDs, rand, RTProb, RTSize, clock);  //returns either a valid process (R or N)or null
      if (newProc != null) { //check for new RT bursts
/*!*/          PQSched.newProcess(newProc);   //One of two calls to MLFQ to add new RT bursts
         ProcIDs++;
         RTcount++;
      }
      System.out.println("  > Clock is " + clock + "  Checking for new NRT bursts");
      newProc = NRTArrive(ProcIDs, rand, ARProb, NRTSize, clock);  //returns either a valid process (R or N)or null
      if (newProc != null) { //check for new NRT bursts
/*!*/         PQSched.newProcess(newProc);   //Firs of two calls to MLFQ to add new NRT bursts
         ProcIDs++;
         NRTcount++;
         //System.out.println("CS so far: " + TotCS);
      }

      //schedule bursts in queues to processors
      System.out.println("  > Clock is " + clock + "   scheduling");
/*!*/    PQSched.schedule(CPUref, CPUs, SchedStrat, Preemption, clock, Mitigation, PQBurstLimit);   //One of two calls to PQSched to schedule bursts
      //System.out.println("CS so far: " + TotCS);

      //let CPUs run a clock tick
      System.out.println("  > Clock is " + clock + "   clock ticks for CPUs");
      for (int i=0; i<CPUs; i++) {
         CPUstatus = CPUref[i].CPUtick(clock);
      } //endfor clock ticks
      if (CPUstatus == 2) System.out.println("!Qquantum exceeded");

      //reschedule bursts in queues, dropping down bursts who finished allocation without completing burst
      System.out.println("  > Clock is " + clock + "   Rescheduling Check for drop down");  //second of two calls to PQSched to reschedule bursts
/*!*/    PQSched.reschedule(CPUref, CPUs, SchedStrat, clock);
      clock++;
      //System.out.println("CS so far: " + TotCS);
  
   
   }//end of main simulation loop while
   
   if (filename != null && !filename.isEmpty())  System.setOut(new PrintStream(new FileOutputStream(filename))); //added for print redirection to trace file
   System.out.println("***************** Run Stats from PQSched.  Times are in milliseconds **********************");
   System.out.println("Run Duration:" + maxtime + " CPUs/Cores:" + CPUs + "  Queues:" + Queues +
      "    SchedStrat:" + SchedStrat + "    Preemption:" + Preemption + "   Mitigation:" + Mitigation + "   SineWaveVar:"  + SineWave);
   System.out.println("  \tNiceLimit:" + NiceLimit + "  \tPQBurstLimit:" + PQBurstLimit + "    ContSwOverhead:" + -CSOverhead);
   System.out.print("Non-RT arrival prob:" + NRTProb + "  Size:" + NRTSize);
   System.out.printf("\t  CPUs(" + (CPUs-1) + ") Cap:%%%.2f",   (float)(  (  (float)(((float)NRTProb/1000)*maxtime) * ((float)(1+NRTSize)/2)  )/(maxtime*(CPUs-1))  )*100 );
   System.out.print("\t RT arrival prob:" + RTProb + "  Size:" + RTSize);
   System.out.printf("\t  CPU(1)Cap:%%%.2f",   (float)(  (  (float)(((float)RTProb/1000)*maxtime) * ((float)(1+RTSize)/2)  )/maxtime  )*100 );
   System.out.printf("\t  TotCPUCap:%%%.2f", ((float)(((float)NRTProb/1000)*maxtime) * ((float)(1+NRTSize)/2) 
                                           +  (float)(((float)RTProb/1000)*maxtime) * ((float)(1+RTSize)/2)  ) /(maxtime*CPUs)   *100    );
   System.out.println();
   System.out.print("Processes created:" + ProcIDs + "    RT=" + RTcount + "    NRT=" + NRTcount);
   System.out.printf("\t\tAve RTwait=%.2f", ((float)PQSched.RTwait/RTcount) );
   System.out.printf("\t\tAve NRTwait=%.2f", ((float)PQSched.NRTwait/NRTcount) );
   System.out.println();
   System.out.println("***** PQSched Reporting *****");
   PQSched.report();
    System.out.println("***** CPUs Reporting *****");
   for (int i=0; i<CPUs; i++) {
      completed += CPUref[i].report();
      TotCS += CPUref[i].GiveCS();
      TOTworked += CPUref[i].GiveCPUWorked();
      TOTidle += CPUref[i].GiveCPUIdle();
   }
   System.out.println("Total Processes Completed:" + completed + "(+ number queued + number in processors) == Processes created: " + ProcIDs );
   System.out.print("Total CS Overhead time:  " + TotCS);
   if (ProcIDs > 0) System.out.println("\t   Average CS per process= " +  (float) TotCS/ProcIDs);
   System.out.println("Total CPU worked time:  " + TOTworked);
   System.out.println("Total CPU idle time:    " + TOTidle);
   System.out.println("Total of CPU times:     " + (TotCS + TOTworked + TOTidle) + "\tOverall CPU Util: " + (float) TOTworked/(TotCS + TOTworked + TOTidle) );
 }//end of main

// returns a Process reference, if null then no process created this clock tick
// may create a new process and return it according to prob distribution (incomplete?)
public static Process RTArrive (int ProcID, Random rand, int RTProb, int RTSize, int clock) {
   int Rand = rand.nextInt(1000);
   Process newone = null;
   if (Rand < RTProb) newone = new Process("Proc" + ProcID, (rand.nextInt(RTSize)+1),'R',0, clock);
   return newone;
} 
public static Process NRTArrive (int ProcID, Random rand, int ARProb, int NRTSize, int clock) {
   int Rand = rand.nextInt(1000);
   Process newone = null;
   if (Rand < ARProb) newone = new Process("Proc" + ProcID, (rand.nextInt(NRTSize)+1),'N',1, clock); //Good
   return newone;
} 

}//end of class


