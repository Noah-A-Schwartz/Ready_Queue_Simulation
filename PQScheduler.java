//Noah Schwartz
//CS3502/02
//Simulation Phase 1

import java.util.ArrayList;
//Not sure if everything in queue is being completed?
class PQScheduler{
	private int queues; //# of q's
	private int basequant; //base quantum
	private int RTquant; // Real time quantum
	private int NiceLimit;
	public int RTwait = 0;
	public int NRTwait = 0;
	ArrayList<PriorityQ> PQueues; //holdss priority queues
	
	public PQScheduler(int queues, int basequant, int RTquant, int NiceLimit){
		this.queues = queues;
		this.basequant = basequant;
		this.RTquant = RTquant;
		this.NiceLimit = NiceLimit;
		PQueues = new ArrayList<PriorityQ>(queues); 	//ArrayList containing the different Quesus(size = number of queues)
		for (int i=0; i<queues; i++){		            //loop to add the queues to the ArrayList
		if (i == 0)
			 PQueues.add(new PriorityQ(RTquant, i + 1));
		else {
			PQueues.add(new PriorityQ(basequant, i + 1));
			basequant *= 2;
		}
		
	}
}
	
	public void newProcess(Process newProc){	
	
		//Adds a new RT process to the queue 0
		if(newProc.type() == 'R')														
		PQueues.get(0).enqueue(newProc); 
		//Adds new NRT process to queue 1
		else PQueues.get(1).enqueue(newProc);	
		
	 }
	//0=executing; 1=burst complete; 2=I/O, 9=ContextSwtchOverhead //adds next in queue to first avaiable processor 
	public void schedule(CPU[] CPUref, int CPUs, int SchedStrat, int Preemption, int clock, int Mitigation, int PQBurstLimit){//Schedule the next process to send to the processor
	
			//schedule RT first
			for(int i = 0; i < CPUs; i++){
				if(CPUref[i].empty() && PQueues.get(0).giveHead() != null) //if CPU empty and there is a job to add
					CPUref[i].newProcess(PQueues.get(0).dequeue(), PQueues.get(0).giveQuant()); //Add next RT process in queue 0 to empty CPU
			}
			
			//NRT queue scheduling
			for(int i = 1; i < PQueues.size(); i++){//Loops through each priority Q, if queue is empty, goes to next queue
				if(PQueues.get(i).giveHead() == null)//no jobs in queue
					continue; //go to next queue
				
			    for (int j = 0; j < CPUs; j++){
				 if(CPUref[j].empty() && PQueues.get(0).giveHead() != null)//If CPU empty
					CPUref[j].newProcess(PQueues.get(i).dequeue(), PQueues.get(i).giveQuant());//Add first NRT process from queue 1 to n to empty CPU
				}
			}
		}			   		   
					
	  
			   
	 
	 
	//reschedule processes that have completed their quantum (RT back to queue 0, NRT drop to next Queue, //0=executing; 1=burst complete; 2=I/O, 9=ContextSwtchOverhead 
	public void reschedule(CPU[] CPUref, int CPUs, int SchedStrat, int clock){
		for (int i = 0; i < CPUs; i++){//traverse through the CPUs
	   //check if CPU has process, add From RT Q if it doesnt
			if(CPUref[i].empty())
				CPUref[i].newProcess(PQueues.get(0).dequeue(), PQueues.get(0).giveQuant()); //quant of process depends on what queue it is in
	   
	   //Check if process type is RT, if quantum is complete, add back to q and add next, or continue executing
	   if(CPUref[i].CPUgiveP() != null){//make sure CPU has process before trying to reschedule
			if(CPUref[i].CPUgiveP().type() == 'R' && CPUref[i].CPUgiveP().getStatus() == 2){ //Process needs to be added back to Q, add new job from RT Q
			    PQueues.get(0).enqueue(CPUref[i].CPUgiveP()); //add back to q1
			    if(PQueues.get(0).giveHead() != null)
					CPUref[i].newProcess(PQueues.get(0).dequeue(), PQueues.get(0).giveQuant()); //dequeue the next process from RT queue and add to processor, if RT q empty, take from q2
				else CPUref[i].newProcess(PQueues.get(1).dequeue(), PQueues.get(1).giveQuant()); 
			
			  //else process is still executing and will stay in processor(do nothing)
	   } 
		//else is NRT	  
			 else{
				if(CPUref[i].CPUgiveP().getStatus() == 2) //if quantum complete
					PQueues.get(1).enqueue(CPUref[i].CPUgiveP()); //add back to q1
				if(PQueues.get(0).giveHead() != null)
					CPUref[i].newProcess(PQueues.get(0).dequeue(), PQueues.get(0).giveQuant()); //dequeue the next process from RT queue and add to processor, if RT q empty, take from q2
				else CPUref[i].newProcess(PQueues.get(1).dequeue(), PQueues.get(1).giveQuant());
			 }	
				
			
		
	   }
					
		}
	}
	
	 
	
	public void report() //calls report methods in PrioroityQ for each queue//dont know if i needed to complete this
	{
		for (int i = 0; i < PQueues.size(); i++){
			PQueues.get(i).report();
		
	}
}
	}

	