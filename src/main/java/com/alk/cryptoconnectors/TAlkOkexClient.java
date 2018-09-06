/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alk.cryptoconnectors;

import com.alk.cryptoservices.core.TUserInterface;
import com.alk.netwrappers.ClientWebSocketEndpoint;
import com.alk.netwrappers.TNormalWebSocket;
import com.alk.netwrappers.TWebSocketable;
import com.senatrex.dbasecollector.queues.TAsyncLogQueue;
import java.util.Map;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONArray;
import org.json.JSONObject;



/**
 *
 * @author wellington
 */
public class TAlkOkexClient implements TExchangeClient, TWebSocketable {

    private TUserInterface fUserInterface;
    TNormalWebSocket fWSClient = null;
    Thread fWsPingThread = null;
    boolean fIsClosed = false;
    String fBinanceInfo="";
    public static final String MINQTY = "minQty";
    public static final String MINPRICE = "minPrice";
    String[][] fPositions=null;
    String fApiKey="";
    String fSecret="";

    String fListenKey = "";
    Format fFormat = new SimpleDateFormat("MM dd HH:mm:ss");

    HashMap<String, HttpsURLConnection> fConnectionPool = new HashMap();;

    
    private String generateSgnature(String aBody) throws InvalidKeyException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(aBody.getBytes());
        BigInteger number = new BigInteger(1, messageDigest);
        String hashtext = number.toString(16);
        // Now we need to zero pad it if you actually want the full 32 chars.
        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }
        return hashtext;
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

    public TAlkOkexClient( String aApiKey, String aSecret, boolean aIsRest ) {
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
  //      CookieHandler.setDefault(new CookieManager());
        String oResuilt = "";
        
        try{
            aCon.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");
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
            String url = "wss://real.okex.com:10441/websocket";
            String lRawBody = "api_key=" + fApiKey;
            String lBodyToCtypt = lRawBody+"&secret_key="+fSecret;
            
            fWSClient = new TNormalWebSocket(url, this);
            String lLoginStr = "{'event':'login','parameters':{'api_key':'"+fApiKey+"','sign':'"+generateSgnature(lBodyToCtypt).toUpperCase()+"'}}";
            fWSClient.sendMessage(lLoginStr);

            if( fWsPingThread == null ){
                fWsPingThread = new Thread(() -> {
                    while (fIsClosed == false) {
                        
                        try {
                            Thread.sleep( 30*1000 );
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        
                        try {
                            fWSClient.sendMessage("{'event':'ping'}");
                            
                        } catch (Exception ex) {
                            
                            TAsyncLogQueue.getInstance().AddRecord( "Exception in ping!" + ex.getLocalizedMessage() );
                                
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
      //  CookieHandler.setDefault(new CookieManager());
        List<String[]> oRes = new ArrayList();
        try {
            String url = "https://www.okex.com/api/v1/userinfo.do";
            String lRawBody = "api_key=" + fApiKey;
            String lBodyToCtypt = lRawBody+"&secret_key="+fSecret;
            
            lRawBody += "&sign="+ generateSgnature(lBodyToCtypt).toUpperCase();
            URL obj = new URL( url );
            HttpsURLConnection con =( HttpsURLConnection )obj.openConnection();
            
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String lResponse = sendRequest( con, "POST", lRawBody );
            JSONObject lJSonResp = new JSONObject( lResponse ); 
            if( lJSonResp.getBoolean( "result" ) ){
                JSONObject lFreeArr = lJSonResp.getJSONObject("info").getJSONObject("funds").getJSONObject("free");
                JSONObject lBlockArr = lJSonResp.getJSONObject("info").getJSONObject("funds").getJSONObject("freezed");
                
                Set<String> lAllKeys = new HashSet<String>();
                lAllKeys.addAll( lFreeArr.keySet() );
                lAllKeys.addAll( lBlockArr.keySet() );
                
                
                String[] lKeyVals = lAllKeys.toArray(new String[]{});
                int lLength = lKeyVals.length;

                for( int i=0; i<lLength; i++ ){ 
                    oRes.add(new String[]{lKeyVals[i], lFreeArr.optString( lKeyVals[i], "0.0" ), lBlockArr.optString( lKeyVals[i], "0.0" )});
                }
            }

        } catch (Exception e) {
            System.out.println(e.toString());
        }
        fPositions = oRes.toArray( new String[][]{} );
        return fPositions;
    }

    @Override
    public void SubscribeInterface(TUserInterface aUserInterface) {
        fUserInterface = aUserInterface;
        startUserDataStream();
    }

    @Override
    public List<String[]> getOpenOrders( String aSymbol ) {
        
        List<String[]> oRes = new ArrayList();
        int lPageNum = 0;
        int lPageLength=200;
     
        try {
            int lReceivedLength = 0;
            String url = "https://www.okex.com/api/v1/order_history.do";
            do{
                lPageNum++;
                String lRawBody = "api_key=" + fApiKey; 
                lRawBody += "&current_page=" + lPageNum + "";
                lRawBody += "&page_length=" + lPageLength + "";
                lRawBody += "&status=" + 0 + "";
                lRawBody += "&symbol=" + aSymbol + "";
                String lBodyToCtypt = lRawBody+"&secret_key="+fSecret;
                lRawBody += "&sign="+ generateSgnature(lBodyToCtypt).toUpperCase();

                URL obj = new URL( url );
                HttpsURLConnection con =( HttpsURLConnection )obj.openConnection();

                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String lResponse = sendRequest( con, "POST", lRawBody );
                JSONObject lResponseObj = new JSONObject(lResponse);

                JSONArray lBal = lResponseObj.optJSONArray("orders");
                lReceivedLength = lBal.length();

                for( int i=0; i<lReceivedLength; i++ ){
                    JSONObject aReport = lBal.getJSONObject( i ); 

                    String lDateTime = fFormat.format( new Date( aReport.getLong( "create_date" ) ) );
                    String lSymbol = aReport.getString( "symbol" );
                    String lSide = aReport.getString( "type" );
                    String lPrice = ""+aReport.getDouble( "price" );
                    String lCumFilledQty = ""+aReport.getDouble( "deal_amount" );
                    String lOrderId = ""+aReport.getLong( "order_id" );
                    String lOrderStatus = decryptOrderStatus( aReport.getInt( "status" ) );

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
            } while( (lReceivedLength == lPageLength) && ( lPageNum<10 ) );
        } catch (Exception e) {
            TAsyncLogQueue.getInstance().AddRecord( "Exception in getOpenOrders!" + e.getLocalizedMessage() );
        }
        return oRes;
        
    }

    private String decryptOrderStatus( int aCode ){
        
        String lResuilt = "unknown";
        if( aCode == -1 ){
            lResuilt = "cancelled";
        }
        
        if( aCode == 0 ){
            lResuilt = "unfilled";
        }
        
        if( aCode == 1 ){
            lResuilt = "part filled";
        }
        
        if( aCode == 2 ){
            lResuilt = "fully filled";
        }
        
        if( aCode == 4 ){
            lResuilt = "cancel request in process";
        }
        
        return lResuilt;
    }
    
    
    
    @Override
    public List<String[]> getOrders(String aSymbol) {
        List<String[]> oRes = new ArrayList();
        int lPageNum = 0;
        int lPageLength=200;
        int lReceivedLength = 0;
        try {
            
            String url = "https://www.okex.com/api/v1/order_history.do";
            do{
                lReceivedLength = 0;
                lPageNum++;
                String lRawBody = "api_key=" + fApiKey; 
                lRawBody += "&current_page=" + lPageNum + "";
                lRawBody += "&page_length=" + lPageLength + "";
                lRawBody += "&status=" + 1 + "";
                lRawBody += "&symbol=" + aSymbol + "";
                String lBodyToCtypt = lRawBody+"&secret_key="+fSecret;
                lRawBody += "&sign="+ generateSgnature(lBodyToCtypt).toUpperCase();

                URL obj = new URL( url );
                HttpsURLConnection con =( HttpsURLConnection )obj.openConnection();

                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String lResponse = sendRequest( con, "POST", lRawBody );
                JSONObject lResponseObj = new JSONObject(lResponse);

                JSONArray lBal = lResponseObj.optJSONArray("orders");
                lReceivedLength = lBal.length();

                for( int i=0; i<lReceivedLength; i++ ){
                    JSONObject lObject = lBal.getJSONObject( i ); 
                    oRes.add( new String[]{
                    ""+lObject.getLong("create_date"),
                    aSymbol,
                    ""+lObject.getLong( "order_id" ),
                    "",
                    ""+lObject.getDouble( "avg_price" ),
                    ""+lObject.getDouble( "amount" ),
                    ""+lObject.getDouble( "deal_amount" ),
                    decryptOrderStatus( lObject.getInt( "status" ) ),
                    "",
                    lObject.getString("type").contains("sell")?"sell":"buy",
                    "0.0",
                    "0"
                    } );

                }
            TAsyncLogQueue.getInstance().AddRecord( lResponse );
            } while( (lReceivedLength == lPageLength) && ( lPageNum<10 ) );
        } catch (Exception e) {
            TAsyncLogQueue.getInstance().AddRecord( "Exception in getOrders!" + e.getLocalizedMessage() );
        }
        
        Collections.sort( oRes, Collections.reverseOrder( ( String[] t, String[] t1 ) -> ( t[ 0 ].compareTo( t1[ 0 ] ) ) ) );
        return oRes;
    }
 
    //[{"id":14815808,"orderId":37439575,"price":"856.40000000","qty":"0.03540000","commission":"0.00159596","commissionAsset":"BNB","time":1519454095438,"isBuyer":true,"isMaker":true,"isBestMatch":true},{"id":14815888,"orderId":37440093,"price":"851.50000000","qty":"0.03540000","commission":"0.00159596","commissionAsset":"BNB","time":1519454095556,"isBuyer":true,"isMaker":false,"isBestMatch":true}

    @Override
    public List<String[]> getTrades(String aSymbol) {
        List<String[]> oRes = new ArrayList<>();
        
        int lPageNum = 0;
        int lPageLength=200;
        int lReceivedLength = 0;
        try {
            
            String url = "https://www.okex.com/api/v1/order_history.do";
            do{
                lReceivedLength = 0;
                lPageNum++;
                String lRawBody = "api_key=" + fApiKey; 
                lRawBody += "&current_page=" + lPageNum + "";
                lRawBody += "&page_length=" + lPageLength + "";
                lRawBody += "&status=" + 1 + "";
                lRawBody += "&symbol=" + aSymbol + "";
                String lBodyToCtypt = lRawBody+"&secret_key="+fSecret;
                lRawBody += "&sign="+ generateSgnature(lBodyToCtypt).toUpperCase();

                URL obj = new URL( url );
                HttpsURLConnection con =( HttpsURLConnection )obj.openConnection();

                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String lResponse = sendRequest( con, "POST", lRawBody );
                JSONObject lResponseObj = new JSONObject(lResponse);

                JSONArray lBal = lResponseObj.optJSONArray("orders");
                lReceivedLength = lBal.length();

                for( int i=0; i<lReceivedLength; i++ ){
                    JSONObject lObject = lBal.getJSONObject( i ); 
                    
                    if(decryptOrderStatus( lObject.getInt( "status" ) ).contains(" filled") || ( lObject.getDouble( "deal_amount" ) > 0.0 )  ){
                    
                        oRes.add( new String[]{
                         ""+lObject.getLong("create_date"),//1
                        ""+lObject.getLong( "order_id" ),//2
                        ""+lObject.getDouble( "avg_price" ),//3
                        ""+lObject.getDouble( "deal_amount" ),//4
                        "",//5
                       "",//6
                        ""+(lObject.getString("type").contains("sell")?"false":"true"),//7
                        ""+(lObject.getString("type").contains("market")?"false":"true"),//8
                        "",//9
                        ""+lObject.getLong( "order_id" ),//10
                        aSymbol} );
                        
                    }                 
                }
            } while( (lReceivedLength == lPageLength) && ( lPageNum<100 ) );
        } catch (Exception e) {
            System.out.println( "Exception in getTrades!" + e.getLocalizedMessage() );
        }
        Collections.sort( oRes, Collections.reverseOrder( ( String[] t, String[] t1 ) -> ( t[ 0 ].compareTo( t1[ 0 ] ) ) ) );
        return oRes;
    }

    @Override
    public void PlaceOrder(String[] OrderParams) {
        
       List<String[]> oRes = new ArrayList();

        try {
            
            String url = "https://www.okex.com/api/v1/trade.do";
            
            String lRawBody = "amount=" + OrderParams[3];
            lRawBody += "&api_key=" + fApiKey;
            lRawBody += "&price=" + OrderParams[2] + "";
            lRawBody += "&symbol=" + OrderParams[ 0 ] + "";
            lRawBody += "&type=" + (OrderParams[1].equals("Sell")?"sell":"buy") + "";

            String lBodyToCtypt = lRawBody+"&secret_key="+fSecret;
            lRawBody += "&sign="+ generateSgnature(lBodyToCtypt).toUpperCase();

            URL obj = new URL( url );
            HttpsURLConnection con =( HttpsURLConnection )obj.openConnection();

            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String lResponse = sendRequest( con, "POST", lRawBody );
            
            if( fUserInterface != null ){
                fUserInterface.SendTextMessage(lResponse);
            }
            
        } catch (Exception e) {
            System.out.println( "Exception in getTrades!" + e.getLocalizedMessage() );
        }
    }

    @Override
    public void CancelOrder(String[] OrderParams) {
        
        List<String[]> oRes = new ArrayList();

        try {
            
            String url = "https://www.okex.com/api/v1/cancel_order.do";
            
            String lRawBody = "api_key=" + fApiKey; 
            lRawBody += "&order_id=" + OrderParams[1] + "";
            lRawBody += "&symbol=" + OrderParams[0] + "";

            String lBodyToCtypt = lRawBody+"&secret_key="+fSecret;
            lRawBody += "&sign="+ generateSgnature(lBodyToCtypt).toUpperCase();

            URL obj = new URL( url );
            HttpsURLConnection con =( HttpsURLConnection )obj.openConnection();

            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String lResponse = sendRequest( con, "POST", lRawBody );
            
            if( fUserInterface != null ){
                fUserInterface.SendTextMessage(lResponse);
            }
            
        } catch (Exception e) {
            System.out.println( "Exception in getTrades!" + e.getLocalizedMessage() );
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
            String url = "https://www.okex.com/api/v1/ticker.do?" + lBody;
            
            URL obj = new URL( url );
            HttpsURLConnection con =( HttpsURLConnection )obj.openConnection();
            con.setRequestProperty("X-MBX-APIKEY", fApiKey);

            

            String lResponse = sendRequest( con, "GET", lBody );
            JSONObject lObject = new JSONObject( lResponse );
            oRes = lObject.getJSONObject("ticker").getString("last");
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
        System.out.println(aMessage);
               
        TAsyncLogQueue.getInstance().AddRecord( aMessage );
        
        try {
            JSONArray lArr = new JSONArray( aMessage );
            int lLength = lArr.length();

            for( int i=0; i<lLength; i++ ){
                JSONObject lMessage = lArr.getJSONObject( i );
                if( lMessage.has( "channel" ) ){
                    String lChannel = lMessage.getString( "channel" );
                    JSONObject lData = lMessage.getJSONObject("data");
                    if( lChannel.contains( "order" ) ){                        
                        handleExecutionReport( lData );
                    }

                    if( lChannel.contains( "balance" ) ){
                        handleAccountReport( lData );
                    }
                }
            }

        } catch (Exception e) {
            if( fUserInterface != null ){
                fUserInterface.SendTextMessage( aMessage );
                TAsyncLogQueue.getInstance().AddRecord( aMessage );
            }
        }
        System.out.println(aMessage);

    }
    
    private void handleExecutionReport( JSONObject aReport ) {
          
        String lDateTime = fFormat.format( new Date( aReport.getLong("createdDate") ) );
        String lSymbol = aReport.getString("symbol");
        String lSide = aReport.getString("tradeType");
        String lPrice = aReport.getString("tradeUnitPrice");
        String lFilledQty = aReport.getString("tradeAmount");
        String lCumFilledQty = aReport.getString("completedTradeAmount");
        String lOrderId = ""+aReport.getLong("orderId");
        String lExecutionType = "";
        
        int lStatus = aReport.getInt( "status" );
        String lOrderStatus = decryptOrderStatus( lStatus );
        
        if( (lStatus == 1) || (lStatus == 2) ){
            
            String[] lTradeArray=new String[]{
                lDateTime,
                lSymbol,
                lSide,
                lPrice,
                lCumFilledQty,
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
            lFilledQty,
            lOrderId,
            lOrderStatus};
        if( fUserInterface != null ){
            fUserInterface.addOrder( lOrderArray );
        }
    }

    private void handleAccountReport( JSONObject aReport ) {
        fUserInterface.SendTextMessage( aReport.toString() );
        JSONObject lInfo = aReport.getJSONObject( "info" );
        JSONObject lFree = lInfo.getJSONObject( "free" );
        JSONObject lFreezed = lInfo.getJSONObject( "freezed" );
        
        Set<String> lSymbols = new HashSet<>();
        lSymbols.addAll( lFree.keySet( ) );
        lSymbols.addAll( lFreezed.keySet( ) );
        Iterator<String> lIt = lSymbols.iterator();
        
        while( lIt.hasNext() ){
            String lSymbol = lIt.next();
            if( fPositions != null ){      
                for( int i=0; i<fPositions.length; i++ ){
                    if( fPositions[ i ].length == 3 ){
                        if( fPositions[ i ][ 0 ].equals( lSymbol ) ){
                            if( lInfo.getJSONObject( "free" ).has( lSymbol ) ){
                                fPositions[ i ][ 1 ] = ""+lInfo.getJSONObject( "free" ).getDouble( lSymbol );
                            }

                            if( lInfo.getJSONObject( "freezed" ).has( lSymbol ) ){
                                fPositions[ i ][ 2 ] = ""+lInfo.getJSONObject( "freezed" ).getDouble( lSymbol );
                            }
                        }
                    }
                }            
            }
        }
        
        
        if( fUserInterface != null ){
            fUserInterface.SetPositions( fPositions );
        }
        int t=0;
    }

    @Override
    public void reconnect() {
        TAsyncLogQueue.getInstance().AddRecord( "reconnecting!" );
        startUserDataStream();
    }

    
          
}
