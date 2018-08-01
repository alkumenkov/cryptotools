/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alk.binancemanager;

import com.alk.binanceservices.core.TUserInterface;
import java.util.List;
import java.util.Map;

/**
 *
 * @author wellington
 */
public interface TExchangeClient {
    
    public String[][] getPositions();  
    public void SubscribeInterface( TUserInterface aUserInterface );
    public List<String[]> getOpenOrders( String aSymbol );
    public List<String[]> getOrders( String aSymbol );
    public List<String[]> getTrades( String aSymbol );

    public void reconnect();
    public void PlaceOrder( String[] OrderParams );
    public void CancelOrder( String[] OrderParams );
    
    public Map<String,String> getTickerInfo( String aSymbol );
    public String getPrice( String aSymbol );
    public void Close();
}
