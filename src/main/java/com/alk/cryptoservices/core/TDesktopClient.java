/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alk.binanceservices.core;

import com.alk.binancemanager.TAlkBinanceClient;
import com.alk.binancemanager.TAlkOkexClient;
import com.alk.clientManager.*;
import com.senatrex.dbasecollector.queues.TAsyncLogQueue;
import com.senatrex.firebirdsample.pdbaseworking.DBaseWorking;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import org.ini4j.Ini;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author wellington
 */
public class TDesktopClient {
    
    public static void main(String [] args){
      
    //    TAlkOkexClient lOkClient = new TAlkOkexClient( fApiKey, fSingKey, false );
    //    lOkClient.getOpenOrders("eth_usdt");
        
        TAsyncLogQueue.getInstance().AddRecord("Started!");
        Ini lIniObject = new Ini( );
        ClientManager lManager = new ClientManager();
        
        try { 
            lIniObject.load( new FileReader( new File( "my.ini") ) );
        } catch ( Exception ex ) { 
            System.out.print( ex ); 
        }
        
        Map<String, String> lBaseParams = lIniObject.get("dbase");
        
        Map<String, String> lMainParams = lIniObject.get("main");
        

        if( lBaseParams != null && lMainParams != null ){
            
            DBaseWorking  lBaseWorking = new DBaseWorking( lBaseParams.get("connectstring"), lBaseParams.get("login"), lBaseParams.get("password") );
            String[][] lClientCodeParams = lBaseWorking.GetQueryAsStringArr( "select sec_params, exchange from client_code_properties where client_code='"+lMainParams.getOrDefault("account", "")+"'" );

            if( lClientCodeParams.length > 1 ){
                JSONObject lSec_params =  new JSONObject( lClientCodeParams[1][0] ); 
                String lConnector = lClientCodeParams[ 1 ][ 1 ];
                ArrayList<String> lSecurities = new ArrayList<>();
                
                String lPath = lMainParams.getOrDefault( "watchlist", "watchlist.json" );
                
                lSecurities = LoadWatchListFromFile( lPath );
                if( lSecurities.isEmpty() ){
                    lSecurities = LoadWatchListFromBase( lBaseWorking, lConnector );
                }
                
                lManager.addWatchingInstruments( lSecurities );
                
                if( lConnector.equals ( "binance" ) ){

                    String lApiKey = lSec_params.getString("API_Key");
                    String lSecret = lSec_params.getString("Secret");

                    if( lApiKey != null && lSecret != null ){

                        TAlkBinanceClient lClient = new TAlkBinanceClient( lApiKey, lSecret, false );
                        lManager.SetPositions( lClient.getPositions() );
                        lManager.addClient( lClient );
                        lClient.SubscribeInterface( lManager );
                        TAsyncLogQueue.getInstance().AddRecord( "Client subscribed" );
                        lClient.getOpenOrders("");
                    }

                }


                if( lConnector.equals ( "okex" ) ){

                    JSONObject lObject =  new JSONObject( lClientCodeParams[1][0] ); 
                    String lApiKey = lObject.getString("API_Key");
                    String lSecret = lObject.getString("Secret");


                    if( lApiKey != null && lSecret != null ){

                        TAlkOkexClient lClient = new TAlkOkexClient( lApiKey, lSecret, false );

                        lManager.SetPositions( lClient.getPositions() );

                        lManager.addClient( lClient );
                        lClient.SubscribeInterface( lManager );
                        TAsyncLogQueue.getInstance().AddRecord( "Client subscribed" );
                      //  lManager.updateForm( );                            
                        for( String lRes:lSecurities ){
                            if( !lRes.equals( "exchange_name" ) ){
                                 lClient.getOpenOrders(  lRes );
                            }
                        }

                    }
                }
            }
            
            
            lManager.main( args );
             
        }
        TAsyncLogQueue.getInstance().finalize();
    }
    
    private static ArrayList<String> LoadWatchListFromFile( String aFilePath ){
        ArrayList< String > oResuilt = new ArrayList< >();
        try {
            String lSource = "";
            InputStream is = new FileInputStream(aFilePath); 
            BufferedReader buf = new BufferedReader(new InputStreamReader(is)); 
            String line = buf.readLine(); StringBuilder sb = new StringBuilder(); 
            while(line != null){ 
                sb.append(line).append("\n"); 
                line = buf.readLine(); 
            } 
            lSource = sb.toString();
            JSONArray lObject =  new JSONArray( lSource ); 
            lObject.forEach( ( aInstrumentName )->{
                oResuilt.add( aInstrumentName.toString( ) );
            });
           
        } catch ( Exception ex ) {
            TAsyncLogQueue.getInstance().AddRecord( ex.getMessage() );
        }
        return oResuilt;
    }
    
    private static ArrayList< String >  LoadWatchListFromBase(  DBaseWorking  aBaseWorking, String aConnector ){
        ArrayList< String > oResuilt = new ArrayList< >();
        String[][] lSecurities = aBaseWorking.GetQueryAsStringArr( "select exchange_name from symbol_properties where exchange='"+aConnector+"'" );

        for( String[] lRes:lSecurities ){
            if( !lRes[0].equals( "exchange_name" ) ){
                oResuilt.add( lRes[ 0 ] );
            }
        }
        
        return oResuilt;
    }
    
}
