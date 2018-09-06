/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alk.cryptoconnectors;

import com.alk.cryptoservices.core.TUserInterface;
import com.binance.api.client.BinanceApiCallback;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.ExecutionType;
import com.binance.api.client.domain.OrderSide;
import com.binance.api.client.domain.OrderType;
import com.binance.api.client.domain.TimeInForce;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.Order;
import com.binance.api.client.domain.account.Trade;
import com.binance.api.client.domain.account.request.AllOrdersRequest;
import com.binance.api.client.domain.account.request.CancelOrderRequest;
import com.binance.api.client.domain.account.request.OrderRequest;
import com.binance.api.client.domain.event.AccountUpdateEvent;
import com.binance.api.client.domain.event.OrderTradeUpdateEvent;
import com.binance.api.client.domain.event.UserDataUpdateEvent;
import com.binance.api.client.domain.event.UserDataUpdateEvent.UserDataUpdateEventType;
import com.binance.api.client.domain.general.ExchangeInfo;
import com.binance.api.client.domain.general.FilterType;
import com.binance.api.client.domain.general.SymbolFilter;
import com.binance.api.client.domain.general.SymbolInfo;
import com.senatrex.dbasecollector.queues.TAsyncLogQueue;
import java.io.Closeable;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author wellington
 */
public class TBinanceClient implements TExchangeClient{

    BinanceApiRestClient fClient;
    BinanceApiWebSocketClient fWebClient; 
    String fListenKey;
    Thread fPingThread;
    boolean fIsClosed;
    Format format = new SimpleDateFormat("MM dd HH:mm:ss");
    public static final String MINQTY = "minQty";
    public static final String MINPRICE = "minPrice";
    BinanceApiClientFactory factory;
     
    public TBinanceClient(){
        this("", "", true);
    }
    
