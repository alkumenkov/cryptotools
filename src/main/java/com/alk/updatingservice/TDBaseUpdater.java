/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alk.updatingservice;

import com.alk.binancemanager.TExchangeClient;
import com.alk.binanceservices.core.TUserInterface;
import com.senatrex.dbasecollector.queues.TAsyncLogQueue;
import com.senatrex.firebirdsample.pdbaseworking.DBaseWorking;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 *
 * @author wellington
 */
public class TDBaseUpdater implements TUserInterface{
    
    private TExchangeClient fAccountClient;
    private DBaseWorking fBaseWorking;
    String[][] fSymbols=null;
    String fClientCode;
    String fCurrentDate;
    String fCurrentTime;
    String fExchangeName="default";
    
    
    
    public TDBaseUpdater( DBaseWorking aDBaseWorking, String aClientCode, String aExchangeName ){
        fClientCode = aClientCode;
        fBaseWorking = aDBaseWorking;
        fAccountClient = null;
        Format format = new SimpleDateFormat("yyyy-MM-dd");
        fCurrentDate =  format.format(new Date());
        fCurrentTime = (new SimpleDateFormat("HH:mm:ss")).format(new Date());
        fExchangeName = aExchangeName;
        
    }
    
    
    
    public void updateDBaseWithOrders(){
        
        if( fSymbols == null ){
            fSymbols = fBaseWorking.GetQueryAsStringArr( "select exchange_name from symbol_properties where exchange='" + fExchangeName + "'" );
        }
        
        if( fAccountClient != null ){
            for( int i=1; i<fSymbols.length; i++ ){
                try {
                    Thread.sleep(250);    
                } catch (InterruptedException ex) {
                }
                String[][] lOrdersArr = fAccountClient.getOrders( fSymbols[i][0] ).toArray( new String[][]{} );
                addOrders( lOrdersArr );
            }
        } else {
            TAsyncLogQueue.getInstance().AddRecord("initialize TAccountClient!");
        }   
    }
    
    public void updateDBaseWithTrades(){
        
        if( fSymbols == null ){
            fSymbols = fBaseWorking.GetQueryAsStringArr( "select exchange_name from symbol_properties where exchange='" + fExchangeName + "'" );
        }
        if( fAccountClient != null ){
            for( int i=1; i<fSymbols.length; i++ ){
                try {
                    Thread.sleep(250);    
                } catch (InterruptedException ex) {
                }
           
                String[][] lTrades = fAccountClient.getTrades( fSymbols[i][0] ).toArray( new String[][]{} );
                addTrades( lTrades );
            }
            
        } else {
            TAsyncLogQueue.getInstance().AddRecord( "initialize TAccountClient!" );
        }
        fBaseWorking.ExecuteUpdateQuery("refresh MATERIALIZED VIEW public.exchange_exported_trades; refresh MATERIALIZED VIEW public.trades_with_properties;");

    }
    
    public void addClient( TExchangeClient aAccountClient ){
        fAccountClient = aAccountClient;
    }
    
   
    public void updateDBaseWithPrices(){
        
        if( fSymbols == null ){
            fSymbols = fBaseWorking.GetQueryAsStringArr( "select exchange_name, symbol from symbol_properties where exchange='" + fExchangeName + "'" );
        }
        
        StringBuilder lBuff = new StringBuilder();
        
        
        if( fAccountClient != null ){
            for( int i=1; i<fSymbols.length; i++ ){
                try {
                    Thread.sleep(250);    
                } catch (InterruptedException ex) {
                }
                String lPrice = fAccountClient.getPrice( fSymbols[i][0] );
                lBuff.append( getPriceQuery( fSymbols[i][1], lPrice ) );

            }
        } else {
            TAsyncLogQueue.getInstance().AddRecord("initialize TAccountClient!");
        }
        
        
        if( lBuff.toString().length() > 0 ){
            fBaseWorking.ExecuteUpdateQuery( lBuff.toString() );
        }
    }
    
    private void addPrices( String aSymbol, String aPrice ){
        
        fBaseWorking.ExecuteUpdateQuery( getPriceQuery( aSymbol, aPrice ) );
        
    }
    
