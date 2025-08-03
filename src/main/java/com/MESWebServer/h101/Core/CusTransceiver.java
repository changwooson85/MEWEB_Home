package com.MESWebServer.h101.Core;


import com.miracom.oneoone.transceiverx.ErrorCode;
import com.miracom.oneoone.transceiverx.Session;
import com.miracom.oneoone.transceiverx.TrxException;
import com.miracom.oneoone.transceiverx.process.ManagementProcessor;

public class CusTransceiver {
    private static String processName;
    private static short processPort;
    private static ManagementProcessor managementProcessor;

    public CusTransceiver(String process_name, int port) throws TrxException {
        if (process_name != null && process_name.length() > 0) {
            this.init(process_name, (short)port);
        }

    }
    public void init(String process_name, int port) throws TrxException {
        processName = process_name;
        processPort = (short)port;
        if (processName != null && processName.length() != 0) {
            managementProcessor = new ManagementProcessor();

            try {
                managementProcessor.connect("localhost:" + processPort);
            } catch (TrxException var4) {
                if (var4.getErrorCode() == 5) {
                    managementProcessor.close();
                }

                throw var4;
            }
        }

    }
    private static boolean validSessionMode(int mode) {
        return mode >= 0 && mode <= 3;
    }

    public static Session createSession(String session_id, int mode) throws TrxException {
        if (!validSessionMode(mode)) {
            throw new TrxException(19);
        } else {
            Session ss = new CusSessionImpl();
            ss.create(session_id, mode);
            return ss;
        }
    }

    public void term() throws TrxException {
        if (managementProcessor != null) {
            managementProcessor.close();
        }

    }

    public static String getProcessName() {
        return processName;
    }

    public static String errorLookup(ErrorCode error) {
        return error.toString();
    }
}