    public TBinanceClient(String aApiKey, String aSecret, boolean aIsRest){
        String lApiKey = aApiKey; //"zDOOTWF11QonmDMBxt8e1codIATt0wAKY3JDwhw3b1TFPdIEOeF9EhHMFfNAUKqu";
        String lSecrKey = aSecret;// "hqF1sxjrH8FdY0Hca9iq9K9mgLGfvTQZkKStUR2KmiwlUXdtvirBvcAVY8VQJfy7";
        
        if( lApiKey!="" && lSecrKey!="" ){
            factory = BinanceApiClientFactory.newInstance(lApiKey, lSecrKey);
        } else {
            factory = BinanceApiClientFactory.newInstance();
        }
        fWebClient = factory.newWebSocketClient(); 
        fIsClosed=false;
       
        fClient = factory.newRestClient();
       
        if( !aIsRest ){
        
            fListenKey = fClient.startUserDataStream();
            fClient.keepAliveUserDataStream( fListenKey );
            fPingThread = new Thread(new Runnable(){
                
                @Override
                public void run() {
                    while (fIsClosed == false) {

                        try{
                            fClient.ping();
                            fClient.keepAliveUserDataStream( fListenKey );
                        } catch ( Exception e ){
                            TAsyncLogQueue.getInstance().AddRecord( "Exception "+e.getMessage() );
                            reconnect();
                        }
                        
                        TAsyncLogQueue.getInstance().AddRecord( "TBinanceClient ping" );
                        
                        try {
                            Thread.sleep(29000*60);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                       // fWriter.print(new String(lHeartbeatStr));
                       // fWriter.flush();
                    }
                }
            });


            fWebClient.onUserDataUpdateEvent( fListenKey, fUserCallBack );
            fPingThread.start();
        }
        int t=0;
        //fPingThread.start();
    }
    
    long fTimeDelay=1000;
    
    @Override
    public void reconnect(){
        TAsyncLogQueue.getInstance().AddRecord( "reconnecting" );
        
        try {
            Thread.sleep( fTimeDelay );
        } catch (InterruptedException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        fTimeDelay = fTimeDelay*2;
        fWebClient = factory.newWebSocketClient(); 
        
        fIsClosed=false;
        fClient = factory.newRestClient();
        
        fListenKey = fClient.startUserDataStream();
        fWebClient.onUserDataUpdateEvent( fListenKey, fUserCallBack );
        fClient.keepAliveUserDataStream( fListenKey );
    }
    
    @Override
    public String[][] getPositions(){
        Account lAcc = fClient.getAccount();
        
        List<AssetBalance>lBal = lAcc.getBalances();
        Collections.sort(lBal, (AssetBalance lBalance1,AssetBalance lBalance2)->{return lBalance1.getAsset().compareTo( lBalance2.getAsset() );} );
        String[][] lRes = new String[ lBal.size() ][ 3 ];
        for( int i=0; i<lBal.size(); i++ ){
            
            lRes[i][0] = lBal.get(i).getAsset();
            lRes[i][1] = lBal.get(i).getFree();
            lRes[i][2] = lBal.get(i).getLocked();
            
        }

        return lRes;
    }
    
    TUserInterface fUserInterface = null;
    
    BinanceApiCallback<UserDataUpdateEvent> fUserCallBack = response -> {
        
    if( response == null ){
        
        TAsyncLogQueue.getInstance().AddRecord("failed reading response!");
        reconnect();
        
    } else {
        
    if ( response.getEventType() == UserDataUpdateEventType.ACCOUNT_UPDATE ) {
        AccountUpdateEvent accountUpdateEvent = response.getAccountUpdateEvent();
        // Print new balances of every available asset
        List<AssetBalance>lBal = accountUpdateEvent.getBalances();
        Collections.sort(lBal, (AssetBalance lBalance1,AssetBalance lBalance2)->{return lBalance1.getAsset().compareTo( lBalance2.getAsset() );} );
        String[][] lRes = new String[ lBal.size() ][ 3 ];
        for( int i=0; i<lBal.size(); i++ ){            
            lRes[i][0] = lBal.get(i).getAsset();
            lRes[i][1] = lBal.get(i).getFree();
            lRes[i][2] = lBal.get(i).getLocked();  
        }
        fUserInterface.SetPositions( lRes );
        TAsyncLogQueue.getInstance().AddRecord( accountUpdateEvent.toString() );
        fUserInterface.SendTextMessage( "AccountUpdateEvent came. See log" );
    } else {
        OrderTradeUpdateEvent orderTradeUpdateEvent = response.getOrderTradeUpdateEvent();
        
        // Print details about an order/trade
        
        orderTradeUpdateEvent.getOrderId();
        // Print original quantity
        
        // Or price
        
        if( orderTradeUpdateEvent.getExecutionType() == ExecutionType.TRADE ){
            
            String[] lTradeArray=new String[]{
                format.format(new Date(orderTradeUpdateEvent.getEventTime())),
                orderTradeUpdateEvent.getSymbol(),
                orderTradeUpdateEvent.getSide().toString(),
                orderTradeUpdateEvent.getPrice(),
                orderTradeUpdateEvent.getQuantityLastFilledTrade(),
                ""+orderTradeUpdateEvent.getOrderId()};
            fUserInterface.addTrade( lTradeArray );
            
        } 
            
        String[] lOrderArray=new String[]{
            format.format(new Date(orderTradeUpdateEvent.getEventTime())),
            orderTradeUpdateEvent.getSymbol(),
            orderTradeUpdateEvent.getSide().toString(),
            orderTradeUpdateEvent.getPrice(),
            orderTradeUpdateEvent.getQuantityLastFilledTrade(),
            ""+orderTradeUpdateEvent.getOrderId(),
            orderTradeUpdateEvent.getOrderStatus().toString()};
        fUserInterface.addOrder( lOrderArray );
            
        TAsyncLogQueue.getInstance().AddRecord( orderTradeUpdateEvent.toString() );
        fUserInterface.SendTextMessage( "Order trade update event came. See log" );
    }
    
    
    
    }
  };

    
    
    @Override
    public void SubscribeInterface(TUserInterface aUserInterface) {
        fUserInterface = aUserInterface;
    }
    
    @Override
    public List<String[]> getOrders( String aSymbol ){
         
        List<Order> allOrders = fClient.getAllOrders( new AllOrdersRequest( aSymbol ) );
        List<String[]> oRes = new ArrayList();
 
        for(int i=0; i<allOrders.size(); i++){
                oRes.add( new String[]{
                ""+allOrders.get(i).getTime(),//curr_time
                allOrders.get(i).getSymbol(),//symbol
                ""+allOrders.get(i).getOrderId(),//order_id
                allOrders.get(i).getClientOrderId(),//client_order_id
                allOrders.get(i).getPrice(),//price
                allOrders.get(i).getOrigQty(),
                allOrders.get(i).getExecutedQty(),
                allOrders.get(i).getStatus().toString(),
                ""+allOrders.get(i).getType(),
                allOrders.get(i).getSide().toString(),
                allOrders.get(i).getStopPrice(),
                allOrders.get(i).getIcebergQty()} );
 
        }
        return oRes;
    }
    
    @Override
    public List<String[]> getOpenOrders( String aSymbol ) {
 
        List<Order> allOrders = fClient.getOpenOrders(new OrderRequest("ETHUSDT"));
      
        List<String[]> lRes = new ArrayList<>();
        for(int i=0; i<allOrders.size(); i++){
            String[] lOrder = new String[]{
                                format.format(new Date(allOrders.get(i).getTime())),
                                allOrders.get(i).getSymbol(),
                                allOrders.get(i).getSide().toString(),
                                allOrders.get(i).getPrice(),
                                allOrders.get(i).getExecutedQty(),
                                ""+allOrders.get(i).getOrderId(),
                                allOrders.get(i).getStatus().toString()};
            lRes.add( lOrder );
            fUserInterface.addOrder( lOrder );
        }
        return lRes;
    }

    
    @Override
    public List<String[]> getTrades( String aSymbol ) {
        List<Trade> allTrades =fClient.getMyTrades( aSymbol ); 
        
        List<String[]> oRes = new ArrayList();
 
        for(int i=0; i<allTrades.size(); i++){
            
                oRes.add( new String[]{
                ""+allTrades.get(i).getTime(),
               ""+allTrades.get(i).getId(),
               allTrades.get(i).getPrice(),
               allTrades.get(i).getQty(),
               allTrades.get(i).getCommission(),
               allTrades.get(i).getCommissionAsset(),
               ""+allTrades.get(i).isBuyer(),
               ""+allTrades.get(i).isMaker(),
               ""+allTrades.get(i).isBestMatch(),
               allTrades.get(i).getOrderId(),
               aSymbol} );
 
        }
        
        return oRes;
    }

    @Override
    public void PlaceOrder(String[] OrderParams) {
        
        NewOrder lOrder = new NewOrder(OrderParams[0],
                                    OrderParams[1].equals("Sell")?OrderSide.SELL:OrderSide.BUY, 
                                    OrderType.LIMIT, 
                                    TimeInForce.GTC,
                                    OrderParams[3],
                                    OrderParams[2]);
        TAsyncLogQueue.getInstance().AddRecord( "PlaceOrder "+lOrder.toString( ) );
        fClient.newOrder( lOrder );

    }

    @Override
    public void CancelOrder(String[] aCancelOrderParams) {
        TAsyncLogQueue.getInstance().AddRecord("CancelOrder "+aCancelOrderParams[ 0 ]+ "\t" +aCancelOrderParams[ 1 ]);
        fClient.cancelOrder( new CancelOrderRequest( aCancelOrderParams[ 0 ],Long.parseLong( aCancelOrderParams[ 1 ] ) ) );
    }
    
    @Override
    public String getPrice( String aSymbol ){
        String lPrice = "";
        try{
            lPrice = fClient.getPrice( aSymbol ).getPrice();
        }catch(Exception e){
            TAsyncLogQueue.getInstance().AddRecord( e.getMessage( ) );
        }
        return lPrice;
    }

    @Override
    public void Close() {
        try { 
            ((Closeable)fWebClient).close();
        } catch (IOException ex) {
            System.out.print(ex.getMessage());
        }
    //    fPingThread.stop();
    //    fIsClosed=true;
   //     fClient.closeUserDataStream( fListenKey );
        int t=0;
    }

    @Override
    public Map<String, String> getTickerInfo(String aSymbol)  {
        
        Map<String, String> oRes = null;
 
        SymbolInfo lSymbolInfo = null;
        
        try{
            ExchangeInfo lInfo = fClient.getExchangeInfo();
            lSymbolInfo = lInfo.getSymbolInfo(aSymbol);
        }catch (Exception e){
            TAsyncLogQueue.getInstance().AddRecord(e.getMessage());
        }
        
        if( lSymbolInfo != null ){
            oRes = new HashMap();       
            
            List<SymbolFilter> lFilters = lSymbolInfo.getFilters();

            for( int i=0; i < lFilters.size(); i++ ){
                SymbolFilter lFilter = lFilters.get(i);
                if( lFilter.getFilterType() == FilterType.LOT_SIZE ){
                    oRes.put( MINQTY, lFilter.getMinQty() );
                }
                
                if( lFilter.getFilterType() == FilterType.PRICE_FILTER ){
                    oRes.put( MINPRICE, lFilter.getMinPrice() );
                }
            }
        }      
        return oRes;
    }
    
    
}
