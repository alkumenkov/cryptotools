/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alk.cryptoservices.core;

import com.alk.cryptoconnectors.TExchangeClient;
import com.alk.cryptoconnectors.TExchangeClientFactory;
import com.alk.updatingservice.TDBaseUpdater;
import com.senatrex.dbasecollector.queues.TAsyncLogQueue;
import com.senatrex.firebirdsample.pdbaseworking.DBaseWorking;
import java.io.File;
import java.io.FileReader;

import java.util.Map;
import org.ini4j.Ini;
import org.json.JSONObject;

/**
 *
 * @author wellington
 */
public class TUpdatingDBase {

    public static void main(String [] args){
        String lClientCode = "bin_m";
        String lType = "prices";
  ///   args = new String[]{ lType,lClientCode };
 
        if( args.length!=2 ){
            
            printHelp();
            
        } else {
            
            lType = args[0]; 
            lClientCode = args[1];
            TAsyncLogQueue.SetLogPath( lType+"_"+lClientCode+"_log.txt" );
            TAsyncLogQueue.getInstance().AddRecord("Started!");
            Ini lIniObject = new Ini();

            TDBaseUpdater lUpdater = null;

            try { 
                lIniObject.load( new FileReader( new File( "my.ini") ) );
            } catch ( Exception ex ) { 
                System.out.print( ex ); 
            }

            Map<String, String> lBaseParams = lIniObject.get("dbase");

            if( lBaseParams != null ){

                DBaseWorking  lBaseWorking = new DBaseWorking( lBaseParams.get("connectstring"), lBaseParams.get("login"), lBaseParams.get("password") );
                String[][] lSecParams = lBaseWorking.GetQueryAsStringArr( "select sec_params, exchange from client_code_properties where client_code='"+lClientCode+"'" );

                if( lSecParams.length > 1 ){
                    
                    lUpdater = new TDBaseUpdater( lBaseWorking, lClientCode, lSecParams[1][1] );

                    JSONObject lObject =  new JSONObject( lSecParams[1][0] ); 
                    TExchangeClient lClient = TExchangeClientFactory.getAccountClient( lSecParams[1][1], lObject, false );

                    if( lClient != null ){
                        
                        lUpdater.addClient( lClient );
                    
                        if( lType.equals( "trades" ) ){
                            lUpdater.updateDBaseWithTrades();
                        }
                        
                        if( lType.equals( "orders") ){
                            lUpdater.updateDBaseWithOrders();
                        }
                        
                        if( lType.equals( "prices") ){
                            lUpdater.updateDBaseWithPrices();
                        }
                        
                        if( lType.equals( "limits") ){                            
                            lUpdater.SetPositions( lClient.getPositions() ); 
                        }
                       
                        lClient.Close();
                    }

                }else{
                    TAsyncLogQueue.getInstance().AddRecord( "Check ini!" );
                }

            } else {
                TAsyncLogQueue.getInstance().AddRecord( "Check dbase params!" );
            } 
        }
        
        TAsyncLogQueue.getInstance().finalize();
       
        int t=0;
       
    }
    
    private static void printHelp(){
        String lHelp = "Hello! This service will update dbase once a call."+"\n\r"+
                "Uses 2 arguments: \n\r type of updating (trades|orders|prices|limits) \n\r client code for binance (configured in dbase)!\n\r"+
                "example: {servicename} trades bin_a";
        System.out.println( lHelp );
    }
}
