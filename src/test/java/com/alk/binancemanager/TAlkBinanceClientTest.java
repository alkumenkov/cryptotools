/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alk.binancemanager;

import com.alk.binanceservices.core.TUserInterface;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author wellington
 */
public class TAlkBinanceClientTest {
    
    String fApiKey = "";//enter your keys here
    String fSecret = "";//enter your keys here

    public TAlkBinanceClientTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    
    /**
     * Test of getTrades method, of class TAlkBinanceClient.
     */
    @Test
    public void testGetTrades() {
        System.out.println("getTrades");
        String aSymbol = "ETHUSDT";
        TAlkBinanceClient instance = new TAlkBinanceClient( fApiKey, fSecret, false );
        String[][] expResult = new String[][]{};
        String[][] result = instance.getTrades(aSymbol).toArray(new String[][]{});
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
       
    }
    /**
     * Test of getPositions method, of class TAlkBinanceClient.
     */
    @Test
    public void testOnMessageTrade() {
        String lAccType = "{\"e\":\"executionReport\",\"E\":1521026660235,\"s\":\"NEOUSDT\",\"c\":\"X2Y_J\",\"S\":\"BUY\",\"o\":\"LIMIT\",\"f\":\"GTC\",\"q\":\"0.55700000\",\"p\":\"76.42500000\",\"P\":\"0.00000000\",\"F\":"+"\r\n"+
        "\"0.00000000\",\"g\":-1,\"C\":\"null\",\"x\":\"TRADE\",\"X\":\"FILLED\",\"r\":\"NONE\",\"i\":19486126,\"l\":\"0.55700000\",\"z\":\"0.55700000\",\"L\":\"76.11000000\",\"n\":\"0.00226605\",\"N\":\"BNB\",\"T\":1521026660064,\"t\":5568263,\"I\":44336135,\"w\":false,\"m\":false,\"M\":true,\"O\":-1,\"Z\":\"-0.00000001\"}";
        
        System.out.println("getTrade");
        TAlkBinanceClient instance = new TAlkBinanceClient( fApiKey, fSecret, false );
        instance.onMessage( lAccType );
        
        // TODO review the generated test code and remove the default call to fail.
        
    }
    
    /**
     * Test of getPositions method, of class TAlkBinanceClient.
     */
    @Test
    public void testGetPositions() {
        System.out.println("getPositions");
        TAlkBinanceClient instance = new TAlkBinanceClient( fApiKey, fSecret, false );
        String[][] expResult = new String[][]{};
        String[][] result = instance.getPositions();
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        
    }
    
    /**
     * Test of getPositions method, of class TAlkBinanceClient.
     */
    @Test
    public void testOnMessageAcc() {
        String lAccType = "{\"e\":\"outboundAccountInfo\",\"E\":1521026660236,\"m\":10,\"t\":10,\"b\":0,\"s\":0,\"T\":true,\"W\":true,\"D\":true,\"u\":1521026660063,\"B\":[{\"a\":\"BTC\",\"f\":\"0.00857954\",\"l\":\"0.00000000\"},"+"\n\r"+
        "{\"a\":\"LTC\",\"f\":\"0.00000925\",\"l\":\"0.00000000\"},{\"a\":\"ETH\",\"f\":\"0.08109062\",\"l\":\"0.00000000\"},{\"a\":\"BNC\",\"f\":\"0.00000000\",\"l\":\"0.00000000\"}]}";
        
        System.out.println("getPositions");
        TAlkBinanceClient instance = new TAlkBinanceClient( fApiKey, fSecret, false );
        instance.onMessage( lAccType );
        
        // TODO review the generated test code and remove the default call to fail.
    
    }

    /**
     * Test of SubscribeInterface method, of class TAlkBinanceClient.
     */
    @Test
    public void testSubscribeInterface() {
        System.out.println("SubscribeInterface");
        TUserInterface aUserInterface = null;
        TAlkBinanceClient instance = new TAlkBinanceClient( fApiKey, fSecret, false );
        instance.SubscribeInterface(aUserInterface);
        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of getOpenOrders method, of class TAlkBinanceClient.
     */
    @Test
    public void testGetOpenOrders() {
        System.out.println("getOpenOrders");
        TAlkBinanceClient instance = new TAlkBinanceClient( fApiKey, fSecret, false );
        String[][] expResult = new String[][]{};
        String[][] result = instance.getOpenOrders("").toArray(new String[][]{});;
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of getOrders method, of class TAlkBinanceClient.
     */
    @Test
    public void testGetOrders() {
        System.out.println("getOrders");
        String aSymbol = "";
        TAlkBinanceClient instance = new TAlkBinanceClient( fApiKey, fSecret, false );
        String[][] expResult = null;
        String[][] result = instance.getOrders(aSymbol).toArray(new String[][]{});;
        // TODO review the generated test code and remove the default call to fail.

    }

    

    /**
     * Test of PlaceOrder method, of class TAlkBinanceClient.
     */
  //  @Test
    public void testPlaceOrder() {
        System.out.println("PlaceOrder");
        String[] OrderParams = null;
        TAlkBinanceClient instance = new TAlkBinanceClient( fApiKey, fSecret, false );
        instance.PlaceOrder(OrderParams);
        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of CancelOrder method, of class TAlkBinanceClient.
     */
//    @Test
    public void testCancelOrder() {
        System.out.println("CancelOrder");
        String[] OrderParams = null;
        TAlkBinanceClient instance = new TAlkBinanceClient( fApiKey, fSecret, false );
        instance.CancelOrder(OrderParams);
        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of getTickerInfo method, of class TAlkBinanceClient.
     */
    @Test
    public void testGetTickerInfo() {
        System.out.println("getTickerInfo");
        String aSymbol = "ETHUSDT";
        TAlkBinanceClient instance = new TAlkBinanceClient( fApiKey, fSecret, false );
        Map<String, String> expResult = null;
        Map<String, String> result = instance.getTickerInfo(aSymbol);
        assertNotEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        
    }

    /**
     * Test of getPrice method, of class TAlkBinanceClient.
     */
    @Test
    public void testGetPrice() {
        System.out.println("getPrice");
        String aSymbol = "ETHUSDT";
        TAlkBinanceClient instance = new TAlkBinanceClient( fApiKey, fSecret, false );
        String expResult = "";
        String result = instance.getPrice(aSymbol);
        assertNotEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
       
    }

    /**
     * Test of Close method, of class TAlkBinanceClient.
     */
    @Test
    public void testClose() {
        System.out.println("Close");
        TAlkBinanceClient instance = new TAlkBinanceClient( fApiKey, fSecret, true );
        instance.Close();
        //TODO review the generated test code and remove the default call to fail.
     
    }
    

}
