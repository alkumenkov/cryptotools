/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alk.clientManager;

import com.alk.cryptoservices.core.TUserInterface;
import com.alk.cryptoconnectors.TExchangeClient;
import com.senatrex.dbasecollector.queues.TAsyncLogQueue;
import java.awt.Color;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.json.JSONObject;

/**
 *
 * @author wellington
 */
public class ClientManager extends javax.swing.JFrame implements TUserInterface{
    
    ArrayList<String> fWatchingInstruments = null;
    TExchangeClient fAccountClient=null;
    String[][] fPositions=null;
    String[] fTableTitle = new String[]{"Asset", "Free", "Locked", "Holded" };
    TreeMap<String, String> fHoldedMap = new TreeMap();
    TreeMap<String, String[]> fTradesMap = new TreeMap();
    TreeMap<String, String[]> fOrdersMap = new TreeMap();
    
    Thread lStatusThread;
    Thread lHistoryThread;
    
    final Object fHistoryWaitObject = new Object();
    final Object fJTable1WaitObject = new Object();
    final Object fJTable2WaitObject = new Object();
    final Object fJTable3WaitObject = new Object();
    
    final Object fPositionsObject = new Object();
    SimpleDateFormat fFormat = new SimpleDateFormat("MM dd HH:mm:ss");
    
    boolean fIsClosed;
    String fLastUpdateTimeLong = "";
    final static String lZeroVal = "0.00000000";
    final static String SIZE = "size";
    final static String HEIGTH = "heigth";
    final static String WIDTH = "width";
    final static String HOLDED = "holded";
    
    /**
     * Creates new form ClientManager
     */
    public ClientManager() {
        initComponents();
        loadconfig();
        
        fLastUpdateTimeLong = ""+( new Date( ) ).getTime();
        
        lStatusThread = new Thread(new Runnable() {
            @Override
            public void run() {
                
                
                while(fIsClosed==false){
                    try {
                            Thread.sleep(1000*60*10);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                            jLabel4.setText( "checking data... "+( new Date() ) );
                         
                        boolean lNeedReconnect = true;
                        try{
                            lNeedReconnect = updateForm();
                        } catch(Exception e){
                            TAsyncLogQueue.getInstance().AddRecord( e.getMessage() );
                        }
                        
                        if( !lNeedReconnect ){
                            jLabel4.setText( "no new data. last check "+( new Date() ) );
                        }else{
                            jLabel4.setText( "reconnecting... "+( new Date() ) );
                            fAccountClient.reconnect();
                            jLabel4.setText( "reconnected! last check "+( new Date() ) );
                        }
                    
                }
            }
        });
        
        lHistoryThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while( fIsClosed==false ){
                            try{
                                synchronized( fHistoryWaitObject ) {
                                    fHistoryWaitObject.wait();
                                }
          
                            }catch( InterruptedException ex ){
                                Logger.getLogger(ClientManager.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            
                            
                            jLabel4.setText( "loading history "+( new Date() ) );
                            
                            try{
                                loadHistory();

                                jLabel4.setText( "history loaded "+( new Date() ) );
                            }catch( Exception e ){
                                jLabel4.setText( "failure history load "+( new Date() ) );
                                TAsyncLogQueue.getInstance().AddRecord( e.getMessage() );
                            }
                           
                        }
                    }
                });
        
        jTable1.setDefaultRenderer(Object.class, new DefaultTableCellRenderer( ){
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
            {
                final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(row % 2 == 0 ? Color.LIGHT_GRAY : Color.WHITE);
                if( table.getColumnCount() == 4 ){

                    try{
                        double lFirstVal= Double.parseDouble(  table.getValueAt(row, 1).toString() );
                        double lSecVal= Double.parseDouble(  table.getValueAt(row, 3).toString() );
                        if( lFirstVal < lSecVal ){
                            c.setBackground( Color.RED );
                        }

                        if( lFirstVal >lSecVal  ){
                            c.setBackground( Color.GREEN );
                        }
                    } catch (Exception e){
                        TAsyncLogQueue.getInstance().AddRecord(e.getMessage());
                    }


                }
                return c;
            }
        } );
    
    }

    ArrayList<String[]> lPositions = new ArrayList();
            
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jDialog1 = new javax.swing.JDialog();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jButton2 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jButton3 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        label1 = new java.awt.Label();
        jButton4 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();

        javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
        jDialog1.getContentPane().setLayout(jDialog1Layout);
        jDialog1Layout.setHorizontalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jDialog1Layout.setVerticalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jScrollPane1.setViewportView(jTable1);

        jButton1.setText("Clear Messages");
        jButton1.setToolTipText("");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jCheckBox1.setSelected(true);
        jCheckBox1.setText("non-zero only");
        jCheckBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox1ItemStateChanged(evt);
            }
        });
        jCheckBox1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jCheckBox1MouseClicked(evt);
            }
        });

        jButton2.setText("New Order");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton2MouseClicked(evt);
            }
        });

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "DateTime", "Symbol", "Side", "Price", "Qty", "Id", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                true, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable2MouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(jTable2);

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "DateTime", "Symbol", "Side", "Price", "Qty", "Id"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(jTable3);

        jButton3.setText("Cancel Selected Order");
        jButton3.setToolTipText("");
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton3MouseClicked(evt);
            }
        });

        jLabel1.setText("Positions");

        jLabel2.setText("Orders");

        jLabel3.setText("Trades");

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane2.setViewportView(jTextArea1);

        label1.setText("Messages");

        jButton4.setText("Hold Current Positions");
        jButton4.setToolTipText("");
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton4MouseClicked(evt);
            }
        });
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel4.setText("Status");

        jButton5.setText("Update Form");
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton5MouseClicked(evt);
            }
        });
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setText("Load History");
        jButton6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton6MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 179, Short.MAX_VALUE)
                                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane1)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(4, 4, 4))
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton5))
                    .addComponent(jScrollPane4))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox1)
                    .addComponent(jLabel1)
                    .addComponent(jButton4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton3)
                        .addComponent(jButton2))
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(129, 129, 129)
                                .addComponent(jButton1))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton5)
                        .addComponent(jButton6)))
                .addGap(7, 7, 7)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addContainerGap())
        );

        label1.getAccessibleContext().setAccessibleDescription("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
        // TODO add your handling code here:
         jTextArea1.setText("");
    }//GEN-LAST:event_jButton1MouseClicked

    private void jCheckBox1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jCheckBox1MouseClicked
        // TODO add your handling code here:
      
    }//GEN-LAST:event_jCheckBox1MouseClicked

    private void jCheckBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox1ItemStateChanged
        // TODO add your handling code here:
        updatePositions();
    }//GEN-LAST:event_jCheckBox1ItemStateChanged

    private void jButton2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MouseClicked
        // TODO add your handling code here:
        
        NewOrderDialog lNewOrderDialog = new NewOrderDialog( fAccountClient );
        lNewOrderDialog.setVisible( true );
        int t=0;
    }//GEN-LAST:event_jButton2MouseClicked
    
    int fSelectedRowToCancel=0;

    private void jTable2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable2MouseClicked
        // TODO add your handling code here:
        fSelectedRowToCancel = jTable2.getSelectedRow();
        
        SendTextMessage( ""+fSelectedRowToCancel );
    }//GEN-LAST:event_jTable2MouseClicked

    private void jButton3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MouseClicked
        // TODO add your handling code here:
        String lSymbol = jTable2.getModel().getValueAt(fSelectedRowToCancel, 1).toString();
        String lUserId = jTable2.getModel().getValueAt(fSelectedRowToCancel, 5).toString();
        fAccountClient.CancelOrder( new String[]{ lSymbol, lUserId } );
    }//GEN-LAST:event_jButton3MouseClicked

    public void HoldPositions(){
        try{
            TreeMap lMap = new TreeMap();
           // fHoldedMap = new TreeMap();
            if( fPositions != null ){
                int lRowCount = fPositions.length;
                String lSymbol="", lFree="", lLocked="";

                for( int i=0; i<lRowCount; i++ ){
                    lSymbol = fPositions[ i ][ 0 ];
                    lFree = fPositions[ i ][ 1 ];
                    lLocked = fPositions[ i ][ 2 ];
                    if( !lFree.equals( lZeroVal ) || !lLocked.equals( lZeroVal ) ){
                        lMap.put( fPositions[ i ][ 0 ], String.format("%4.8f" , (Double.parseDouble( lFree )+Double.parseDouble( lLocked )) ) );
                    }
                }
                fHoldedMap = lMap;
            }
                 
        }catch( Exception e ){
            TAsyncLogQueue.getInstance().AddRecord( e.getMessage() );
        }  
    }
           
    
    
    private void jButton4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton4MouseClicked
        // TODO add your handling code here:
       
        HoldPositions();
        updatePositions();
    }//GEN-LAST:event_jButton4MouseClicked

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton4ActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        saveconfig();         
    }//GEN-LAST:event_formWindowClosing

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton5MouseClicked
        // TODO add your handling code here:
        try{
            updateForm();
        } catch(Exception e){
            TAsyncLogQueue.getInstance().AddRecord( e.getMessage() );
        } 
    }//GEN-LAST:event_jButton5MouseClicked

    private void jButton6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton6MouseClicked
        synchronized( fHistoryWaitObject ) {	
            fHistoryWaitObject.notify();
        }
               
    }//GEN-LAST:event_jButton6MouseClicked

    private void saveconfig(){
        
        int lHeigth = this.getSize().height;
        int lWidth = this.getSize().width;
        int lX = this.getX();
        int lY = this.getY();
        JSONObject lParams =  new JSONObject();
        
        JSONObject fDimensions =  new JSONObject(); 
        fDimensions.put(HEIGTH, lHeigth);
        fDimensions.put(WIDTH, lWidth);
        
        JSONObject fPlace =  new JSONObject(); 
        fPlace.put("X", lX);
        fPlace.put("Y", lY);
        lParams.put( "place", fPlace );
        
        lParams.put( SIZE, fDimensions );
        
        JSONObject fHoldedPositions =  new JSONObject();
        Set<String> lAssets = fHoldedMap.keySet();
        lAssets.forEach(lAsset->{
            fHoldedPositions.put( lAsset, fHoldedMap.get( lAsset ) );
                    });
        
        lParams.put(HOLDED, fHoldedPositions);
        
        try{
            Writer lWrite = lParams.write(new FileWriter("config"));
            lWrite.flush();
        }catch(Exception e){
            TAsyncLogQueue.getInstance().AddRecord( e.getMessage() );
        }
    }
    
    private void loadconfig(){
        String lSource = "";
        try {
            InputStream is = new FileInputStream("config"); 
            BufferedReader buf = new BufferedReader(new InputStreamReader(is)); 
            String line = buf.readLine(); StringBuilder sb = new StringBuilder(); 
            while(line != null){ 
                sb.append(line).append("\n"); 
                line = buf.readLine(); } 
            lSource = sb.toString();

        } catch ( Exception ex ) {
            TAsyncLogQueue.getInstance().AddRecord( ex.getMessage() );
        }
        
        if(lSource.equals("")){
            return;
        }
        
        JSONObject lParams =  new JSONObject(lSource);
        JSONObject lDimentions = null;
        try{
            lDimentions = lParams.getJSONObject( SIZE );
            if( lDimentions != null ){

                int lHeigth = lDimentions.getInt( HEIGTH );
                int lWidth = lDimentions.getInt( WIDTH );
                this.setSize(lWidth, lHeigth);
            }
        }catch(Exception e){
            
        }
        
        JSONObject lPlace=null;
        try{
            lPlace = lParams.getJSONObject( "place" );
            if( lPlace != null ){
                lPlace.getInt( "X" );
                int lX = lPlace.getInt( "X" );
                int lY = lPlace.getInt( "Y" );
                this.setLocation(lX, lY);
            }
        }catch(Exception e){
            
        }
        
        try{
            TreeMap<String,String> lMap = new TreeMap();

            JSONObject fHoldedPositions =  lParams.getJSONObject(HOLDED);

            Set<String> lAssets = fHoldedPositions.keySet();
            lAssets.forEach(lAsset->{
                lMap.put( lAsset, fHoldedPositions.getString( lAsset ) );
                        });

            fHoldedMap = lMap;
            updatePositions();
        }catch(Exception e){
            
        }
    }
    
    
    public synchronized void SetPositions( String[][] aTable ){
        synchronized( fPositionsObject ){
            if( aTable != null ){
                ArrayList<String[]> lPositions = new ArrayList<>();
                for(int i=0; i<aTable.length; i++){
                    String lFree = lZeroVal;
                    String lFreezed = lZeroVal;

                    try{   
                        lFree =  String.format( "%4.8f", Double.parseDouble( aTable[i][1] ) );
                        lFreezed = String.format( "%4.8f", Double.parseDouble( aTable[i][2] ) );
                    }catch( Exception e ){
                        SendTextMessage( "error parsing positions! "+aTable[ i ][ 0 ] + " " + aTable[ i ][ 1 ] + " "+  aTable[ i ][ 2 ] );
                    }

                    lPositions.add( new String[]{ aTable[ i ][ 0 ], lFree, lFreezed } );

                }
                fPositions = lPositions.toArray( new String[][]{} );
                updatePositions();
            }
        }
       /* jTable1.setModel(new javax.swing.table.DefaultTableModel(
            aTable,
            fTableTitle
        ));*/
    }

    public void addWatchingInstruments(ArrayList<String> aInstruments) {
        fWatchingInstruments = aInstruments;
    }
    
    private void loadHistory(){
        TAsyncLogQueue.getInstance().AddRecord( "loadHistory! LastUpdateTimeLong value is "+fLastUpdateTimeLong );
        boolean lIsNewValsAdded = false;
        long lCurrTimeMillis = ( new Date( ) ).getTime();
        fLastUpdateTimeLong = ""+lCurrTimeMillis;
        String lEndTimeMillisStr = ""+lCurrTimeMillis;
        String lStartTimeMillisStr = ""+( lCurrTimeMillis - 24*3600*1000 );
        List<String[]> lOrders = new ArrayList<>();
        fWatchingInstruments.forEach( ( lInstrument ) -> {
            try {
                Thread.sleep( 250 );    
            } catch (InterruptedException ex) {
                Logger.getLogger(ClientManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            lOrders.addAll( fAccountClient.getOrders( lInstrument ) );
        });

        Collections.sort( lOrders, Collections.reverseOrder((String[] t, String[] t1) -> ( t[ 0 ].compareTo( t1[ 0 ] ) )) );

        List<String[]> lFilteredOrders = new ArrayList<>();
        
        for( String[] lOrder:lOrders ){
            if( (lStartTimeMillisStr.compareTo( lOrder[ 0 ] ) < 0) && (lEndTimeMillisStr.compareTo( lOrder[ 0 ] ) > 0 ) ){
                
                lIsNewValsAdded = true;
                lFilteredOrders.add( new String[]{ fFormat.format( new Date( Long.parseLong( lOrder[ 0 ] ) ) ), 
                                        lOrder[ 1 ], 
                                        lOrder[ 9 ], 
                                        lOrder[ 4 ],
                                        lOrder[ 5 ],
                                        lOrder[ 2 ],
                                        lOrder[ 7 ]});
            }
        }


        if( lIsNewValsAdded ){
            List<String[]> lTrades = new ArrayList<>();
            List<String[]> lFilteredTrades = new ArrayList<>();
            fWatchingInstruments.forEach((lInstrument) -> {
                try {
                    Thread.sleep(250);    
                } catch (InterruptedException ex) {
                    Logger.getLogger(ClientManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                 lTrades.addAll( fAccountClient.getTrades( lInstrument ) );
            });

            if( lTrades.size() > 0 ){
                Collections.sort( lTrades, Collections.reverseOrder((String[] t, String[] t1) -> ( t[ 0 ].compareTo( t1[ 0 ] )*(-1) )) );

                for( String[] lTrade:lTrades ){
                    if( (lStartTimeMillisStr.compareTo( lTrade[ 0 ] ) < 0) && (lEndTimeMillisStr.compareTo( lTrade[ 0 ] ) > 0) ){
                        lIsNewValsAdded = true;
                        lFilteredTrades.add( new String[]{ fFormat.format( new Date( Long.parseLong( lTrade[ 0 ] ) ) ), 
                                                lTrade[ 10 ], 
                                                ( lTrade[ 6 ].contains( "true" ) ? "buy" : "sell" ), 
                                                lTrade[ 2 ],
                                                lTrade[ 3 ],
                                                lTrade[ 9 ]});
                    }
                }
            }
            this.SendTextMessage( "in sync block "+( new Date() ) );
            synchronized( fJTable2WaitObject ){
                 this.SendTextMessage( "in sync block fJTable2WaitObject" );
                ArrayList<String[]> lCleandeOrderRows = getCleanedDataRows( jTable2, lEndTimeMillisStr );
                this.SendTextMessage( "rows cleaned" );
                
                lFilteredOrders.addAll( lCleandeOrderRows );
                this.SendTextMessage( "rows added" );
                
                Collections.sort( lFilteredOrders, Collections.reverseOrder((String[] t, String[] t1) -> ( t[ 0 ].compareTo( t1[ 0 ] )*(-1) )) );
                this.SendTextMessage( "rows sorted" );
                jTable2.setModel(new javax.swing.table.DefaultTableModel(
                    lFilteredOrders.toArray(new String[][]{}),
                    new String [] {
                        "DateTime", "Symbol", "Side", "Price", "Qty", "Id", "Status"
                    }
                ));
            }
            
           /* lFilteredOrders.forEach(
                    (cnsmr)->{
                ( ( DefaultTableModel )jTable2.getModel( ) ).insertRow(0, cnsmr);}
            );*/
            this.SendTextMessage( "in sync block "+( new Date() ) );
            synchronized( fJTable3WaitObject ){
                this.SendTextMessage( "in sync block fJTable3WaitObject" );
                ArrayList<String[]> lCleanedTradeRows = getCleanedDataRows( jTable3, lEndTimeMillisStr );
                this.SendTextMessage( "rows cleaned" );
                
                lFilteredTrades.addAll(lCleanedTradeRows);
                this.SendTextMessage( "rows added" );
                
                Collections.sort( lFilteredTrades, Collections.reverseOrder((String[] t, String[] t1) -> ( t[ 0 ].compareTo( t1[ 0 ] )*(-1) )) );
                this.SendTextMessage( "rows sorted" );
                jTable3.setModel( new javax.swing.table.DefaultTableModel(
                    lFilteredTrades.toArray(new String[][]{}),
                    new String [] {
                        "DateTime", "Symbol", "Side", "Price", "Qty", "Id"
                    }
                ));
                this.SendTextMessage( "model setted" );
            }
           /* lFilteredTrades.forEach(
                    (cnsmr)->{
                ( ( DefaultTableModel )jTable3.getModel( ) ).insertRow(0, cnsmr);}
            );*/
            this.SendTextMessage( "in sync block " );
            int lRowsCount = jTable2.getRowCount();
            if( lRowsCount > 0 ){
               try{
                   jTable2.changeSelection(jTable2.getRowCount() - 1, 0, false, false);
               }catch(Exception e){
                   this.SendTextMessage("error scrolling orders! See log");
                   TAsyncLogQueue.getInstance().AddRecord("error scrolling orders table! See log");
                   TAsyncLogQueue.getInstance().AddRecord(e.getMessage());
               }
            }
            
            lRowsCount = jTable3.getRowCount();
            if( lRowsCount > 0 ){
               try{
                   jTable3.changeSelection(jTable3.getRowCount() - 1, 0, false, false);
               }catch(Exception e){
                   this.SendTextMessage("error scrolling orders! See log");
                   TAsyncLogQueue.getInstance().AddRecord("error scrolling orders table! See log");
                   TAsyncLogQueue.getInstance().AddRecord(e.getMessage());
               }
            }
            
            this.repaint();
           /* jTable2.setModel(
                new DefaultTableModel(
                    lFilteredOrders.toArray(new String[][]{}),
                    new String [] {"DateTime", "Symbol", "Side", "Price", "Qty", "Id", "Status"}
                )
            );
            
            jTable3.setModel(
                new DefaultTableModel(
                    lFilteredTrades.toArray(new String[][]{}),
                    new String [] { "DateTime", "Symbol", "Side", "Price", "Qty", "Id"}
                )
            ); */   
        }  
        TAsyncLogQueue.getInstance().AddRecord( "finish loadHistory! LastUpdateTimeLong value is "+fLastUpdateTimeLong );
    }
    
    private ArrayList<String[]> getCleanedDataRows( JTable aTable, String aEndTimeMillis ){
      //  int lRowCount = ( ( DefaultTableModel )aTable.getModel( ) ).getRowCount();
        
        Object[] lObjectArr = ( ( DefaultTableModel )aTable.getModel( ) ).getDataVector().toArray( new Object[]{} );
        ArrayList<String[]> oResuilt = new ArrayList<>();
        for(int i=0; i<lObjectArr.length; i++){
            
            Object[] lObjArr =((Vector)lObjectArr[i]).toArray(new Object[]{});
            String[] lStringArr = Arrays.copyOf(lObjArr, lObjArr.length, String[].class);
            String lDate = lStringArr[0];
            Calendar cal = Calendar.getInstance();
            int year = cal.get( Calendar.YEAR );
            try {                    
                cal.setTime( ( new SimpleDateFormat( "yyyy MM dd HH:mm:ss" ) ).parse( ""+year+" "+lDate ) );
                String lRes = ""+cal.getTimeInMillis();
                if( lRes.compareTo( aEndTimeMillis ) > 0 ){
                    oResuilt.add(lStringArr);
                }
            } catch (ParseException ex) {
                int t=0;
            }
        }
        
        
          /*  String lDate = ( ( DefaultTableModel )aTable.getModel( ) ).getValueAt(i, 0).toString();
            Calendar cal = Calendar.getInstance();
            int year = cal.get( Calendar.YEAR );
            try {                    
                cal.setTime( ( new SimpleDateFormat( "yyyy MM dd HH:mm:ss" ) ).parse( ""+year+" "+lDate ) );
                String lRes = ""+cal.getTimeInMillis();
                if( lRes.compareTo( aEndTimeMillis ) < 0 ){
                    ( ( DefaultTableModel )aTable.getModel( ) ).removeRow(i);
                    i--;
                    lRowCount--;
                }

            } catch (ParseException ex) {
                int t=0;
            } */
        return oResuilt;
    }
    
    public boolean updateForm(){
        
        jLabel4.setText( "refreshing Form "+( new Date() ) );
        
        SetPositions( fAccountClient.getPositions() );
        
        int lAddedVals = 0;
        TAsyncLogQueue.getInstance().AddRecord( "updateForm LastUpdateTimeLong value is "+fLastUpdateTimeLong );
        
        boolean lIsNewValsAdded = false;
   
        if( fAccountClient != null ){
            
            long lCurrTimeMillis = ( new Date( ) ).getTime();

            String lLastValue = fFormat.format( ( lCurrTimeMillis - 24*3600*1000) );
            
            String lLastValueLong = "";
            int lRows = ( ( DefaultTableModel )jTable2.getModel( ) ).getRowCount();
            if( lRows > 0 ){
                lLastValue = ( ( DefaultTableModel )jTable2.getModel( ) ).getValueAt( lRows - 1, 0 ).toString();
                TAsyncLogQueue.getInstance().AddRecord( "last value is "+lLastValue );
                Calendar cal = Calendar.getInstance();
            
                int year = cal.get( Calendar.YEAR );
                try {
                    
                    cal.setTime( ( new SimpleDateFormat( "yyyy MM dd HH:mm:ss" ) ).parse( ""+year+" "+lLastValue ) );
                   
                    String lRes = ""+cal.getTimeInMillis();
                    if( lRes.compareTo( fLastUpdateTimeLong ) > 0 ){
                        fLastUpdateTimeLong = lRes;
                    }

                } catch (ParseException ex) {
                    int t=0;
                } 
            }
            
            List<String[]> lOrders = new ArrayList<>();
            fWatchingInstruments.forEach( ( lInstrument ) -> {
                try {
                    Thread.sleep( 250 );    
                } catch (InterruptedException ex) {
                    Logger.getLogger(ClientManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                lOrders.addAll( fAccountClient.getOrders( lInstrument ) );
            });

            Collections.sort( lOrders, Collections.reverseOrder((String[] t, String[] t1) -> ( t[ 0 ].compareTo( t1[ 0 ] )*( -1 ) )) );
//String lMaxLongVal
            for( String[] lOrder:lOrders ){
                if( fLastUpdateTimeLong.compareTo( lOrder[ 0 ] ) < 0 ){
                    lAddedVals++;
                    lIsNewValsAdded = true;
                    addOrder( new String[]{ fFormat.format( new Date( Long.parseLong( lOrder[ 0 ] ) ) ), 
                                            lOrder[ 1 ], 
                                            lOrder[ 9 ], 
                                            lOrder[ 4 ],
                                            lOrder[ 5 ],
                                            lOrder[ 2 ],
                                            lOrder[ 7 ]});
                    this.SendTextMessage( "adding orders "+( new Date() ) );
                }
            }

            if( lIsNewValsAdded ){
                
                String[] lRes = Collections.max(lOrders, (String[] t, String[] t1) -> ( t[ 0 ].compareTo( t1[ 0 ] ) ));
                fLastUpdateTimeLong = lRes[ 0 ];
                
                List<String[]> lTrades = new ArrayList<>();

                fWatchingInstruments.forEach((lInstrument) -> {
                    try {
                        Thread.sleep(250);    
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ClientManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                     lTrades.addAll( fAccountClient.getTrades( lInstrument ) );
                });

                Collections.sort( lTrades, Collections.reverseOrder((String[] t, String[] t1) -> ( t[ 0 ].compareTo( t1[ 0 ] )*(-1) )) );

                for( String[] lTrade:lTrades ){
                    if( fLastUpdateTimeLong.compareTo( lTrade[ 0 ] ) < 0 ){
                        lAddedVals++;
                        lIsNewValsAdded = true;
                        addTrade( new String[]{ fFormat.format( new Date( Long.parseLong( lTrade[ 0 ] ) ) ), 
                                            lTrade[ 10 ], 
                                            ( lTrade[ 6 ].contains( "true" ) ? "buy" : "sell" ), 
                                            lTrade[ 2 ],
                                            lTrade[ 3 ],
                                            lTrade[ 9 ]});
                    }
                }
            }
        }
        
        TAsyncLogQueue.getInstance().AddRecord( "finish updateForm! LastUpdateTimeLong value is "+fLastUpdateTimeLong );
        jLabel4.setText( "Form refreshed "+ lAddedVals + " vals added "+( new Date() ) );
        return lIsNewValsAdded;
        
    }
    
    public boolean InitLastUpdate(){
        boolean lIsNewValsAdded = false;
        long lCurrTimeMillis = ( new Date( ) ).getTime();
        
        if( fAccountClient != null ){
            List<String[]> lOrders = new ArrayList<>();

            String lLastValue = fFormat.format( ( lCurrTimeMillis - 24*3600*1000) );
            String lLastValueLong = "";
            int lRows = ( ( DefaultTableModel )jTable2.getModel( ) ).getRowCount();
            if( lRows > 0 ){
                lLastValue = ( ( DefaultTableModel )jTable2.getModel( ) ).getValueAt( lRows - 1, 0 ).toString();
            }

            Calendar cal = Calendar.getInstance();
            
            int year = cal.get( Calendar.YEAR );
            try {
                cal.setTime( (new SimpleDateFormat("yyyy MM dd HH:mm:ss")).parse( ""+year+" "+lLastValue) );
                lLastValueLong = ""+cal.getTimeInMillis();
            } catch (ParseException ex) {
                int t=0;
            }
            
            fWatchingInstruments.forEach( ( lInstrument ) -> {
                try {
                    Thread.sleep( 250 );    
                } catch (InterruptedException ex) {
                    Logger.getLogger(ClientManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                lOrders.addAll( fAccountClient.getOrders( lInstrument ) );
            });

            Collections.sort( lOrders, Collections.reverseOrder((String[] t, String[] t1) -> ( t[ 0 ].compareTo( t1[ 0 ] )*( -1 ) )) );

            for( String[] lOrder:lOrders ){
                if( fLastUpdateTimeLong.compareTo( lOrder[ 0 ] ) < 0 ){
                    lIsNewValsAdded = true;
                    fLastUpdateTimeLong = lOrder[ 0 ];
                }
            }
        }
           
        return lIsNewValsAdded;
        
    }
    

    static class MyTableModel extends DefaultTableModel {

        List<Color> rowColours = Arrays.asList(
            Color.RED,
            Color.GREEN,
            Color.CYAN
        );

        private MyTableModel(String[][] toArray, String[] fTableTitle) {
            super(toArray, fTableTitle);
        }

        public void setRowColour(int row, Color c) {
            rowColours.set(row, c);
            fireTableRowsUpdated(row, row);
        }

        public Color getRowColour(int row) {
            return rowColours.get(row);
        }

        
    }

    
    private void updatePositions(){
        
        ArrayList< String[] > lAllPositions = new ArrayList<String[]>();
        
        if( fPositions != null ){
            
            for( int i = 0; i < fPositions.length; i++ ){
                if( !jCheckBox1.isSelected() || (jCheckBox1.isSelected() && 
                    ( !(Double.parseDouble( fPositions[ i ][ 1 ] )==0.0) || !(Double.parseDouble( fPositions[ i ][ 2 ] )==0.0) || !fHoldedMap.getOrDefault( fPositions[i][0], lZeroVal ).equals( lZeroVal )  ) ) ){    
                   //     ( !fPositions[ i ][ 1 ].equals( lZeroVal ) || !fPositions[ i ][ 2 ].equals( lZeroVal ) || !fHoldedMap.getOrDefault( fPositions[i][0], lZeroVal ).equals( lZeroVal )  ) ) ){
                    
                    lAllPositions.add( new String[]{ fPositions[i][0], fPositions[i][1], fPositions[i][2], fHoldedMap.getOrDefault( fPositions[i][0], lZeroVal ) } );
                    
                    TAsyncLogQueue.getInstance().AddRecord( "in updatePositions " + fPositions[ i ][ 0 ]+ "\t" + fPositions[ i ][ 1 ] + "\t" + fPositions[ i ][ 2 ] );
                }
   
            }
            
            MyTableModel lModel = new MyTableModel(
                   lAllPositions.toArray( new String[][]{} ), 
                   fTableTitle
            );
                    
            jTable1.setModel(lModel);
            
            
            
     
        
        } 
    }
    
    public synchronized void SendTextMessage( String aMessage ){
        jTextArea1.setText( jTextArea1.getText() + aMessage + "\r\n" );    
    }
    
    public void addClient( TExchangeClient aAccountClient ){
        fAccountClient = aAccountClient;
    }
    
    /**
     * @param args the command line arguments
     */
    public void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ClientManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ClientManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ClientManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ClientManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                setVisible(true);
            }
        });
        
        if( args != null ){
            lStatusThread.start();
            lHistoryThread.start();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JTextArea jTextArea1;
    private java.awt.Label label1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void addOrder(String[] aOrder) {
        
        synchronized( fJTable2WaitObject ){
            if( ( ( DefaultTableModel )jTable2.getModel( ) ).getColumnCount() == aOrder.length){//7
                ( ( DefaultTableModel )jTable2.getModel( ) ).addRow( aOrder );

                int lRowsCount = jTable2.getRowCount();
                if( lRowsCount > 0 ){
                   try{
                       jTable2.changeSelection(jTable2.getRowCount() - 1, 0, false, false);
                   }catch(Exception e){
                       this.SendTextMessage("error scrolling orders! See log");
                       TAsyncLogQueue.getInstance().AddRecord("error scrolling orders table! See log");
                       TAsyncLogQueue.getInstance().AddRecord(e.getMessage());

                   }
                }
                this.repaint();

                fLastUpdateTimeLong = ""+( new Date( ) ).getTime(); 

            } else {
                TAsyncLogQueue.getInstance().AddRecord("cant draw  Order! Order is " + Arrays.toString( aOrder ) );
            }

        }   

    }
    
    @Override
    public void addTrade(String[] aTrade) {
        synchronized( fJTable3WaitObject ){
            int lWidth = ( ( DefaultTableModel )jTable3.getModel( ) ).getColumnCount();
            if( lWidth == aTrade.length ){//6
                ( ( DefaultTableModel )jTable3.getModel( ) ).addRow( aTrade );

                int lRowsCount = jTable3.getRowCount();
                if( lRowsCount > 0 ){
                try{
                    jTable3.changeSelection(jTable3.getRowCount() - 1, 0, false, false);
                }catch(Exception e){
                    this.SendTextMessage("error scrolling trade! See log");
                    TAsyncLogQueue.getInstance().AddRecord(e.getMessage());
                }

                }
                this.repaint();
                fLastUpdateTimeLong = ""+( new Date( ) ).getTime(); 
            } else {
                TAsyncLogQueue.getInstance().AddRecord("cant draw  Trade! Trade is " + Arrays.toString( aTrade )+" table is "+lWidth );
            }            
        }
    }
}
