package com.alk.cryptoservices.core;

import com.alk.accounttools.TAssetMatrix;
import com.alk.cryptoconnectors.TBinanceClient;
import static com.alk.cryptoservices.core.TInstrumentDBaseFixer.IsInstrumentExists;
import com.senatrex.dbasecollector.queues.TAsyncLogQueue;
import com.senatrex.firebirdsample.pdbaseworking.DBaseWorking;
import java.io.File;
import java.io.FileReader;
import java.util.Map;
import org.ini4j.Ini;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author wellington
 */
public class TViewerAccountSummary {
    
    public static DBaseWorking fBaseWorking;
    
    public static void main(String [] args){
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
        
            fBaseWorking = new DBaseWorking( lBaseParams.get("connectstring"), lBaseParams.get("login"), lBaseParams.get("password") );

            fBaseWorking.ExecuteUpdateQuery("refresh MATERIALIZED VIEW public.exchange_exported_trades; refresh MATERIALIZED VIEW public.trades_with_properties;");
           
            TAssetMatrix lIncomeMatr = getIncomeLimit( "2018-03-14", lClientCode );
            System.out.println(lIncomeMatr);
            double lTotal = lIncomeMatr.getSummary( "USDT", lClientCode );
            System.out.println( lTotal );

            TAssetMatrix lCurrMatr = getCurrentLimit( "2018-03-15", lClientCode );
            System.out.println(lCurrMatr);
            lTotal = lCurrMatr.getSummary( "USDT", "2018-03-15" );
            System.out.println( lTotal );

            TAssetMatrix lPnlMatr = getPnl( "2018-03-14", "2018-03-16", lClientCode );
            System.out.println( lPnlMatr );
            lTotal = lPnlMatr.getSummary( "USDT", "2018-03-15" );
            System.out.println( lTotal );
           
            System.out.println( lIncomeMatr.plus( lPnlMatr ) );
            
            int t=0;
        } else {
            TAsyncLogQueue.getInstance().AddRecord( "Check dbase params!" );
        } 

        int t=0;
  //     System.out.print(getSummary());
    }
    
    public static String getSummary(){
        String lClientCode = "bin_m";
        String lType = "trades";
        String oRes = "null";
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

            fBaseWorking.ExecuteUpdateQuery("refresh MATERIALIZED VIEW public.exchange_exported_trades; refresh MATERIALIZED VIEW public.trades_with_properties;");
           
            TAssetMatrix lIncomeMatr = getIncomeLimit( "2018-03-14", "bin_a" );
            oRes+=lIncomeMatr.toString();
            double lTotal = lIncomeMatr.getSummary( "USDT", "2018-03-14" );
            oRes+=("\n\r"+lTotal);
        
            TAssetMatrix lCurrMatr = getCurrentLimit( "2018-03-15", "bin_a" );
            oRes+=lIncomeMatr.toString();
            lTotal = lCurrMatr.getSummary( "USDT", "2018-03-15" );
            oRes+=("\n\r"+lTotal);
        
            TAssetMatrix lPnlMatr = getPnl( "2018-03-06", "2018-03-14", "bin_a" );
            oRes+=lIncomeMatr.toString();
            lTotal = lPnlMatr.getSummary( "USDT", "2018-03-15" );
            oRes+=("\n\r"+lTotal);
           
            
            int t=0;
        } else {
            TAsyncLogQueue.getInstance().AddRecord( "Check dbase params!" );
        } 
        
        return oRes;
    }
    
    public static TAssetMatrix getCurrentLimit( String aDate, String aAccount ){
        
        String[][] lCurrentMoney = fBaseWorking.GetQueryAsStringArr( "select currency, (current_position+current_blocked) from binance_exported_limits where curr_date = '" + aDate + "' and client_code = '" + aAccount + "'" );
            
        TAssetMatrix lCurrMonMatr = new TAssetMatrix("current money");
        for(int i=1; i<lCurrentMoney.length; i++){
            lCurrMonMatr.addVals(lCurrentMoney[i][0], lCurrentMoney[i][1]);
        }
        
        return lCurrMonMatr;
    }
    
    public static TAssetMatrix getIncomeLimit( String aDate, String aAccount ){
        
        String[][] lStartMoney = fBaseWorking.GetQueryAsStringArr( "select currency, (incoming_position+incoming_blocked) from binance_exported_limits where curr_date = '" + aDate + "' and client_code = '" + aAccount + "'" );
            
        TAssetMatrix lStartMoneyMatr = new TAssetMatrix("income money");
        for(int i=1; i<lStartMoney.length; i++){
            lStartMoneyMatr.addVals(lStartMoney[i][0], lStartMoney[i][1]);
        }
        
        return lStartMoneyMatr;
    }
    
    public static TAssetMatrix getPnl( String aStartDate, String aEndDate, String aAccount ){
           
        String[][] lPnLCurrency = fBaseWorking.GetQueryAsStringArr( "select sum(qty*side) as total_asset, asset, sum(qty*price*side*(-1)) as total_currency, currency,sum((-1)*commission) as commission, commissionasset\n" +
                                                                    "from trades_with_properties where curr_date<='"+aEndDate+"' and curr_date>='"+aStartDate+"' and client_code = '" + aAccount + "'  group by asset, currency, client_code, commissionasset" );
        TAssetMatrix lPnlMatr = new TAssetMatrix("pnl");
        for(int i=1; i<lPnLCurrency.length; i++){
            lPnlMatr.addVals(lPnLCurrency[i][1], lPnLCurrency[i][0]);
            lPnlMatr.addVals(lPnLCurrency[i][3], lPnLCurrency[i][2]);
            lPnlMatr.addVals(lPnLCurrency[i][5], lPnLCurrency[i][4]);
        }
            
        return lPnlMatr;
    }
    
}
