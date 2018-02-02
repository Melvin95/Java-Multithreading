package questionThree;

import javax.swing.*;
import java.util.concurrent.*;
import java.awt.*;

public class Harbor extends JPanel{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Color DEFAULT_OCEAN_COLOUR = Color.CYAN;
	
	/*Color of fork-lift and mechanic team(just a circle)
	 *Red = SLEEPING. Green = WORKING */
	private static Color FORK_LIFT_1_COLOR = Color.RED;
	private static Color FORK_LIFT_2_COLOR = Color.RED;
	private static Color FORK_LIFT_3_COLOR = Color.RED;
	private static Color MECH_TEAM_COLOR = Color.RED;
	
	private static int numberOfShips = 0;			/*Used to know how many times to "draw" a ship*/
	
	/*Variable used for movement simulation*/
	private static int yTugBoat = 300;
	private static int yShip    = 200;
	private static int xShip    = 400;
	private static int yTugBoatOut = 500;
	private static int yShipOut = 600;
	private static int xShipOut = 600;
	boolean tugOut = false;
	
	private HarborPanel harborPanel;
	
	public Harbor(){
		setLayout(new BorderLayout(15,15));
		harborPanel = new HarborPanel();
		add(harborPanel,BorderLayout.CENTER);
		
	}
	
	class HarborPanel extends JPanel{
		private static final long serialVersionUID = -1932502745906252757L;
		
		public HarborPanel() {
			setBackground(DEFAULT_OCEAN_COLOUR);

			Thread main = new mainThread();
			main.start();
		}
		
		class mainThread extends Thread{
			
			public void run(){
				IncomingTugBoat in = new IncomingTugBoat();
				in.start();
			
				ForkLift forklift = new ForkLift();
				forklift.start();
				
				MechanicTeam mech = new MechanicTeam();
				mech.start();
				
				OutTugBoat out = new OutTugBoat();
				out.start();
				
				for(int i=0 ;i<20;i++){
					Ship aShip = new Ship(i);			
					aShip.start();
					try{
						sleep(500);				/*Rate of ship entry into world*/
					}
					catch(InterruptedException e){	}
				}
			}
		}
		
		protected void paintComponent(Graphics g){
			super.paintComponent(g);
			Graphics2D g2;
			g2 = (Graphics2D)g;
		      

		    g2.setColor(Color.BLACK);
		    g2.setStroke(new BasicStroke(5));
		    
		    g2.setFont(new Font("Serif",Font.BOLD,40));
		    g2.drawString("OCEAN",(getWidth()/2),100 );
		    
		    /*Drawing basic structure of harbor*/
		    g2.drawLine(0,getHeight()/3 , (getWidth()/2)-150, getHeight()/3);
		    g2.drawLine(getWidth(),getHeight()/3 , (getWidth()/2)+150, getHeight()/3);
		    /*Incoming Lane and Outgoing Lane*/
		    g2.drawLine((getWidth()/2)-150, getHeight()/3, getWidth()/2 -150, getHeight()/3 + 200);
		    /*Seperates lanes*/
		    g2.drawLine((getWidth()/2)+5, getHeight()/3, (getWidth()/2) +5, getHeight()-200);
		    g2.drawLine((getWidth()/2) +5, getHeight()-100,(getWidth()/2)+5, getHeight());
		    /*Maintenance Lane*/
		    g2.drawLine((getWidth()/2)+150, getHeight()/3, getWidth()/2 +150, getHeight()/3 + 200);
		    //Loading Dock
		    g2.drawString("LOADING DOCK",75 , getHeight()-25);
		    //Maintenance Dock
		    g2.drawString("MAINTENANCE DOCK",getWidth()/2 +50 , getHeight()-25);
		    
		    /*Draw ship(s) out of harbor*/
		    for(int i = 0; i<numberOfShips;i++)
		    	g2.fillRect(25*(i*2),25, 30, 30);
		    
		    /*Draws ship getting tugged in*/
		    g2.fillRect(xShip,yShip, 30, 30);
		    
		    /*Draws the number of ships in the loading dock*/
		    for(int j=0;j<waitingLdock;j++)
		    	g2.fillRect(25*(j*2), getHeight()-150, 30, 30);
		    
		    /*Draws the number of ships in the maintenance dock*/
		    for(int k=1;k<waitingMdock+1;k++)
		    	g2.fillRect(700+(k*50),getHeight()-150 , 30, 30);
		    
		    /*Tug-Boats*/
		    g2.setColor(Color.BLACK);
		    g2.drawLine(getWidth()/2-115, yTugBoat, xShip, yShip);
		    g2.setColor(Color.ORANGE);		//Incoming
		    g2.fillRect(getWidth()/2 -125, yTugBoat, 20, 50);
		    g2.setColor(Color.MAGENTA);		//Outgoing
		    g2.fillRect(getWidth()/2+100, yTugBoatOut, 20, 50);
		    
		    /*Draws ship getting tugged out*/
		    if(tugOut==true){
			    g2.setColor(Color.BLACK);
			    g2.fillRect(xShipOut,yShipOut ,30 , 30);
			    g2.drawLine(xShipOut+15,yShipOut,xShipOut+15,yTugBoatOut);
		    }

		    /*Fork-Lifts*/
		    g2.setColor(FORK_LIFT_1_COLOR);
		    g2.fillArc(25, 300, 50, 50,0, 360);
		    
		    g2.setColor(FORK_LIFT_2_COLOR);
		    g2.fillArc(150, 300, 50, 50, 0, 360);
		    
		    g2.setColor(FORK_LIFT_3_COLOR);
		    g2.fillArc(250, 300, 50, 50, 0, 360);
		    
		    /*Mechanic Team*/
		    g2.setColor(MECH_TEAM_COLOR);
		    g2.fillArc(getWidth()-250,getHeight()/2,50,50,0,360);		    
		}
		
