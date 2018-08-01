/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alk.clientManager;
 
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author wellington
 */
public class ClientManagerTest {

    public ClientManagerTest() {
       
            
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
     * Test of addOrder method, of class ClientManager.
     */
    @Test
    public void testAddOrder() {
         ClientManager fInstance = new ClientManager();
        fInstance.main(null);
        System.out.println("addOrder");
        String[] aOrder = new String[]{"a","b","c","d","e","f","g"};
        
        for(int i=0; i<100; i++){
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                
            }
            aOrder = new String[]{"a"+i,"b","c","d","e","f","g"};
            fInstance.addOrder(aOrder);
        }
        
         System.out.println("addTrade");
        String[] aTrade  = new String[]{"a","b","c","d","e","f"};
        
        fInstance.addTrade(aTrade);
        for(int i=0; i<100; i++){
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                
            }
            aTrade = new String[]{"a"+i,"b","c","d","e","f"};
            fInstance.addTrade(aTrade);
        }
        
        System.out.println("SendTextMessage");
        String aMessage = "";
       
        for(int i=0;i<100; i++){
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                
            }
            fInstance.SendTextMessage(""+i);
        }
        fInstance.dispose();
        int t=0;
    }
}
    
