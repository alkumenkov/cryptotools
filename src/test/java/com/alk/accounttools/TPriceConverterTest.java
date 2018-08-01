/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alk.accounttools;

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
public class TPriceConverterTest {
    
    public TPriceConverterTest() {
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
     * Test of getInstance method, of class TPriceConverter.
     */
    @Test
    public void testGetInstance() {
        System.out.println("getInstance");
        TPriceConverter instance = TPriceConverter.getInstance();
        TPriceConverter expResult = null;
        assertNotEquals(expResult, instance);
        // TODO review the generated test code and remove the default call to fail.
       
    }

    /**
     * Test of convertCurrency method, of class TPriceConverter.
     */
    @Test
    public void testConvertCrossCurrency() {
        System.out.println("testConvertCrossCurrency");
        String aAsset = "LTC";
        String aCurrency = "BCC";
        String aMarketDate = "2018-03-02";
        TPriceConverter instance = TPriceConverter.getInstance();
        double expResult = 0.0;
        double result = instance.convertCurrency(aAsset, aCurrency, aMarketDate);
        assertNotEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
       
    }
    
    /**
     * Test of convertCurrency method, of class TPriceConverter.
     */
    @Test
    public void testConvertNotCrossCurrency() {
        System.out.println("testConvertNotCrossCurrency");
        String aAsset = "EOS";
        String aCurrency = "BTC";
        String aMarketDate = "2018-03-02";
        TPriceConverter instance = TPriceConverter.getInstance();
        double expResult = 0.0;
        double result = instance.convertCurrency(aAsset, aCurrency, aMarketDate);
        assertNotEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
       
    }
    
    /**
     * Test of convertCurrency method, of class TPriceConverter.
     */
    @Test
    public void testXPRCurrency() {
        System.out.println("testConvertNotCrossCurrency");
        String aAsset = "XRP";
        String aCurrency = "USDT";
        String aMarketDate = "2018-03-06";
        TPriceConverter instance = TPriceConverter.getInstance();
        double expResult = 0.0;
        double result = instance.convertCurrency(aAsset, aCurrency, aMarketDate);
        assertNotEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
       
    }
    
     /**
     * Test of convertCurrency method, of class TPriceConverter.
     */
    @Test
    public void testSameCurrency() {
        System.out.println("testConvertNotCrossCurrency");
        String aAsset = "USDT";
        String aCurrency = "USDT";
        String aMarketDate = "2018-03-06";
        TPriceConverter instance = TPriceConverter.getInstance();
        double expResult = 0.0;
        double result = instance.convertCurrency(aAsset, aCurrency, aMarketDate);
        assertNotEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
       
    }
    
}
