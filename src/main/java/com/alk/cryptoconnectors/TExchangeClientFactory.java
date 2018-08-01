/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alk.binancemanager;

import org.json.JSONObject;

/**
 *
 * @author wellington
 */
public class TExchangeClientFactory {
    public static TExchangeClient getAccountClient( String lExchangeName, JSONObject aSecParams, boolean lWebSocketLiving ){
        TExchangeClient oClient= null;
        
        if( lExchangeName.equals( "binance" ) && aSecParams != null ){
            String lApiKey = aSecParams.getString("API_Key");
            String lSecret = aSecParams.getString("Secret");

            if( lApiKey != null && lSecret != null ){
                oClient = new TAlkBinanceClient( lApiKey, lSecret, lWebSocketLiving );                       
            //    String[][] lPrice = lClient.getTrades("BTCUSDT");  
            } 
        }
        
        if( lExchangeName.equals( "okex" ) && aSecParams != null ){
            String lApiKey = aSecParams.getString("API_Key");
            String lSecret = aSecParams.getString("Secret");

            if( lApiKey != null && lSecret != null ){
                oClient = new TAlkOkexClient( lApiKey, lSecret, lWebSocketLiving );       
            } 
        }
        
        return oClient;
    } 
}
