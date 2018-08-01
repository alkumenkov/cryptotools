/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alk.accounttools;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author wellington
 */
public class TAssetMatrix {
    
    private String fMatrixName;
    private HashMap<String, Double> fCoreMap = new HashMap();
    private HashMap<String, Double> fPricesMap = new HashMap();
    
    public TAssetMatrix( String aMatrixName ){
        fMatrixName = aMatrixName;
    }
    
    public void SetMatrixName( String aMatrixName ){
        fMatrixName = aMatrixName;
    }
    
    public String GetMatrixName( ){
        return fMatrixName;
    }
  
  
    public HashMap<String, Double> getCoreMap(){
        return fCoreMap;
    }
    
    public boolean addVals(String aAsset, String aQty){
        boolean lRes = false;
        
        try{            
            fCoreMap.put( aAsset, (fCoreMap.getOrDefault(aAsset, 0.0 )+Double.parseDouble( aQty )) );   
            lRes = true;
        }catch( Exception e ){
            lRes = false; 
        }
        
        return lRes;
    }
    
    public boolean addVals(String aAsset, Double aQty){
        boolean lRes = false;
        
        try{            
            fCoreMap.put( aAsset, (fCoreMap.getOrDefault(aAsset, 0.0 )+ aQty) );   
            lRes = true;
        }catch( Exception e ){
            lRes = false; 
        }
        
        return lRes;
    }
    
    @Override
    public String toString(){
        StringBuilder lBuilder = new StringBuilder();
        Set<String> lKeys = fCoreMap.keySet();
        lKeys.forEach( lKey->{
           lBuilder.append( lKey + "\t" + fCoreMap.get( lKey ) + ";\r\n" );
        } );
  //      fCoreMap.
   
        return "name: "+fMatrixName+"\r\nvalues:"+lBuilder.toString();
    }
    
    public TAssetMatrix plus( TAssetMatrix aOtherMatgix ){
        
        TAssetMatrix lResMatrix = new TAssetMatrix( this.GetMatrixName() + "+" + aOtherMatgix.GetMatrixName() );
        
        Set<String> lKeys = fCoreMap.keySet();
        lKeys.forEach( lKey->{
            lResMatrix.addVals( lKey, fCoreMap.get(lKey ) );  
        } );
        
        
        lKeys = aOtherMatgix.getCoreMap().keySet();
        lKeys.forEach( lKey->{
            lResMatrix.addVals( lKey, aOtherMatgix.getCoreMap().get( lKey ) );  
        } );

        return lResMatrix;
    }
    
    public TAssetMatrix minus( TAssetMatrix aOtherMatgix ){
        
        TAssetMatrix lResMatrix = new TAssetMatrix( this.GetMatrixName() + "-" + aOtherMatgix.GetMatrixName() );
        
        Set<String> lKeys = fCoreMap.keySet();
        lKeys.forEach( lKey->{
            lResMatrix.addVals( lKey, fCoreMap.get(lKey ) );  
        } );
       
        lKeys = aOtherMatgix.getCoreMap().keySet();
        lKeys.forEach( lKey->{
            lResMatrix.addVals( lKey, aOtherMatgix.getCoreMap().get( lKey )*( -1 ) );  
        } );
        
        return lResMatrix;
  
    }
    
    public double getSummary( String aAsset, String aCurrDate ){
        double lRes = 0;
        
        Set<String> lKeys = fCoreMap.keySet();
        String[] lKeyVals = lKeys.toArray( new String[]{} );
        for( int i=0; i<lKeyVals.length; i++ ){
            Double lPair = fCoreMap.get( lKeyVals[i] );
            Double lPrice =fPricesMap.get( lKeyVals[i] );
            if( lPrice == null ){
                lPrice = TPriceConverter.getInstance().convertCurrency( lKeyVals[i], aAsset, aCurrDate );
                fPricesMap.put(lKeyVals[i], lPrice);
            }
            System.out.println( lKeyVals[i]+":"+lPrice );
            lRes += lPrice*lPair;
            int t=0;
        }
        return lRes;
    }
    
    public TreeMap<String, String[]> getAdvancedSummary( String aAsset, String aCurrDate ){
        TreeMap<String, String[]> oRes = new TreeMap<String, String[]>();
        
        Set<String> lKeys = fCoreMap.keySet();
        
        String[] lKeyVals = lKeys.toArray( new String[]{} );
        for( int i=0; i<lKeyVals.length; i++ ){
            
            Double lPair = fCoreMap.get( lKeyVals[i] );
            Double lPrice =fPricesMap.get( lKeyVals[i] );
            if( lPrice == null ){
                lPrice = TPriceConverter.getInstance().convertCurrency( lKeyVals[i], aAsset, aCurrDate );
                fPricesMap.put(lKeyVals[i], lPrice);
            }
            
            oRes.put( lKeyVals[i], new String[]{ String.format("%4.4f", lPair), 
                                                String.format("%4.4f",lPrice), 
                                                String.format("%4.4f",lPair*lPrice)});
            int t=0;
        }
        
        return oRes;
    }
    
}
