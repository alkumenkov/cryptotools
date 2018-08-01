package com.senatrex.dbasecollector.queues;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * <p>
 * class creates Thread safe queue <br>
 * updated 24 авг. 2015 г.14:50:22
 *  </p>
 */
public class TAsyncLogQueue{
	
	private static TAsyncLogQueue fAsyncQueue = null;
	private  Queue< String >  fQueue= null;
	private long fTimeFromInitialize = 0L;
	private  boolean fTerminate = false;
	private  Object fWaitObject = new Object();
	private  Thread fThread = null;
	private  static String fLogFilePath = "log.txt";
	private  int fSizeQueue;
        
        public static void SetLogPath( String aLogPath ){
            fLogFilePath = aLogPath;
        }
	
	private TAsyncLogQueue(  ) {
		fSizeQueue = 0;
		System.out.println( System.nanoTime( ) );		
		System.out.println( "constructor started" );
		fQueue = new ConcurrentLinkedQueue< String >( );
		fTimeFromInitialize = System.nanoTime( );
		fTerminate = false;				
		System.out.println( System.nanoTime( ) );
		fThread = new Thread( new TPopThread( fLogFilePath ) );
		fThread.start( );
	}
	
	private  class TPopThread implements Runnable{

		
		private FileWriter fFileOutput; 
		public TPopThread( String aLogFilePath ) {
			try {
				fFileOutput = new FileWriter( new File( aLogFilePath ) );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void run( ) {
			
			
			while( !fTerminate ||  !fQueue.isEmpty() ) {

				if( !fTerminate && fQueue.isEmpty() ) {
					try {							
						synchronized( fWaitObject ) {
							fWaitObject.wait( );
						}
					} catch ( InterruptedException e ) {
						// TODO Auto-generated catch block
						System.err.println( e.getMessage() );
					}
				}				
				doWork( );
			}
			System.out.println( "exit from run block" );
		}
				
		protected void doWork( ) {
	
			if( fQueue.size( ) > 0 ) {
			String lStringToWrite = fQueue.poll( );
			//System.out.println( lStringToWrite );
		
			try {
				fFileOutput.write( lStringToWrite + "\r\n" );
				fFileOutput.flush( );
			} catch ( IOException e1 ) {
				// TODO Auto-generated catch block
				e1.printStackTrace( );
			}
			
			}
		
		}
	}
	
	/**
	 * <p>
	 * In first calling creates safe queue object. Then return queue object
	 * @return Reference to queue Object
	 *  </p>
	 */	
	public static synchronized TAsyncLogQueue getInstance( ) {		
			if( fAsyncQueue == null ) {
				fAsyncQueue = new TAsyncLogQueue( );
			}
			//System.out.println( "returning instance" );
		return fAsyncQueue;
		
	}

	/**
	 * <p>
	 * Adds item to Queue. Adds to item time since initializing in nanos
	 * @param aString  String which will be added to queue
	 *  </p>
	 */	
	public void AddRecord( String aString ) {
		
		synchronized( fWaitObject ) {	
			
			fWaitObject.notify( );
			//System.out.println( "adding to queue " + aString );
			fQueue.add( (new Date()).toString() + "\t" + (System.nanoTime( ) - fTimeFromInitialize) + "\t" + aString );
			fSizeQueue = fQueue.size();
		}	
	}

	/**
	 * <p>
	 * Waiting for finishing all popers ant terminates process
	 *  </p>
	 */	
	public void finalize( ){
            System.out.println( "start terminate" );
            fTerminate = true;	
            synchronized( fWaitObject ) {			
                    fWaitObject.notify( );
            }
		
            try {
                fThread.join( );
            } catch (InterruptedException ex) {
                System.out.println( "iterrupted" );
            }
            fAsyncQueue = null;
            System.out.println( "terminated!" );
		
    }  
	
	/**
	 * <p>
	 * Changing log path. Using before first initializing. Default value is "log.txt"
	 * @param aFilePath Path to log file
	 *  </p>
	 */	
	public void ChangeFilePath( String aFilePath ) {		
		fLogFilePath = aFilePath;	
	}
}