    private String getPriceQuery( String aSymbol, String aPrice ){
        
        String oResuilt="";
        if( !aPrice.isEmpty( ) ){
            String [][] lExistingRow = 
            fBaseWorking.GetQueryAsStringArr("select * from exchange_exported_prices where curr_date='"+fCurrentDate+"' and symbol='"+aSymbol+"'");

            if( lExistingRow.length > 1 ){
                oResuilt = "update exchange_exported_prices set close_price="+aPrice+" where symbol='"+aSymbol+"' and curr_date='"+fCurrentDate+"';";
            } else {
                oResuilt = "insert into exchange_exported_prices (curr_date,symbol,close_price) values('"+fCurrentDate+"','"+aSymbol+"',"+aPrice+");";
            }
        }
        return oResuilt;
    }

    
    
    @Override
    public void SetPositions(String[][] aPositions) {
        
        String[][] lRes = fBaseWorking.GetQueryAsStringArr("select * from " + fExchangeName + "_exported_limits where curr_date='"+fCurrentDate+"' and client_code='"+fClientCode+"'");
        boolean lIsInserting = true;
        
        if( lRes.length > 1 ){
            lIsInserting = false;
        }
        
        StringBuilder lBuff = new StringBuilder();
        
        for(int i=0; i<aPositions.length; i++){
           lBuff.append( prepareLimitsQuery( aPositions[ i ], lIsInserting ) );
        }
        
        fBaseWorking.ExecuteUpdateQuery( lBuff.toString() );
        fBaseWorking.ExecuteUpdateQuery( "refresh MATERIALIZED VIEW public.exchange_exported_limits;" );
        int t=0;
    }
    
    private String prepareLimitsQuery( String [] aAsset, boolean aIsInserting ){
        String oRes = "";
        
        double lAmound=0.0;
        double lBlocked=0.0;
        boolean lAssetsAreZero=false;
        try{
            lAmound = Double.parseDouble( aAsset[ 1 ] );
            lBlocked = Double.parseDouble( aAsset[ 2 ] );
            if(Double.compare(lAmound, 0.0)==0 && Double.compare(lBlocked, 0.0)==0){
                lAssetsAreZero=true;
            }
        }catch ( Exception e ){
            
        }
        
        String[][] lRes = fBaseWorking.GetQueryAsStringArr("select * from " + fExchangeName + "_exported_limits where curr_date='"+fCurrentDate+"' and client_code='"+fClientCode+"' and currency = '"+aAsset[ 0 ]+"'");
        if( !aIsInserting ){
            if( lRes.length > 1 ){
                oRes = "update " + fExchangeName + "_exported_limits set current_position=" + aAsset[ 1 ] + ", current_blocked="+aAsset[ 2 ]+", last_time='"+fCurrentTime+"' where curr_date='"+fCurrentDate+"' and client_code='"+fClientCode+"' and currency = '"+aAsset[ 0 ]+"';";
            } else {
                if( !lAssetsAreZero ){
                    oRes = "insert into " + fExchangeName + "_exported_limits (curr_date,currency,client_code,incoming_position,incoming_blocked,current_position,current_blocked,start_time,last_time) values "+"\r\n"+
                            "('"+fCurrentDate+"','"+aAsset[ 0 ]+"','"+fClientCode+"',0.0, 0.0,"+aAsset[ 1 ]+","+aAsset[ 2 ]+",'"+fCurrentTime+"','"+fCurrentTime+"');";
                }
            }
        }else {
            if(!lAssetsAreZero){
                oRes = "insert into " + fExchangeName + "_exported_limits (curr_date,currency,client_code,incoming_position,incoming_blocked,current_position,current_blocked,start_time,last_time) values "+"\r\n"+
                        "('"+fCurrentDate+"','"+aAsset[ 0 ]+"','"+fClientCode+"',"+aAsset[ 1 ]+", "+aAsset[ 2 ]+","+aAsset[ 1 ]+","+aAsset[ 2 ]+",'"+fCurrentTime+"','"+fCurrentTime+"');";

            }
        }
        return oRes;
    }
    

    @Override
    public void SendTextMessage(String aMessage) {
        TAsyncLogQueue.getInstance().AddRecord( "message for TDBaseUpdater: " + aMessage );
    }

    @Override
    public void addOrder(String[] aOrder) {
        
        fBaseWorking.ExecuteUpdateQuery( prepareOrderQuery( aOrder ) );
    
    }
    
