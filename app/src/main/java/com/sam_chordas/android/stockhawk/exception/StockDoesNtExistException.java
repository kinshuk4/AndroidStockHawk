/**
 * Copyright (C) August 2016
 * The Stock Hawk project
 */

package com.sam_chordas.android.stockhawk.exception;


public class StockDoesNtExistException extends Exception {

    public StockDoesNtExistException(){
        super();
    }
    public StockDoesNtExistException(String msg){
        super(msg);
    }

}
