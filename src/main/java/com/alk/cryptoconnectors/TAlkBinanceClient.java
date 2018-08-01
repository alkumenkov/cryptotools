/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alk.binancemanager;

import com.alk.binanceservices.core.TUserInterface;
import com.alk.netwrappers.TNormalWebSocket;
import com.alk.netwrappers.TWebSocketable;
import com.senatrex.dbasecollector.queues.TAsyncLogQueue;
import java.util.Map;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author wellington
 */
public class TAlkBinanceClient implements TExchangeClient, TWebSocketable {

    private final String USER_AGENT = "Mozilla/5.0";
    private TUserInterface fUserInterface;
    TNormalWebSocket fWSClient = null;
    Thread fWsPingThread = null;
    boolean fIsClosed = false;
    String fBinanceInfo="";
    public static final String MINQTY = "minQty";
    public static final String MINPRICE = "minPrice";
  
    String fApiKey="";
    String fSecret="";

    String fListenKey = "";
    Format fFormat = new SimpleDateFormat("MM dd HH:mm:ss");

    HashMap<String, HttpsURLConnection> fConnectionPool = new HashMap();;

    private String generateSgnature(String aBody, String aSecret) throws InvalidKeyException, NoSuchAlgorithmException {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(aSecret.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secretKeySpec);
        return new String(Hex.encodeHex(sha256_HMAC.doFinal(aBody.getBytes())));
    }

//https://api.binance.com/api/v3/myTrades?symbol=BTCUSDT&recvWindow=6000000&timestamp=1520862039468
    private HttpsURLConnection getConnectionFromPool(String aUrl) throws IOException {

        HttpsURLConnection oCon = fConnectionPool.get(aUrl);
        if (oCon == null) {
            URL obj = new URL(aUrl);
            oCon = (HttpsURLConnection) obj.openConnection();
           
            fConnectionPool.put(aUrl, oCon);
        }
        return oCon;
    }

    String lResponseSample = "{\"listenKey\":\"Jewa1D094T8XMAOJEuJeWblxjicWkF5G3HKjbjpeCxuH5b808WC1SC4HuS0q\"}";

    public TAlkBinanceClient( String aApiKey, String aSecret, boolean aIsRest ) {
        
        fApiKey = aApiKey;
        fSecret = aSecret;
        if( aIsRest ){            
            startUserDataStream();         
        }       
    }
    
    private boolean IsValidResponseCode( int aCode ){
        return (aCode<300);
    }
    
    private String sendRequest(HttpsURLConnection aCon, String aMethod, String aBody ){
        
        String oResuilt = "";
        
        try{
            
            aCon.setRequestMethod( aMethod );
            aCon.setDoInput( true );
            aCon.setDoOutput( true );
            
            if( ( !aMethod.equals( "GET" ) ) && aBody.length() > 0 ){
                OutputStream wr = aCon.getOutputStream();
                wr.write( aBody.getBytes( "UTF-8" ) );
                wr.flush();
                wr.close();
            }

            int responseCode = aCon.getResponseCode();
            System.out.println( "Response Code : " + responseCode );

            BufferedReader in = null;
            if ( IsValidResponseCode( responseCode ) ) {
                in = new BufferedReader(new InputStreamReader(aCon.getInputStream()));
            } else {
                in = new BufferedReader(new InputStreamReader(aCon.getErrorStream()));
            }
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ( ( inputLine = in.readLine( ) ) != null ) {
                response.append( inputLine );   
            }
            
            oResuilt =  response.toString();
            TAsyncLogQueue.getInstance().AddRecord("response code="+responseCode+" Resuilt="+oResuilt+" Body"+aBody+" Method "+aMethod );
        }catch ( Exception e ){
            TAsyncLogQueue.getInstance().AddRecord("error sending Request!"+aMethod+"; body:"+aBody+"; reason:"+e.getLocalizedMessage());
        }
        
        return oResuilt;
    }
    
