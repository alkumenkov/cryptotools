package com.senatrex.dbasecollector.queues;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.senatrex.firebirdsample.pdbaseworking.DBaseWorking;

public class TDBaseQueue {

    private static TDBaseQueue fAsyncQueue = null;
    private  Queue< String> fQueue = null;
    private  Thread fThread = null;
    private  boolean fTerminate = false;
    private  Object fWaitObject = new Object();
    private  int sizeQueue;

    private  int fBuffLimit = 10;
    
    private static String fDBaseConnectionString;
    private static String fLogin;
    private static String fPassword;
    private static String fDriver;
    private  StringBuffer fQueryBuffer;
    //private static 

    private volatile DBaseWorking fDBaseWorking;

    public String[][] getQueryResuilt( String aQuery ){
        String[][]lResuilt = fDBaseWorking.GetQueryAsStringArr( aQuery );
        return lResuilt;
    }
    
    private TDBaseQueue() {

        fQueue = new ConcurrentLinkedQueue< String>();
        fTerminate = false;
        sizeQueue = 0;

        
        fDBaseWorking = new DBaseWorking(fDBaseConnectionString, fLogin, fPassword);
        fDBaseWorking.InitializeDriver( fDriver );
        fDBaseWorking.keepConnectionAlive( true );

        fQueryBuffer = new StringBuffer();
        fThread = new Thread( new TPopThread( ) );
        fThread.start( );

        //fBuffLimit=1000;
    }

    public static void initDBase( String aDriver, String aDBaseConnectionString, String aLogin, String aPassword ) {
        fDriver = aDriver;
        fDBaseConnectionString = aDBaseConnectionString;
        fLogin = aLogin;
        fPassword = aPassword;
    }

    private  class TPopThread implements Runnable {

        public TPopThread() {
        }

        public void run() {

            while (!fTerminate || !fQueue.isEmpty()) {

                if (!fTerminate && fQueue.isEmpty()) {
                    try {
                        synchronized (fWaitObject) {
                            fWaitObject.wait();
                        }
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        System.err.println(e.getMessage());
                    }
                }
                doWork();

            }
            System.out.println("exit from run block");
        }

        /**
         * <p>
         * This is end of queue!
         * </p>
         */
        protected void doWork() {

            if (fQueue.size() > 0) {
            //    synchronized ( fQueue ) {
                    String lDBaseQuery = fQueue.poll();
                    //	sizeQueue--;
                    sizeQueue = fQueue.size();
                    //fBuffSize
                    fQueryBuffer.append(lDBaseQuery);
                    if (fQueryBuffer.length() > fBuffLimit) {
                        boolean lIsUpdated = false;

                        lIsUpdated = fDBaseWorking.ExecuteUpdateQuery( fQueryBuffer.toString() );//
                        //fQueryBuffer.toString();
                        TAsyncLogQueue.getInstance( ).AddRecord( fQueryBuffer.toString() );
                        if (!lIsUpdated) {

                            try {
                                FileWriter fFileOutput;
                                fFileOutput = new FileWriter("query.sql", true);
                                fFileOutput.write(fQueryBuffer.toString());
                                fFileOutput.flush();
                                fFileOutput.close();
                            } catch (Exception e) {
                                
                            }
                        }
                        //System.out.println( lDBaseQuery );
                        fQueryBuffer = new StringBuffer();
                    }
            //    }
            }
        }
    }

    /**
     * <p>
     * In first calling creates safe queue object. Then return queue object
     *
     * @return Reference to queue Object
     * </p>
     */
    public static synchronized TDBaseQueue getInstance() {
        if (fAsyncQueue == null) {
            fAsyncQueue = new TDBaseQueue();
        }
        return fAsyncQueue;
    }

    /**
     * <p>
     * Adds item to Queue. Adds to item time since initializing in nanos
     *
     * @param aDBaseQuery Query string which will be added to queue
     * </p>
     */
    public void AddRecord(String aDBaseQuery) {
        synchronized ( fWaitObject ) {
            fWaitObject.notify();
            fQueue.add( aDBaseQuery );
            sizeQueue = fQueue.size();
        }
    }

    /**
     * <p>
     * Clears buffer, write consistent to file
     * </p>
     */
    public void clearBuffer() {

        synchronized ( fQueue ) {

            try {
                FileWriter fFileOutput;
                fFileOutput = new FileWriter("query.sql", true);
                while (fQueue.size() > 0) {
                    fFileOutput.write(fQueue.poll());

                }
                fFileOutput.flush();
                fFileOutput.close();

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            //sizeQueue = 0;		
        }
    }

    /**
     * <p>
     * Get number of elements in queue
     *
     * @return length of queue
     * </p>
     */
    public int getQueueSize() {
        return sizeQueue;
    }

    /**
     * <p>
     * Waiting for finishing all popers ant terminates process
     * </p>
     */
    public void finalize() throws Throwable {
        System.out.println("start terminate");
        fTerminate = true;
        synchronized (fWaitObject) {
            fWaitObject.notify();
        }

        fThread.join();
        fAsyncQueue = null;
        System.out.println("terminated!");
    }

    /**
     * Set length of queries buffer to dbase. if buffer size more than this
     * value, queries will be sent to dbase
     *
     * @param aBuffSize length of queries buffer
     */
    public void setBuffParams(int aBuffSize) {
        fBuffLimit = aBuffSize;
    }

    
}
