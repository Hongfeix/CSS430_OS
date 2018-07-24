/*
* @file: Shell.java
* @author: Hongfei xie 
* @description: read a sequence of command and execute them based on ; and &.
*  ";" asynchronize execute; "&" synchronize execute 
*/
class Shell extends Thread
{
	private int processCount;
	private String[] command;

	public Shell(){
		processCount = 1;
		SysLib.cout("Shell process made by Hongfei Xie\n");
	  command = null;
	}

	public void run(){
		boolean isExecuted = false; // this variable to check if the shell actully make a new thread
																// if all commands invalid, then isExecuted is false
		exitLoop:  // set a break point for exit shell														
		
		while(true){ // when not hit the "exit" command
			SysLib.cout("Shell[" + processCount + "]% ");
			StringBuffer buffer = new StringBuffer();
			SysLib.cin(buffer);
			
			
			try{
				command = SysLib.stringToArgs(buffer.toString()); // read whole command
			}
			catch (Exception e){
				SysLib.cout("Can't read command, exit shell\n");
				break;
			}
			
			if (command.length == 0){  // if nothing input, continue loop
				//SysLib.cout("No input\n");
				continue;
			}	
				  
			if (command[0].contentEquals("exit") || command[0].contentEquals("Exit")){ // exit shell
				SysLib.cout("Exiting shell\n"); 
				break exitLoop;
			}
			
			else{ // "valid" statment
			
				String[] semi = buffer.toString().split(";");
				String asynchron;
				/*
				* CASE 1: Only ; statement 
				*/
				for (int j = 0; j < semi.length; j++){ // asynchronize action ;
					asynchron = semi[j];
					if (asynchron.length() == 0 || asynchron.trim().length() == 0){ // check if the command is only made by space 
						continue;
					}	
					/*
					* CASE 2: Contain & inside ;
					*/
					if (asynchron.indexOf("&") != -1) { // check if asynchronize contains synchronize command  
						int counter = 0;  // counter to count synchronize threads
						for (String synchron:asynchron.split("&")){ // for case contain multi &
							if (synchron.length() == 0 || synchron.trim().length() == 0){ // check if the command is only made by space 
								continue;
							}
							
							
							if (asynchron.indexOf("exit") > 0 || asynchron.indexOf("Exit") > 0){ // exit shell
								SysLib.cout("Exiting shell\n"); 
								break exitLoop;
							}
							
							int pidSyn = SysLib.exec(SysLib.stringToArgs(synchron)); // execute synchronize call
							if (pidSyn == -1) { // invalid command
								SysLib.cout("Can't execute command: " + synchron + "\n"); 
							}
							else{ // valid command, then increase thread counter
								isExecuted = true;
								counter++;							
							}
						}
						
						/** This section is for the case there is a "&" at the end of command **/
						// When there is a & at very last statment, then synchrony create a new Shell process
						if ( (j == (semi.length - 1)) && (asynchron.charAt(asynchron.length() - 1) == '&') ){
							processCount++; // increase shell number
							run(); // create another shell
							return;  // quit current shell
						}
						/****************************************************************************/
						else{
							for (int i = 0; i < counter; i++){  // wait for all valid commands finish
			 					SysLib.join();  
			 				}
			 			}
					}
					
					else{ // execute the asynchronize call 
						if (asynchron.indexOf("exit") > 0 || asynchron.indexOf("Exit") > 0){ // exit shell
								SysLib.cout("Exiting shell\n"); 
								break exitLoop;
						}
						
						int pidAsyn = SysLib.exec(SysLib.stringToArgs(asynchron));
						if (pidAsyn == -1){ // invalid command
							SysLib.cout("Can't execute command: " + asynchron + "\n");
						}
						else{ // valid command
							isExecuted = true;
							while(SysLib.join() != pidAsyn){ // Wait for the current procees done 
								continue;
							}
						}
					}	
				}	
				
				if (isExecuted == true)	{ // increase process(shell) by 1 only a thread executed
					processCount++; 
				}
				
			} // end of else  	
		}// end of while
	SysLib.sync();
	SysLib.exit();
	}// end of run
}// end of shell
	
