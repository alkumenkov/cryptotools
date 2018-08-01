/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alk.accounttools;

import com.alk.clientManager.ClientManager;
import com.senatrex.dbasecollector.queues.TAsyncLogQueue;
import com.senatrex.firebirdsample.pdbaseworking.DBaseWorking;
import java.io.File;
import java.io.FileReader;
import java.util.Map;
import org.ini4j.Ini;

/**
 *
 * @author wellington
 */
public class TPriceConverter {
    
    private static TPriceConverter fInstance=null;
    DBaseWorking  fBaseWorking;
    String[][] fPricesSnapShot=null;
    String fMarketDate=null;
            
    public static TPriceConverter getInstance(){
        if( fInstance == null ){
            fInstance = new TPriceConverter();
        }
        return fInstance;
    }
    
    private TPriceConverter(){
        TAsyncLogQueue.getInstance().AddRecord("Started!");
        Ini lIniObject = new Ini();

        try { 
            lIniObject.load( new FileReader( new File( "my.ini") ) );
        } catch ( Exception ex ) { 
            System.out.print( ex ); 
        }

        Map<String, String> lBaseParams = lIniObject.get("dbase");
        
        if( lBaseParams != null ){
            fBaseWorking = new DBaseWorking( lBaseParams.get("connectstring"), lBaseParams.get("login"), lBaseParams.get("password") );     
        }
    }
    
    public double convertCurrency( String aAsset, String aCurrency, String aMarketDate ){
        
        double lResuilt = 0.0;
        
        if( fPricesSnapShot == null || ( !fMarketDate.equals( aMarketDate ) ) ){
            fPricesSnapShot = fBaseWorking.GetQueryAsStringArr( "select asset,currency,close_price from symbol_properties prop\n" +
                                                                "join exchange_exported_prices pr on prop.symbol=pr.symbol\n" +
                                                                "where pr.curr_date='"+aMarketDate+"'" );  
            fMarketDate=aMarketDate;
        }
        
        
        
        
        lResuilt = findNonCrossCurrency( aAsset, aCurrency );
        if( lResuilt == 0.0 ){
            lResuilt =  findNonCrossCurrencyReverse( aAsset, aCurrency );
            if( lResuilt == 0.0 ){
                lResuilt =  findFirstCrossCurrency( aAsset, aCurrency );
                if( lResuilt == 0.0 ){
                    lResuilt =  findFirstCrossCurrencyReverse( aAsset, aCurrency );
                }
            }
        }
        
        return lResuilt;
    }
    
    
    private double findNonCrossCurrency( String aAsset, String aCurrency ){
        double oRes = 0.0;
       
        if( (fPricesSnapShot!=null) && fPricesSnapShot.length > 1 ){   
            for(int i=1; i<fPricesSnapShot.length; i++){
                String lAsset = fPricesSnapShot[ i ][ 0 ];
                String lCurrency = fPricesSnapShot[ i ][ 1 ];
                if( lAsset.equals( aAsset ) && lCurrency.equals( aCurrency ) ){
                    try{
                        oRes = ( Double.parseDouble( fPricesSnapShot[ i ][ 2 ] ) );
                    }catch( Exception e ){
                        TAsyncLogQueue.getInstance().AddRecord( "findNonCrossCurrency" + e.getLocalizedMessage());
                    }
                    break;
                }
            }
        }
        
        return oRes;
    }
    
    private double findNonCrossCurrencyReverse( String aAsset, String aCurrency ){
        double oRes = 0.0;
       
        if( ( fPricesSnapShot != null ) && ( fPricesSnapShot.length > 1 ) ){   
            for(int i=1; i<fPricesSnapShot.length; i++){
                String lAsset = fPricesSnapShot[ i ][ 1 ];
                String lCurrency = fPricesSnapShot[ i ][ 0 ];
                if( lAsset.equals( aAsset ) && lCurrency.equals( aCurrency ) ){
                    
                    try{
                        double lValue = Double.parseDouble( fPricesSnapShot[ i ][ 2 ] );
                        if( lValue > 0.0 ){
                            oRes = ( 1/lValue );
                        }
                    }catch( Exception e ){
                        TAsyncLogQueue.getInstance().AddRecord( "findNonCrossCurrencyReverse" + e.getLocalizedMessage());
                    }
                    break;
                    
                }
            }
        }
        
        return oRes;
    }
    
    private double findFirstCrossCurrency( String aAsset, String aCurrency ){
        
        double oRes = 0.0;
       
        if( (fPricesSnapShot!=null) && fPricesSnapShot.length > 1 ){   
            for(int i=1; i<fPricesSnapShot.length; i++){
                String lAsset = fPricesSnapShot[ i ][ 0 ];
                String lCurrency = fPricesSnapShot[ i ][ 1 ];
                
                if( lAsset.equals( aAsset ) ){
                    double lVal = 0.0;
                    lVal = findNonCrossCurrencyReverse( lCurrency, aCurrency ); 
                    if( lVal == 0.0 ){
                       lVal = findNonCrossCurrency( lCurrency, aCurrency); 
                    }
                     
                     
                    if( lVal > 0.0 ){
                        try{
                            oRes = ( Double.parseDouble( fPricesSnapShot[ i ][ 2 ] ) );
                            oRes = oRes*lVal;
                        }catch( Exception e ){
                            TAsyncLogQueue.getInstance().AddRecord( "findFirstCrossCurrency" + e.getLocalizedMessage());
                        }
                        break;
                    }   
                }
            }
        }
        
        return oRes;
    }
    
    private double findFirstCrossCurrencyReverse( String aAsset, String aCurrency ){
        
        double oRes = 0.0;
       
        if( (fPricesSnapShot!=null) && fPricesSnapShot.length > 1 ){   
            for(int i=1; i<fPricesSnapShot.length; i++){
                String lAsset = fPricesSnapShot[ i ][ 1 ];
                String lCurrency = fPricesSnapShot[ i ][ 0 ];
                
                if( lAsset.equals( aAsset ) ){
                    
                    double lVal = 0.0;
                    lVal = findNonCrossCurrencyReverse( lCurrency, aCurrency ); 
                    if( lVal == 0.0 ){
                       lVal = findNonCrossCurrency( lCurrency, aCurrency); 
                    }
                     
                    if( lVal > 0.0 ){
                        try{
                            double lRes = ( Double.parseDouble( fPricesSnapShot[ i ][ 2 ] ) );
                            oRes = 1/lRes;
                            oRes = oRes*lVal;
                        }catch( Exception e ){
                            TAsyncLogQueue.getInstance().AddRecord( "findFirstCrossCurrency" + e.getLocalizedMessage());
                        }
                        break;
                    }   
                }
            }
        }
        
        return oRes;
    }
    
    
}
