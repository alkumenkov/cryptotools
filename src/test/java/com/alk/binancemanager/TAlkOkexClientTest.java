/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alk.binancemanager;

import com.alk.binanceservices.core.TUserInterface;
import java.util.HashMap;
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
public class TAlkOkexClientTest {
    
    String fApiKey = "";//enter your keys here
    String fSingKey = "";//enter you keys here
    
    public TAlkOkexClientTest() {
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
     * Test of getPositions method, of class TAlkOkexClient.
     */
    @Test
    public void testGetPositions() {
        System.out.println("getPositions");
        TAlkOkexClient instance = new TAlkOkexClient(fApiKey,fSingKey,false);       
        String[][] expResult = new String[][]{};
        String[][] result = instance.getPositions();
        assertArrayEquals(expResult, result);

    }

    /**
     * Test of SubscribeInterface method, of class TAlkOkexClient.
     */
    @Test
    public void testSubscribeInterface() {
        System.out.println("SubscribeInterface");
        TUserInterface aUserInterface = null;
        TAlkOkexClient instance = new TAlkOkexClient( fApiKey, fSingKey, false );
        instance.SubscribeInterface( aUserInterface );

    }

    /**
     * Test of getOpenOrders method, of class TAlkOkexClient.
     */
    @Test
    public void testGetOpenOrders() {
        System.out.println("getOpenOrders");
        TAlkOkexClient instance = new TAlkOkexClient( fApiKey, fSingKey, false );
        String lSymbol = "eth_usdt";
        String[][] expResult = new String[][]{};
        String[][] result = instance.getOpenOrders( lSymbol ).toArray(new String[][]{});;
        assertArrayEquals(expResult, result);

    }

    /**
     * Test of getOrders method, of class TAlkOkexClient.
     */
    @Test
    public void testGetOrders() {
        System.out.println("getOrders");
        TAlkOkexClient instance = new TAlkOkexClient( fApiKey, fSingKey, false );
        String lSymbol = "eth_usdt";
        String[][] expResult = new String[][]{};
        String[][] result = instance.getOrders(lSymbol).toArray(new String[][]{});;

    }

    /**
     * Test of getTrades method, of class TAlkOkexClient.
     */
    @Test
    public void testGetTrades() {
        System.out.println("getTrades");
        String aSymbol = "eth_usdt";
        TAlkOkexClient instance = new TAlkOkexClient( fApiKey, fSingKey, false );
        String[][] expResult = new String[][]{};
        String[][] result = instance.getTrades(aSymbol).toArray(new String[][]{});;
        assertArrayEquals(expResult, result);


    }

    
    /**
     * Test of getTickerInfo method, of class TAlkOkexClient.
     */
    @Test
    public void testGetTickerInfo() {
        System.out.println("getTickerInfo");
        String aSymbol = "";
        TAlkOkexClient instance = new TAlkOkexClient( fApiKey, fSingKey, false );
        Map<String, String> expResult = new HashMap();
        Map<String, String> result = instance.getTickerInfo(aSymbol);
        assertEquals(expResult, result);

    }

    /**
     * Test of getPrice method, of class TAlkOkexClient.
     */
    @Test
    public void testGetPrice() {
        System.out.println("getPrice");
        String aSymbol = "ltc_btc";
        TAlkOkexClient instance = new TAlkOkexClient( fApiKey, fSingKey, false );
        String expResult = "";
        String result = instance.getPrice(aSymbol);
        assertNotEquals(expResult, result);
    }

    /**
     * Test of Close method, of class TAlkOkexClient.
     */
    @Test
    public void testClose() {
        System.out.println("Close");
        TAlkOkexClient instance = null;
       // instance.Close();
        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of onMessage method, of class TAlkOkexClient.
     */
    @Test
    public void testOnMessage() {
        System.out.println("onMessage");
        String aMessageBalance = "[{\"binary\":0,\"channel\":\"ok_sub_spot_eth_usdt_balance\",\"data\":{\"info\":{\"free\":{\"eth\":0.081401232765},\"freezed\":{\"eth\":0.02}}}}]";
        String aMessageOrder = "[{\"binary\":0,\"channel\":\"ok_sub_spot_eth_usdt_order\",\"data\":{\"symbol\":\"eth_usdt\",\"orderId\":295717923,\"tradeUnitPrice\":\"526.5990\",\"tradeAmount\":\"0.020000\",\"createdDate\":\"1521819650281\",\"completedTradeAmount\":\"0.000000\",\"averagePrice\":\"0\",\"tradePrice\":\"0.0000\",\"tradeType\":\"sell\",\"status\":0}}]";
       
        TAlkOkexClient instance = new TAlkOkexClient( fApiKey, fSingKey, false );
        instance.getPositions();
        instance.onMessage( aMessageBalance );
        instance.onMessage( aMessageOrder ); 

    }
    
}
