/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alk.netwrappers;

import com.neovisionaries.ws.client.HostnameUnverifiedException;
import com.neovisionaries.ws.client.OpeningHandshakeException;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.senatrex.dbasecollector.queues.TAsyncLogQueue;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wellington
 */
public class TNormalWebSocket {
    private String fAddress;
    private TWebSocketable fClient;
    WebSocket ws = null;
    
    public TNormalWebSocket( String aAddress, TWebSocketable aClient ){
        fAddress = aAddress;
        fClient = aClient;
        
        WebSocketFactory factory = new WebSocketFactory();
        
        try {
            ws = factory.createSocket( fAddress );
            ws.addListener(new WebSocketAdapter() {
                @Override
                public void onTextMessage(WebSocket websocket, String message) throws Exception {
                   fClient.onMessage( message );
                }

                @Override
                public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                   TAsyncLogQueue.getInstance().AddRecord("closing! by server "+closedByServer);
                   System.out.println("closing!");
                }
                
                public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                    System.out.println("ping!");  
                }

                public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                     System.out.println("pong!");
                }
  
            });
        } catch (IOException ex) {
            TAsyncLogQueue.getInstance().AddRecord( "exception in websocket! "+ex.getMessage( ) );
        }
        
          
       try
        {
            // Connect to the server and perform an opening handshake.
            // This method blocks until the opening handshake is finished.
            if( ws != null ){
                ws.connect();
            }
        }
        catch (OpeningHandshakeException e)
        {
            int t=0;
            // A violation against the WebSocket protocol was detected
            // during the opening handshake.
        }
        catch (HostnameUnverifiedException e)
        {
            int t=0;
            // The certificate of the peer does not match the expected hostname.
        }
        catch (WebSocketException e)
        {
            int t=0;
            // Failed to establish a WebSocket connection.
        }catch(Exception e){
            int t=0;
        }
       
       
    }
    
    public void sendMessage( String aMessage ){
        ws.sendText(aMessage);
    }
    
    public void disconnect(){
        
    }
    
}