    private void startUserDataStream(){
        
        fConnectionPool = new HashMap();

        try {
            String url = "https://api.binance.com/api/v1/userDataStream";
            URL obj = new URL(url);

            HttpsURLConnection con =( HttpsURLConnection )obj.openConnection();
            con.setRequestProperty("X-MBX-APIKEY", fApiKey);
            // optional default is GET
            
            String lResponse = sendRequest(con, "POST", "" );
            
            JSONObject lObject = new JSONObject( lResponse );

            fListenKey = lObject.getString("listenKey");
            TAsyncLogQueue.getInstance().AddRecord( "new fListenKey!"+fListenKey );
            
            
            
            String lWssUrl = "wss://stream.binance.com:9443/ws/" + fListenKey;

            //wss://stream.binance.com:9443/ws/Jewa1D094T8XMAOJEuJeWblxjicWkF5G3HKjbjpeCxuH5b808WC1SC4HuS0q
            fWSClient = new TNormalWebSocket(lWssUrl, this);

            if( fWsPingThread == null ){
                fWsPingThread = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        while (fIsClosed == false) {

                            try {
                                Thread.sleep( 29*1000 );
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                            try {
                                
                                URL obj = new URL( "https://api.binance.com/api/v1/userDataStream" );
                                HttpsURLConnection con =( HttpsURLConnection )obj.openConnection();
                                con.setRequestProperty("X-MBX-APIKEY", fApiKey);
                                
                                String lBody = "listenKey=" + fListenKey;
                                TAsyncLogQueue.getInstance().AddRecord( "TBinanceClient ping "+lBody );
                                

                                String lResponse = sendRequest(con, "PUT", lBody );
                                if( lResponse.equals( "{}" ) ){
                                    TAsyncLogQueue.getInstance().AddRecord( "TBinanceClient ping success!" );
                                }else {                                   
                                    TAsyncLogQueue.getInstance().AddRecord( "ping error!"+lResponse );
                                    if( lResponse.contains("This listenKey does not exist") ){
                                        TAsyncLogQueue.getInstance().AddRecord( "reconnecting" );
                                        startUserDataStream();
                                    }
                                }
    
                            } catch (Exception ex) {
                                
                                TAsyncLogQueue.getInstance().AddRecord( "Exception in ping!" + ex.getLocalizedMessage() );
                                
                            }    
                        }
                    }
                });

                fWsPingThread.start();
            }
            //print result
            int t = 0;

        } catch ( Exception e ) {
            System.out.println( e.toString( ) );
        }
    }
     
   // "balances":[{"asset":"BTC","free":"0.00857996","locked":"0.00000000"},{"asset":"LTC","free":"0.00000925","locked":"0.00000000"},{"asset":"ETH","free":"0.08109062","locked":"0.00000000"},{"asset":"BNC","free":"0.00000000","locked":"0.00000000"}
    
    @Override
    public String[][] getPositions() {
        String lBody = "&timestamp=" + System.currentTimeMillis() + "";
         List<String[]> oRes = new ArrayList();
        try {
            String url = "https://api.binance.com/api/v3/account?" + lBody;
            url += "&signature=" + generateSgnature(lBody, fSecret);
            URL obj = new URL( url );
            HttpsURLConnection con =( HttpsURLConnection )obj.openConnection();
            con.setRequestProperty("X-MBX-APIKEY", fApiKey);

            String lResponse = sendRequest( con, "GET", lBody );
       //     lResponse
            JSONObject lRespObject = new JSONObject( lResponse );
            JSONArray lBal = lRespObject.getJSONArray( "balances" );

            int lLength = lBal.length();

            for( int i=0; i<lLength; i++ ){
                JSONObject lObject = lBal.getJSONObject( i ); 
                oRes.add(new String[]{lObject.getString("asset"), lObject.getString("free"), lObject.getString("locked")});
            }

        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return oRes.toArray( new String[][]{} );
    }

    @Override
    public void SubscribeInterface(TUserInterface aUserInterface) {
        fUserInterface = aUserInterface;
        startUserDataStream();
    }

    @Override
    public List<String[]> getOpenOrders(String aSymbol) {
        
        List<String[]> oRes = new ArrayList();
        
        String lBody = "timestamp=" + System.currentTimeMillis() + "";
        try {
            String url = "https://api.binance.com/api/v3/openOrders?" + lBody;
            url += "&signature=" + generateSgnature(lBody, fSecret);
            URL obj = new URL( url );
            HttpsURLConnection con =( HttpsURLConnection )obj.openConnection();
            con.setRequestProperty("X-MBX-APIKEY", fApiKey);


            String lResponse = sendRequest( con, "GET", "" );
            
            JSONArray lBal = new JSONArray( lResponse );
            int lLength = lBal.length();

            for( int i=0; i<lLength; i++ ){
                JSONObject aReport = lBal.getJSONObject( i ); 
                   
                String lDateTime = fFormat.format( new Date( aReport.getLong("time") ) );
                String lSymbol = aReport.getString("symbol");
                String lSide = aReport.getString("side");
                String lPrice = aReport.getString("price");
                String lCumFilledQty = aReport.getString("executedQty");
                String lOrderId = ""+aReport.getLong("orderId");
                String lOrderStatus = aReport.getString("status");


                String[] lOrderArray=new String[]{
                    lDateTime,
                    lSymbol,
                    lSide,
                    lPrice,
                    lCumFilledQty,
                    lOrderId,
                    lOrderStatus};
                oRes.add(lOrderArray);
                if( fUserInterface != null ){
                    fUserInterface.addOrder( lOrderArray );
                };
            }
            TAsyncLogQueue.getInstance().AddRecord( lResponse );
          
        } catch (Exception e) {
            TAsyncLogQueue.getInstance().AddRecord( "Exception in getOpenOrders!" + e.getLocalizedMessage() );
        }
        return oRes;
        
    }

    @Override
    public List<String[]> getOrders(String aSymbol) {
        List<String[]> oRes = new ArrayList();
        
        String lBody = "symbol="+aSymbol+"&timestamp=" + System.currentTimeMillis() + "";
        
        try {
            String url = "https://api.binance.com/api/v3/allOrders?" + lBody;
            url += "&signature=" + generateSgnature(lBody, fSecret);
            URL obj = new URL( url );
            HttpsURLConnection con =( HttpsURLConnection )obj.openConnection();
            con.setRequestProperty("X-MBX-APIKEY", fApiKey);


            String lResponse = sendRequest( con, "GET", "" );
            
            JSONArray lBal = new JSONArray( lResponse );
            int lLength = lBal.length();

            for( int i=0; i<lLength; i++ ){
                JSONObject lObject = lBal.getJSONObject( i ); 
                oRes.add( new String[]{
                ""+lObject.getLong("time"),//0
                aSymbol,//1
                ""+lObject.getLong("orderId"),//2
                lObject.getString("clientOrderId"),//3
                lObject.getString("price"),//4
                lObject.getString("origQty"),//5
                lObject.getString("executedQty"),//6
                lObject.getString("status"),//7
                lObject.getString("type"),//8
                lObject.getString("side"),//9
                lObject.getString("stopPrice"),//10
                lObject.getString("icebergQty")
                } );
            }
            
                       
            TAsyncLogQueue.getInstance().AddRecord( lResponse );
          
        } catch (Exception e) {
            TAsyncLogQueue.getInstance().AddRecord( "Exception in getOrders!" + e.getLocalizedMessage() );
        }
        
        Collections.sort( oRes, Collections.reverseOrder((String[] t, String[] t1) -> ( t[ 0 ].compareTo( t1[ 0 ] ) )) );
        
        return oRes;
    }
 
    //[{"id":14815808,"orderId":37439575,"price":"856.40000000","qty":"0.03540000","commission":"0.00159596","commissionAsset":"BNB","time":1519454095438,"isBuyer":true,"isMaker":true,"isBestMatch":true},{"id":14815888,"orderId":37440093,"price":"851.50000000","qty":"0.03540000","commission":"0.00159596","commissionAsset":"BNB","time":1519454095556,"isBuyer":true,"isMaker":false,"isBestMatch":true}

     @Override
    public List<String[]> getTrades(String aSymbol) {

        List<String[]> oRes = new ArrayList();
        
        String lBody = "symbol=" + aSymbol + "&timestamp=" + System.currentTimeMillis() + "";

        try {
            String url = "https://api.binance.com/api/v3/myTrades?" + lBody;
            url += "&signature=" + generateSgnature(lBody, fSecret);
            URL obj = new URL( url );
            HttpsURLConnection con =( HttpsURLConnection )obj.openConnection();
            con.setRequestProperty("X-MBX-APIKEY", fApiKey);


            String lResponse = sendRequest( con, "GET", lBody );
            
            JSONArray lBal = new JSONArray( lResponse );
            int lLength = lBal.length();

            for( int i=0; i<lLength; i++ ){
                JSONObject lObject = lBal.getJSONObject( i ); 
                oRes.add( new String[]{
                ""+lObject.getLong("time"),//0
                ""+lObject.getLong("id"),//1
                lObject.getString("price"),//2
                lObject.getString("qty"),//3
                lObject.getString("commission"),//4
                lObject.getString("commissionAsset"),//5
                ""+lObject.getBoolean("isBuyer"),//6
                ""+lObject.getBoolean("isMaker"),//7
                ""+lObject.getBoolean("isBestMatch"),//8
                ""+lObject.getLong("orderId"),//9
                aSymbol} );//10

            }
            System.out.println( lResponse );
           
        } catch (Exception e) {
            System.out.println( "Exception in getTrades!" + e.getLocalizedMessage() );
        }
        return oRes;
    }
    
 /*   @Override
    public List<String[]> getTrades(String aSymbol) {

        List<String[]> oRes = new ArrayList();
        
        String lBody = "symbol=" + aSymbol + "&timestamp=" + System.currentTimeMillis() + "";

        try {
            String url = "https://api.binance.com/api/v3/myTrades?" + lBody;
            url += "&signature=" + generateSgnature(lBody, fSecret);
            URL obj = new URL( url );
            HttpsURLConnection con =( HttpsURLConnection )obj.openConnection();
            con.setRequestProperty("X-MBX-APIKEY", fApiKey);


            String lResponse = sendRequest( con, "GET", lBody );
            
            JSONArray lBal = new JSONArray( lResponse );
            int lLength = lBal.length();

            for( int i=0; i<lLength; i++ ){
                JSONObject lObject = lBal.getJSONObject( i ); 
                oRes.add( new String[]{
                ""+lObject.getLong("time"),//0
                ""+lObject.getLong("id"),//1
                lObject.getString("price"),//2
                lObject.getString("qty"),//3
                lObject.getString("commission"),//4
                lObject.getString("commissionAsset"),//5
                ""+lObject.getBoolean("isBuyer"),//6
                ""+lObject.getBoolean("isMaker"),//7
                ""+lObject.getBoolean("isBestMatch"),//8
                ""+lObject.getLong("orderId"),//9
                aSymbol} );//10

            }
            System.out.println( lResponse );
           
        } catch (Exception e) {
            System.out.println( "Exception in getTrades!" + e.getLocalizedMessage() );
        }
        return oRes;
    }*/

    @Override
    public void PlaceOrder(String[] OrderParams) {
        
        String lBody = "symbol=" + OrderParams[0]+
                       "&side=" + (OrderParams[1].equals("Sell")?"SELL":"BUY") + 
                       "&type=LIMIT"+
                       "&timeInForce=GTC"+
                       "&quantity="+OrderParams[3]+
                       "&price="+OrderParams[2]+
                       "&timestamp=" + System.currentTimeMillis();
        try {
            String url = "https://api.binance.com/api/v3/order";
            lBody += "&signature=" + generateSgnature(lBody, fSecret);
            URL obj = new URL( url );
            HttpsURLConnection con =( HttpsURLConnection )obj.openConnection();
            con.setRequestProperty("X-MBX-APIKEY", fApiKey);


            String lResponse = sendRequest( con, "POST", lBody );
            
            System.out.println( lResponse );
           
        } catch (Exception e) {
            System.out.println( "Exception in PlaceOrder!" + e.getLocalizedMessage() );
        }
    }

    @Override
    public void CancelOrder(String[] OrderParams) {
        String lBody = "symbol=" + OrderParams[0]+
                        "&orderId=" + OrderParams[1]+
                        "&timestamp=" + System.currentTimeMillis();
        try {
            String url = "https://api.binance.com/api/v3/order";
            lBody += "&signature=" + generateSgnature(lBody, fSecret);
            URL obj = new URL( url );
            HttpsURLConnection con =( HttpsURLConnection )obj.openConnection();
            con.setRequestProperty("X-MBX-APIKEY", fApiKey);


            String lResponse = sendRequest( con, "DELETE", lBody );
            
            System.out.println( lResponse );
           
        } catch (Exception e) {
            System.out.println( "Exception in CancelOrder!" + e.getLocalizedMessage() );
        }
    }

    //"symbols":[{"symbol":"ETHBTC","status":"TRADING","baseAsset":"ETH","baseAssetPrecision":8,"quoteAsset":"BTC","quotePrecision":8,"orderTypes":["LIMIT","LIMIT_MAKER","MARKET","STOP_LOSS_LIMIT","TAKE_PROFIT_LIMIT"],"icebergAllowed":true,"filters":[{"filterType":"PRICE_FILTER","minPrice":"0.00000100","maxPrice":"100000.00000000","tickSize":"0.00000100"},{"filterType":"LOT_SIZE","minQty":"0.00100000","maxQty":"100000.00000000","stepSize":"0.00100000"},{"filterType":"MIN_NOTIONAL","minNotional":"0.00100000"}]},
    //           {"symbol":"LTCBTC","status":"TRADING","baseAsset":"LTC","baseAssetPrecision":8,"quoteAsset":"BTC","quotePrecision":8,"orderTypes":["LIMIT","LIMIT_MAKER","MARKET","STOP_LOSS_LIMIT","TAKE_PROFIT_LIMIT"],"icebergAllowed":true,"filters":[{"filterType":"PRICE_FILTER","minPrice":"0.00000100","maxPrice":"100000.00000000","tickSize":"0.00000100"},{"filterType":"LOT_SIZE","minQty":"0.01000000","maxQty":"100000.00000000","stepSize":"0.01000000"},{"filterType":"MIN_NOTIONAL","minNotional":"0.00100000"}]}
    
    @Override
    public Map<String, String> getTickerInfo(String aSymbol) {
        Map<String, String> oRes = new HashMap();
        URL obj;
        try {
            obj = new URL( "https://api.binance.com/api/v1/exchangeInfo" );       
            HttpsURLConnection con =( HttpsURLConnection )obj.openConnection();
            con.setRequestProperty("X-MBX-APIKEY", fApiKey);

            if( fBinanceInfo.isEmpty() ){
                fBinanceInfo = sendRequest(con, "GET", "" );
            }
            JSONObject lObject = new JSONObject(fBinanceInfo);
            JSONArray lArr = lObject.getJSONArray("symbols");
            
            int lLength = lArr.length();
        
            for( int i=0; i<lLength; i++ ){
                JSONObject lSymbolObject = lArr.getJSONObject( i ); 
                if( lSymbolObject.getString( "symbol" ).equals( aSymbol ) ){
                    JSONArray lFilters = lSymbolObject.getJSONArray("filters");
                    for( int j=0; j<lFilters.length(); j++ ){
                         JSONObject lFilter = lFilters.getJSONObject( j ); 
                        if( lFilter.getString("filterType").equals( "PRICE_FILTER" ) ){    
                            oRes.put( MINPRICE, lFilter.getString( MINPRICE ) );
                        }

                        if( lFilter.getString("filterType").equals( "LOT_SIZE" ) ){
                            oRes.put( MINQTY, lFilter.getString( MINQTY ) );
                        }
                        if( oRes.size()==2 ){
                            return oRes;
                        }
                    }
                }
            }
            int t=0;
        
        } catch ( Exception ex ) {
             TAsyncLogQueue.getInstance().AddRecord( "Exception in getTickerInfo!" + ex.getLocalizedMessage() );
        }
        return oRes;
    }

    @Override
    public String getPrice(String aSymbol) {
        String oRes="";
        String lBody = "symbol=" + aSymbol;
        try {
            String url = "https://api.binance.com/api/v3/ticker/price?" + lBody;
            
            URL obj = new URL( url );
            HttpsURLConnection con =( HttpsURLConnection )obj.openConnection();
            con.setRequestProperty("X-MBX-APIKEY", fApiKey);

            

            String lResponse = sendRequest( con, "GET", lBody );
            JSONObject lObject = new JSONObject( lResponse );
            oRes = lObject.getString("price");
            System.out.println( lResponse );
           
        } catch (Exception e) {
            System.out.println( "Exception in getPrice!" + e.getLocalizedMessage() );
        }
        return oRes;
    }

    @Override
    public void Close() {
        fIsClosed = true;
        URL obj;
        if( fWsPingThread != null ){
            
            try {
                obj = new URL( "https://api.binance.com/api/v1/userDataStream" );       
                HttpsURLConnection con =( HttpsURLConnection )obj.openConnection();
                con.setRequestProperty("X-MBX-APIKEY", fApiKey);

                String lBody = "listenKey=" + fListenKey;


                String lResponse = sendRequest(con, "DELETE", lBody );
                if( lResponse.equals( "{}" ) ){
                    TAsyncLogQueue.getInstance().AddRecord( "TBinanceClient close success!fListenKey "+fListenKey );
                }else {                                   
                    TAsyncLogQueue.getInstance().AddRecord( "close error!"+lResponse );
                    if( lResponse.contains("This listenKey does not exist") ){
                        TAsyncLogQueue.getInstance().AddRecord( "reconnecting" );
                    }
                }

            } catch ( Exception ex ) {
                 TAsyncLogQueue.getInstance().AddRecord( "Exception in close!" + ex.getLocalizedMessage() );
            }
        }
        int t=0;

    }

    @Override
    public void onMessage(String aMessage) {
        
        TAsyncLogQueue.getInstance().AddRecord( aMessage );
        
        if( aMessage.contains( "Closing!" ) ){
            if( fIsClosed == false ){
                TAsyncLogQueue.getInstance().AddRecord( "reconnecting!" );
                startUserDataStream();
            }
        } else{
        
            try {

                TAsyncLogQueue.getInstance().AddRecord( aMessage );

                JSONObject lObject = new JSONObject(aMessage);

                String lType = lObject.getString("e");

                if( lType.equals("outboundAccountInfo") ){
                    handleAcountReport( lObject );
                }

                if( lType.equals("executionReport") ){
                    handleExecutionReport( lObject );
                }

            } catch (Exception e) {
                if( fUserInterface != null ){
                    fUserInterface.SendTextMessage( aMessage );
                    TAsyncLogQueue.getInstance().AddRecord( aMessage );
                }
            }
            System.out.println(aMessage);
            
        }
    }

    private void handleExecutionReport(JSONObject aReport) {
        
        
        String lDateTime = fFormat.format( new Date( aReport.getLong("E") ) );
        String lSymbol = aReport.getString("s");
        String lSide = aReport.getString("S");
        String lPrice = aReport.getString("p");
        String lFilledQty = aReport.getString("l");
        String lCumFilledQty = aReport.getString("z");
        String lOrderId = ""+aReport.getLong("i");
        String lExecutionType = aReport.getString("x");
        String lOrderStatus = aReport.getString("X");
        
        if( lExecutionType.equals("TRADE")){
            
            String[] lTradeArray=new String[]{
                lDateTime,
                lSymbol,
                lSide,
                lPrice,
                lFilledQty,
                lOrderId};
            if( fUserInterface != null ){
                fUserInterface.addTrade( lTradeArray );

            }  
        } 
            
        String[] lOrderArray=new String[]{
            lDateTime,
            lSymbol,
            lSide,
            lPrice,
            lCumFilledQty,
            lOrderId,
            lOrderStatus};
        if( fUserInterface != null ){
                fUserInterface.addOrder( lOrderArray );

        }
        
    }

    private void handleAcountReport(JSONObject aReport) {
        
        JSONArray lBal = aReport.getJSONArray( "B" );
        int lLength = lBal.length();
        
        String[][] lRes = new String[ lLength ][ 3 ];
        
        for( int i=0; i<lLength; i++ ){
            JSONObject lObject = lBal.getJSONObject( i ); 
            lRes[i][0] = lObject.getString("a");
            lRes[i][1] = lObject.getString("f");
            lRes[i][2] = lObject.getString("l"); 
        }
        
        if( fUserInterface != null ){
                fUserInterface.SetPositions(lRes);

        }
        int t=0;
        
    }
    
    @Override
    public void reconnect() {
        TAsyncLogQueue.getInstance().AddRecord( "reconnecting!" );
        startUserDataStream();
    }

    
}
