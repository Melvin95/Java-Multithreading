package questionTwo;

import java.util.concurrent.Semaphore;

/* There's a huge assumption here, that the incoming tug-boat isn't working like a queue
 * So if the harbor is full(actually loading dock full) then the ship doesn't enter harbor and just leaves
 * It isn't going to sleep, it just terminates
 */
public class Main extends Thread{

/*------------------------------------MAIN METHOD---------------------------------------------------------*/
	public static void main(String[] args) {
		
		Main main = new Main();
		main.start();

	}
	public void run(){
		
		IncomingTugBoat in = new IncomingTugBoat();
		in.start();
		
		ForkLift forklift = new ForkLift();
		forklift.start();
		
		MechanicTeam mech = new MechanicTeam();
		mech.start();
		
		OutTugBoat out = new OutTugBoat();
		out.start();
		
		for(int i=1 ;i<13;i++){
			Ship aShip = new Ship(i);			
			aShip.start();
			try{
				/*Vary this to vary how many ships enter harbor a time
				 *Short time: probably 1st five ships will get in and stop others from entering
				 *because loading dock will be full(fork-lifts take time to do work)
				 *Long time: all ships should enter and exit harbor
				 */
				sleep(800);		
			}
			catch(InterruptedException e){	}
		}
	}
/*------------------------------------------------------------------------------------------------------------------------------------*/
	
	class Ship extends Thread{
		
		int shipID;
		boolean allowedOnMdock;
		
		public Ship(int shipID){
			this.shipID = shipID;
			allowedOnMdock = false;
		}
		
		public void run(){
				try{
					System.out.println("Ship "+shipID+" requests to enter harbor");
					mutex.acquire();            //One ship at a time
					if(waitingLdock<LdockSpace){
						inQueue.release();
						inTugBoat.acquire();	//Wait for incoming-tug boat
						sleep(200);				//Time taken to tug in...
						System.out.println("Ship "+shipID+" enters harbor, goes to loading dock");
						waitingLdock++;
						mutex.release();
						mutexIn.release();    //Allow other ships to use tug-boat(frees up tug boat)
						
						
						/*--------------------LOADING/UNLOADING BY FORK-LIFT--------------------------------------*/
						forkQueue.release(); 			//Notify fork-lift that there's work to do
						try{
							forkLift.acquire(); 		//Wait for available fork-lift
							sleep(3000);				//Time taken to do work
							System.out.println("Ship "+shipID+" is now loaded/unloaded");
							forkLiftTotal.release();	//This fork-lift finish do work, release it
						}
						catch(InterruptedException e){	}
						/*-----------------------------------------------------------------------------------------*/
						
						/*----------------------------------MAINTENANCE DOCK-------------------------------------------------*/
						while(allowedOnMdock==false){
							try{
								mutex.acquire();		//Lane to maintenance dock can only take one ship at a time
								if(waitingMdock<MdockSpace){
									System.out.println("Ship "+shipID+" is now on maintenance dock");
									waitingMdock++;
									waitingLdock--;
									allowedOnMdock=true;
									mutex.release();	//Allow other ships to enter lane
									
									mechQueue.release(); //Signal to mechanic team that work to be done
									try{
										mechTeam.acquire(); //Wait for mechanic team
										sleep(1500);		//Time taken to do work
										System.out.println("Ship "+shipID+" is now serviced");
										mutexMech.release(); //Allow other ships to be serviced
									}	
									catch(InterruptedException e){	}
								}
								else{
									System.out.println("Ship "+shipID+" waits on loading dock, no space on maintenace dock");
									mutex.release();
									sleep(1500);       //Ship waits at loading dock, it will check for space later
								}
							}
							catch(InterruptedException e){ }	
						}
					  /*----------------------------------------------------------------------------------------------------*/
						
						
						try{
							System.out.println("Ship "+shipID+" wants to leave now, waiting for tug-boat");
							outQueue.release(); //Signal to outgoing tug boat to fetch ship
							outTugBoat.acquire(); //Wait for available tug-boat 
							sleep(200);			  //Time taken to tug-out
							System.out.println("Ship "+shipID+" has left the harbor");
							allowedOnMdock = false;
							waitingMdock--;
							mutexOut.release();  //Allow other ships to be tugged-out	
						}
						catch(InterruptedException e){	}
						
					}
					else{
						//No space on loading dock, ship leaves
						System.out.println("NO SPACE ON HARBOUR FOR SHIP "+shipID+". Ship passing harbour");
						mutex.release();
					}
					
				}
				catch(InterruptedException e){ }
		}
		
	}
	
