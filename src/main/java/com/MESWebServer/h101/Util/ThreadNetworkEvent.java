package com.MESWebServer.h101.Util;


public interface ThreadNetworkEvent {

      void onListen(String msg);
      void onConnect(String msg);
      void onDisConnect(String msg);
      public String onSyncSend(String sendMessage);
      public void onAsyncRecieve(Object obj);
}
