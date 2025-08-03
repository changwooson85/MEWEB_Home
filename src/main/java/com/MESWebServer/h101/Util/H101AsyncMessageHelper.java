package com.MESWebServer.h101.Util;




public class H101AsyncMessageHelper {

    public static final String INTENT_SERVER_MESSAGE = "com.barcodeapplication.action.INTENT_SERVER_MESSAGE";
    public static final String INTENT_SERVER_MESSAGE_KEY = "server_message_key";
    private static H101AsyncMessageHelper mH101AsyncMessageHelper;


    public static H101AsyncMessageHelper GetInstance() {
        if(mH101AsyncMessageHelper == null)
            mH101AsyncMessageHelper = new H101AsyncMessageHelper();

        return mH101AsyncMessageHelper;
    }
    public void SendBroadCast(String message){
        if (message == null) return;

    }
}