	class IncomingTugBoat extends Thread{
		public void run(){
			while(true){
					try{
						inQueue.acquire();		//Checks if there's ships needing to be pulled in	
						try{
							mutexIn.acquire(); //Only one tug-boat
							System.out.println("Tug-boat coming to pull in ship");
							inTugBoat.release();//Releases tug-boat to pull in
						}
						catch(InterruptedException e){	}

					}
				catch(InterruptedException e){	}
			}		
		}
	}
	
	class ForkLift extends Thread{

		public void run(){
			while(true){
				try{
					forkQueue.acquire();		//Check if there's ships that need work done else sleep
					try{
						forkLiftTotal.acquire();    //Check if there's fork-lift doing nothing
						System.out.println("Fork Lift ready to work on ship ");
						forkLift.release(); 		//Releases a fork-lift to work
					}
					catch(InterruptedException e){  }
				}
				catch(InterruptedException e){	}
			}
		}
	}
	
	class MechanicTeam extends Thread{
		
		public void run(){
			while(true){
				try{
					mechQueue.acquire();      		 //Check if there's ships in need of servicing else sleep
					try{
						mutexMech.acquire();       //One mechanic only
						System.out.println("Mechanic team is ready to work on ship ");
						mechTeam.release();       //Wake up mechanic team, make them ready to work
					}
					catch(InterruptedException e){ }

				}
				catch(InterruptedException e){}
			}
		}
	}
	
	class OutTugBoat extends Thread{
		public void run(){
			while(true){
				try{
					outQueue.acquire();		//Checks if there's ships wanting to leave
					try{
						mutexOut.acquire();	//Wait for available outgoing-tug boat
						System.out.println("Outgoing Tug-Boat pulling ship out...");
						outTugBoat.release(); //Releases outgoing tug-boat to work
					}
					catch(InterruptedException e){	}
				}
				catch(InterruptedException e){ }
			}
		}
	}
	//Maximum space on loading and maintenance docks
	private final int LdockSpace = 5;
	private final int MdockSpace = 3;
	
	//Number of ships on loading and maintenance docks at any given time
	private int waitingLdock = 0;
	private int waitingMdock = 0;
	
	/*--------------------------INCOMINNG TUG-BOAT-----------------------------------------------*/
	//Semaphore for that queues incoming ships
	private Semaphore inQueue = new Semaphore(0,true);
	
	//Semaphore representing the incoming-tug boat, whether it's available to pull in or not
	private Semaphore inTugBoat = new Semaphore(0,true);
	
	//Mutual exclusion semaphore for incoming tug-boat
	private Semaphore mutexIn = new Semaphore(1,true);
	
	/*-------------------FORK LIFTS----------------------------*/
	//Number of ships waiting to be loaded/unloaded by a fork-lift
	private Semaphore forkQueue = new Semaphore(0,true);

	//Represents a fork-lift doing work
	private Semaphore forkLift = new Semaphore(0,true);
	
	//Represents total number of fork-lifts available
	private Semaphore forkLiftTotal = new Semaphore(3,true);
		
	/*-------------------Mechanical Team--------------------------*/
	//Number of ships needing servicing from the mechanic team
	private Semaphore mechQueue = new Semaphore(0,true);
	
	//Represents the mechanic team, whether it's available or not
	private Semaphore mechTeam = new Semaphore(0,true);
	
	//Mutual exclusion for mechanic team
	private Semaphore mutexMech = new Semaphore(1,true);

   /*-----------------OUTGOING TUG-BOAT----------------------------*/
	//Signals if there's work for outgoing tug-boat
	private Semaphore outQueue = new Semaphore(0,true);
	
	//Represents outgoing tug-boat available or not
	private Semaphore outTugBoat = new Semaphore(0,true);
	
	//Mutual exclusion for outgoing tug-boat, there's only one
	private Semaphore mutexOut = new Semaphore(1,true);
	
	
	private Semaphore mutex = new Semaphore(1,true);
	
}