    public void addTrades( String[][] aTradesArr ){
        
        StringBuilder lBuff = new StringBuilder();
        for ( String[] lOrderArr : aTradesArr ) {
            lBuff.append( prepareTradeQuery( lOrderArr ) );
       //      fBaseWorking.ExecuteUpdateQuery( prepareTradeQuery( lOrderArr ) );
            
        }
        if( lBuff.toString().length() > 0 ){
            fBaseWorking.ExecuteUpdateQuery( lBuff.toString() );
        }
        
    }
    
    public void addOrders( String[][] aOrdersArr ){
        
        StringBuilder lBuff = new StringBuilder();
        for (String[] lOrderArr : aOrdersArr) {
            lBuff.append( prepareOrderQuery( lOrderArr ) );
            
        //    fBaseWorking.ExecuteUpdateQuery( prepareOrderQuery( lOrderArr ) );
        }
        
        fBaseWorking.ExecuteUpdateQuery( lBuff.toString() );
        
    }
    
    private String prepareOrderQuery( String[] aOrder ){
        
        String oResuilt="";
        
        String [][] lExistingRow = 
        fBaseWorking.GetQueryAsStringArr("select * from " + fExchangeName + "_exported_orders where order_id='"+aOrder[2]+"' and client_order_id='"+aOrder[3]+"' and client_code='"+fClientCode+"'");
      
        if( lExistingRow.length > 1 ){
            oResuilt = "update " + fExchangeName + "_exported_orders set price="+aOrder[4]+", orig_qty="+aOrder[5]+", executed_qty='"+aOrder[6]+"' where order_id='"+aOrder[2]+"' and client_order_id='"+aOrder[3]+"' and client_code='"+fClientCode+"';";
        } else {
            oResuilt = 
            "insert into " + fExchangeName + "_exported_orders" + "\n\r" + 
            " (curr_time,symbol,order_id,client_order_id,price,orig_qty,executed_qty,status,type,side,stop_price,iceberg_qty,client_code) values"+"\n\r"+
            "("+aOrder[0]+",'"//curr_time
                +aOrder[1]+"','"//symbol
                +aOrder[2]+"','"//order_id
                +aOrder[3]+"',"//client_order_id
                +aOrder[4]+","//price
                +aOrder[5]+","//orig_qty
                +aOrder[6]+",'"//executed_qty
                +aOrder[7]+"','"//status
                +aOrder[8]+"','"//type
                +aOrder[9]+"',"//side
                +aOrder[10]+","//stop_price
                +aOrder[11]+",'"+//iceberg_qty
                    fClientCode+"');";
        }
        return oResuilt;
    }

    @Override
    public void addTrade(String[] aTrade) {
        
        fBaseWorking.ExecuteUpdateQuery( prepareTradeQuery( aTrade ) );
       
    }
    
    private String prepareTradeQuery( String[] aTrade ){
        
        
        String oResuilt="";

        String [][] lExistingRow = 
        fBaseWorking.GetQueryAsStringArr("select * from " + fExchangeName + "_exported_trades where order_id='"+aTrade[9]+"' and id='"+aTrade[1]+"' and client_code='"+fClientCode+"'");
        
        if( lExistingRow.length > 1 ){
            oResuilt = "update " + fExchangeName + "_exported_trades set price="+aTrade[2]+", qty="+aTrade[3]+", commission="+(aTrade[4].equals("")?"0.0":aTrade[4])+", commissionasset='"+aTrade[5]+"'"+"\n\r"+
            " where order_id='"+aTrade[9]+"' and id='"+aTrade[1]+"' and client_code='"+fClientCode+"';";
            
        } else {

            oResuilt = 
              "insert into " + fExchangeName + "_exported_trades" + "\n\r" 
            + " (curr_time, id, price, qty, commission, commissionasset, buyer,maker, bestmatch, order_id, symbol, client_code) values"+"\n\r"+
            "("+aTrade[0]+",'"+aTrade[1]+"',"+aTrade[2]+","+aTrade[3]+","+(aTrade[4].equals("")?"0.0":aTrade[4])+",'"+aTrade[5]+"','"+aTrade[6]+"','"+aTrade[7]+"','"+aTrade[8]+"','"+aTrade[9]+"','"+aTrade[10]+"','"+fClientCode+"');";
        
        }
        
        return oResuilt;
        
    }


}
