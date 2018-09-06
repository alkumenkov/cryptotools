/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alk.cryptoservices.core;

import java.util.ArrayList;

/**
 *
 * @author wellington
 */
public interface TUserInterface {
    
    public void SetPositions( String[][] aPositions );
    
    public void SendTextMessage( String aMessage );
    
    public void addOrder(String[] aOrder);
    
    public void addTrade(String[] aTrade);
}