		class Ship extends Thread{
			
			int shipID;
			boolean allowedOnMdock;
			
			public Ship(int shipID){
				this.shipID = shipID;
				allowedOnMdock = false;
			}
			
			public void run(){
				try{
					numberOfShips++;	       /*There's a ship in the world*/
					repaint();				   /*Paint the ship*/
					mutex.acquire();			/*A ship requests to enter*/
					if(waitingLdock<LdockSpace){
						numberOfShips--;
						inQueue.release();
						inTugBoat.acquire();
						for(int i=0; i<10;i++){
							getTugged();
							repaint();
							sleep(200);
						}
						for(int i=0;i<10;i++){
							resetTugBoat();
							repaint();
							sleep(200);
						}
						waitingLdock++;
						repaint();
						mutex.release();
						xShip = 400;
						yShip = 200;
						mutexIn.release();   					
		/*--------------------LOADING/UNLOADING BY FORK-LIFT--------------------------------------*/
						forkQueue.release(); 			
						try{
							forkLift.acquire(); 
							sleep(20000);		/*Had to increase this because of all the sleep() for movement*/		
							determineForkLift();
							repaint();	
							forkLiftTotal.release();
						}
						catch(InterruptedException e){	}
	/*-----------------------------------------------------------------------------------------*/
							
	/*----------------------------------MAINTENANCE DOCK-------------------------------------------------*/
						while(allowedOnMdock==false){
							try{
								mutex.acquire();		
								if(waitingMdock<MdockSpace){
									waitingMdock++;
									waitingLdock--;
									repaint();
									allowedOnMdock=true;
									mutex.release();	
										
									mechQueue.release(); 
									try{
										mechTeam.acquire(); 
										sleep(5000);		
										MECH_TEAM_COLOR = Color.RED;
										repaint();
										sleep(200);
										mutexMech.release(); 
									}	
									catch(InterruptedException e){	}
								}
								else{
									mutex.release();
									sleep(1500);   
								}
							}
							catch(InterruptedException e){ }	
						}
  /*----------------------------------------------------------------------------------------------------*/
						try{
							outQueue.release(); 
							outTugBoat.acquire();
							allowedOnMdock = false;
							waitingMdock--;
							tugOut = true;
							for(int o=0;o<10;o++){
								getTuggedOut();
								repaint();
								sleep(200);
							}
							for(int q=0;q<10;q++){
								resetOut();
								repaint();
								sleep(200);
							}
							yShipOut = 600;
							tugOut = false;
							mutexOut.release(); 	
						}
						catch(InterruptedException e){	}			
					}
					else{
						numberOfShips--;
						mutex.release();
					}
						
				}
				catch(InterruptedException e){ }
			}			
			/*Ship's method to determine color of fork-lift
			 * Same logic as the one in the Fork-Lift thread except that changing it back to red here */
			public void determineForkLift(){
				if(FORK_LIFT_1_COLOR==Color.GREEN)
					FORK_LIFT_1_COLOR = Color.RED;
				else if(FORK_LIFT_2_COLOR==Color.GREEN)
					FORK_LIFT_2_COLOR = Color.RED;
				else if(FORK_LIFT_3_COLOR==Color.GREEN)
					FORK_LIFT_3_COLOR=Color.RED;
			}
			
			public void getTugged(){
				yTugBoat+=35;
				yShip += 35;
			}
			
			public void getTuggedOut(){
				yTugBoatOut -= 35;
				yShipOut -=35;
			}
			public void resetTugBoat(){
				yTugBoat-=35;
			}
			
			public void resetOut(){
				yTugBoatOut+=35;
			}
		}
		
		class IncomingTugBoat extends Thread{
			public void run(){
				while(true){
					try{
						inQueue.acquire();	
						try{
							mutexIn.acquire();
							inTugBoat.release();
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
						forkQueue.acquire();			//Check if there's ships that need work done else sleep
						try{
							forkLiftTotal.acquire();    //Check if there's fork-lift doing nothing
							determineForkLift();
							repaint();
							forkLift.release(); 		//Releases a fork-lift to work
						}
						catch(InterruptedException e){  }
					}
					catch(InterruptedException e){	}
				}
			}
			
			/* So a fork-lift is sleeping(red) AND there's work -> change it to green
			   if there's more than one fork-lift sleeping, just randomly choose one(green)
			   Impossible(assuming question 2 was right, to reach here if there's no free fork lift
			   But, just in case, used else if...*/
			public void determineForkLift(){
				if(FORK_LIFT_1_COLOR == Color.RED)
					FORK_LIFT_1_COLOR = Color.GREEN;
				else if(FORK_LIFT_2_COLOR == Color.RED)
					FORK_LIFT_2_COLOR = Color.GREEN;
				else if(FORK_LIFT_3_COLOR == Color.RED)
					FORK_LIFT_3_COLOR = Color.GREEN;
			}
		}

		
		class MechanicTeam extends Thread{
			public void run(){
				while(true){
					try{
						mechQueue.acquire();      		 //Check if there's ships in need of servicing else sleep
						try{
							mutexMech.acquire();         //One mechanic only
							MECH_TEAM_COLOR = Color.GREEN;
							repaint();
							mechTeam.release();         //Wake up mechanic team, make them ready to work
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
			
			//Mutual exclusion for shared resources
			private Semaphore mutex = new Semaphore(1,true);
			
	}

}
