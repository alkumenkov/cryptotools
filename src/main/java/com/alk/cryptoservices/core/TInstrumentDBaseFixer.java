/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alk.binanceservices.core;

import com.alk.binancemanager.TAlkBinanceClient;
import com.alk.binancemanager.TBinanceClient;
import com.senatrex.dbasecollector.queues.TAsyncLogQueue;
import com.senatrex.firebirdsample.pdbaseworking.DBaseWorking;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import org.ini4j.Ini;

/**
 *
 * @author wellington
 */
public class TInstrumentDBaseFixer {
    

    public static void main( String []args ){
        String lClientCode = "bin_m";
        String lType = "trades";
    
        TAsyncLogQueue.getInstance().AddRecord("Started!");
        Ini lIniObject = new Ini();

        try { 
            lIniObject.load( new FileReader( new File( "my.ini") ) );
        } catch ( Exception ex ) { 
            System.out.print( ex ); 
        }

        Map<String, String> lBaseParams = lIniObject.get("dbase");

        if( lBaseParams != null ){

            DBaseWorking  lBaseWorking = new DBaseWorking( lBaseParams.get("connectstring"), lBaseParams.get("login"), lBaseParams.get("password") );
 
            String[][] lExistingSymbols = lBaseWorking.GetQueryAsStringArr( "select distinct exchange_name from symbol_properties" );
            
            String[] lBaseCurrencies = new String[]{"USDT","BTC","ETH"};

            TAlkBinanceClient lClient = new TAlkBinanceClient("", "", true);
            
            String[][] lLimits =null;
                    
            if(args.length>0){
                ArrayList<String[]> lVect = new ArrayList();
                for( int i=0; i<args.length; i++ ){
                    lVect.add( new String[]{ args[ i ] } );
                }
                lLimits = lVect.toArray( new String[][]{} );
            } else {
                lLimits = lBaseWorking.GetQueryAsStringArr( "select distinct currency from binance_exported_limits" );
            }
            
            
            StringBuilder lBuff = new StringBuilder();
            
            if( lLimits.length > 1 ){
                for( int i=0; i< lLimits.length; i++ ) {
                    String lLimit = lLimits[ i ][ 0 ];
                    
                    for ( String lBaseCurrency:lBaseCurrencies ){
                        if( !IsInstrumentExists( lBaseCurrency, lLimit, lExistingSymbols ) ){

                            String lNewSymbol = lLimit + lBaseCurrency;
                            Map<String,String> lClientInfo = lClient.getTickerInfo( lNewSymbol );
                            if( lClientInfo != null ){
                                lBuff.append( "insert into symbol_properties(start_date,symbol,asset,currency,exchange,lot,min_amound,exchange_name,type,valid_to) values "+
                                        "('2018-02-20','"+lNewSymbol+"b"+"','"+lLimit+"','"+lBaseCurrency+"','binance',1,"+lClientInfo.getOrDefault(TBinanceClient.MINQTY, "1" )+",'"+lNewSymbol+"','coin','2020-10-10');" );
                            }
                        }
                    }        
                }
            }
            
            String lResuilt =  lBuff.toString( );
            if( lResuilt.length()==0 ){
                TAsyncLogQueue.getInstance().AddRecord("nothing to update!");
            } else {
                lBaseWorking.ExecuteUpdateQuery( lBuff.toString( ) );
            }
            lClient.Close();

        } else {
            TAsyncLogQueue.getInstance().AddRecord( "Check dbase params!" );
        } 
        
        TAsyncLogQueue.getInstance().finalize();
        int t=0;
    }
    
    static boolean IsInstrumentExists(String lBaseCurrency, String aLimit, String[][] aExistingSymbols ){
        String lNewSymbol = aLimit+lBaseCurrency;
        
        boolean lExist = false;
        for( int i=0; i<aExistingSymbols.length; i++ ){
            if( aExistingSymbols[i][0].equals( lNewSymbol ) ){
                lExist=true;
                break;
            }
        }
   
        return lExist;
    }
    
    
}
