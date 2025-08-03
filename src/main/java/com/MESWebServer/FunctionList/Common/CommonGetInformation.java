package com.MESWebServer.FunctionList.Common;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class CommonGetInformation {
    static public String GetLocalIP(){
        InetAddress local = null;
        try {
            local = InetAddress.getLocalHost();
        }
        catch (UnknownHostException e ) {
            e.printStackTrace();
        }
        if( local == null ) {
            return "";
        }
        else {
            return local.getHostAddress();
        }
    }
}
