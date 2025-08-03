package com.MESWebServer.h101.Core;


import com.miracom.oneoone.transceiverx.*;
import com.miracom.oneoone.transceiverx.message.MessageImpl;
import com.miracom.oneoone.transceiverx.message.MultipartMessageImpl;
import com.miracom.oneoone.transceiverx.parser.AckConfirm;
import com.miracom.oneoone.transceiverx.parser.DeliveryType;
import com.miracom.oneoone.transceiverx.parser.IOIStreamMessage;
import com.miracom.oneoone.transceiverx.parser.StreamSendMessage;
import com.miracom.oneoone.transceiverx.pending.AsyncWaitHolder;
import com.miracom.oneoone.transceiverx.pending.BlockingQueue;
import com.miracom.oneoone.transceiverx.pending.SyncWaitHolder;
import com.miracom.oneoone.transceiverx.pending.WaitHolder;
import com.miracom.oneoone.transceiverx.session.MessageDispatcher;
import com.miracom.oneoone.transceiverx.session.SessionImpl;
import com.miracom.oneoone.transceiverx.session.agent.*;
import com.miracom.oneoone.transceiverx.session.connection.Configuration;
import com.miracom.oneoone.transceiverx.session.connection.Connection;
import com.miracom.oneoone.transceiverx.session.connection.ConnectionEvent;
import com.miracom.oneoone.transceiverx.session.handler.*;
import com.miracom.oneoone.transceiverx.session.management.SessionMonitor;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class CusSessionImpl extends SessionImpl {

    public final int SESSION_STATUS_STARTED = 1;
    public final int SESSION_STATUS_CONNECTED = 2;
    protected final long DEFAULT_TTL = 30000L;
    protected int sessionMode = 2;
    int state;
    Socket socket;
    private final Object socketMutex = new Object();
    String sessionId;
    String sessionIdConfig;
    Connection connection;
    private boolean secure = false;
    MessageDispatcher dispatcher;
    long defaultTTL = 30000L;
    BlockingQueue messageQueue;
    WaitHolder ackForDefaultWaitHolder;
    WaitHolder ackForReplyWaitHolder;
    WaitHolder syncRequestWaitHolder;
    WaitHolder asyncRequestWaitHolder = null;
    AsyncReplyHandler asyncReplyHandler;
    SyncReplyHandler syncReplyHandler;
    ControlMessageHandler controlMessageHandler;
    D3MessageHandler d3MessageHandler;
    ResAckHandler resAckHandler;
    SendMessageHandler sendMessageHandler;
    SessionContextHandler sessionContextHandler;
    GetMessageAgent getMessageAgent;
    TuneChannelAgent tuneChannelAgent;
    SessionEventAgent sessionEventAgent;
    MessageConsumerAgent messageConsumerAgent;
    SendMessageAgent sendMessageAgent;
    SessionContextAgent sessionContextAgent;
    PushDeliveryAgent pushDeliveryAgent;
    SessionMonitor monitor;
    Configuration configuration;
    private boolean loggedIn = false;

    public CusSessionImpl() {
        this.setup();
    }

    public void finalize() {
        this.destroy();
    }

    protected void setup() {
        this.dispatcher = new MessageDispatcher();
        //this.monitor = new SessionMonitor(this);
        this.monitor = new CusSessionMonitor(this);
        this.configuration = new Configuration();
        this.messageQueue = new BlockingQueue();
        this.ackForDefaultWaitHolder = new SyncWaitHolder();
        this.ackForReplyWaitHolder = new SyncWaitHolder();
        this.syncRequestWaitHolder = new SyncWaitHolder();
        this.syncReplyHandler = new SyncReplyHandler(this.syncRequestWaitHolder);
        this.controlMessageHandler = new ControlMessageHandler();
        this.d3MessageHandler = new D3MessageHandler(this.messageQueue);
        this.resAckHandler = new ResAckHandler(this.ackForDefaultWaitHolder, this.ackForReplyWaitHolder, this.monitor);
        this.sendMessageHandler = new SendMessageHandler(this.messageQueue);
        this.sessionContextHandler = new SessionContextHandler();
        this.getMessageAgent = new GetMessageAgent(this, this.messageQueue);
        this.tuneChannelAgent = new TuneChannelAgent(this);
        this.sessionEventAgent = new SessionEventAgent(this);
        this.messageConsumerAgent = new MessageConsumerAgent(this);
        this.sessionContextAgent = new SessionContextAgent(this);
        this.sendMessageAgent = new SendMessageAgent(this);
        this.dispatcher.registerHandler(this.resAckHandler);
        this.dispatcher.registerHandler(this.syncReplyHandler);
        this.dispatcher.registerHandler(this.sendMessageHandler);
        this.dispatcher.registerHandler(this.controlMessageHandler);
        this.dispatcher.registerHandler(this.d3MessageHandler);
        this.dispatcher.registerHandler(this.sessionContextHandler);
    }

    public void create(String session_id, int mode) throws TrxException {
        this.sessionIdConfig = session_id;
        this.sessionMode = mode;
    }

    public synchronized void destroy() {
        try {
            this.disconnect();
        } catch (TrxException var2) {
        }

        if (this.asyncRequestWaitHolder != null) {
            this.asyncRequestWaitHolder.dispose();
        }

    }

    public void connect(String connectString) throws TrxException {
        this.connect(connectString, this.configuration.isAutoReconnect(), (String)null);
    }

    public void connect(String connectString, String bindAddress) throws TrxException {
        this.connect(connectString, this.configuration.isAutoReconnect(), bindAddress);
    }

    public void connect(String connectString, boolean autoReconnect) throws TrxException {
        this.connect(connectString, autoReconnect, (String)null);
    }

    public synchronized void connect(String connectString, boolean autoReconnect, String bindAddress) throws TrxException {
        if (this.connection != null) {
            throw new TrxException(27);
        } else {
            this.configuration.setAutoReconnect(autoReconnect);
            this.connection = new Connection(connectString, this.configuration, this.dispatcher);
            this.connection.addConnectionListener(this);
            this.connection.addConnectionListener(this.sessionContextAgent);
            this.connection.addConnectionListener(this.tuneChannelAgent);
            this.connection.addConnectionListener(this.getMessageAgent);
            this.connection.addConnectionListener(this.sessionEventAgent);
            this.connection.addConnectionListener(this.sendMessageAgent);
            this.connection.addConnectionListener(this.monitor);
            this.connection.setBindAddress(bindAddress);
            this.connection.setSecure(this.secure);

            Thread thread;
            try {
                this.connection.connect();
                thread = new Thread(this.connection, "Connection");
                thread.setDaemon(true);
                thread.start();
            } catch (Exception var5) {
                this.connection.removeConnectionListener(this);
                this.connection.removeConnectionListener(this.sessionContextAgent);
                this.connection.removeConnectionListener(this.tuneChannelAgent);
                this.connection.removeConnectionListener(this.getMessageAgent);
                this.connection.removeConnectionListener(this.sessionEventAgent);
                this.connection.close();
                this.connection = null;
                throw new TrxException(10, var5);
            }

            this.setStarted(true);
            if (this.isPushDeliveryMode()) {
                this.pushDeliveryAgent = new PushDeliveryAgent(this);
                thread = new Thread(this.pushDeliveryAgent, "PushDeliveryAgent");
                thread.setDaemon(true);
                thread.start();
            }

            this.startManagementThread();
        }
    }

    public void disconnect() throws TrxException {
        if (this.isStarted()) {
            if (this.connection == null) {
                throw new TrxException(5);
            } else {
                this.setStarted(false);
                this.connection.close();
                this.connection = null;
                if (this.pushDeliveryAgent != null) {
                    this.pushDeliveryAgent.stop();
                }

                this.pushDeliveryAgent = null;
            }
        }
    }

    private void initializeAsyncRequestReplySystem() {
        if (!this.isAsyncRequestReplySystemInitialized()) {
            this.asyncRequestWaitHolder = new AsyncWaitHolder();
            this.asyncReplyHandler = new AsyncReplyHandler(this.asyncRequestWaitHolder);
            this.dispatcher.registerHandler(this.asyncReplyHandler);
        }
    }

    public void tuneMulticast(String channel) throws TrxException {
        this.tuneChannelAgent.tuneMulticast(channel);
    }

    public void tuneUnicast(String channel) throws TrxException {
        this.tuneChannelAgent.tuneUnicast(channel);
    }

    public void tuneGuaranteedMulticast(String queue) throws TrxException {
        this.tuneChannelAgent.tuneGuaranteedMulticast(queue);
    }

    public void tuneGuaranteedUnicast(String queue) throws TrxException {
        this.tuneChannelAgent.tuneGuaranteedUnicast(queue);
    }

    public void untuneMulticast(String channel) throws TrxException {
        this.tuneChannelAgent.untuneMulticast(channel);
    }

    public void untuneUnicast(String channel) throws TrxException {
        this.tuneChannelAgent.untuneUnicast(channel);
    }

    public void untuneGuaranteedMulticast(String queue) throws TrxException {
        this.tuneChannelAgent.untuneGuaranteedMulticast(queue);
    }

    public void untuneGuaranteedUnicast(String queue) throws TrxException {
        this.tuneChannelAgent.untuneGuaranteedUnicast(queue);
    }

    public Message createMessage() throws TrxException {
        return new MessageImpl();
    }

    public MultipartMessage createMultipartMessage() throws TrxException {
        return new MultipartMessageImpl();
    }

    public void sendUnicast(Message message) throws TrxException {
        this.sendMessageAgent.sendUnicast(message);
    }

    public void sendMulticast(Message message) throws TrxException {
        this.sendMessageAgent.sendMulticast(message);
    }

    public void sendRequestAsync(Message message, Object hint) throws TrxException {
        this.sendMessageAgent.sendRequestAsync(message, hint);
    }

    public Message sendRequest(Message message) throws TrxException {
        return this.sendMessageAgent.sendRequest(message);
    }

    public void sendReply(Message request, Message reply) throws TrxException {
        this.sendMessageAgent.sendReply(request, reply);
    }

    public void sendGuaranteedMulticast(String queue, Message message) throws TrxException {
        this.sendMessageAgent.sendGuaranteedMulticast(queue, message);
    }

    public void sendGuaranteedUnicast(String queue, Message message) throws TrxException {
        this.sendMessageAgent.sendGuaranteedUnicast(queue, message);
    }

    public void sendConfirm(Message message, boolean confirm) throws TrxException {
        this.sendMessageAgent.sendConfirm(message, confirm);
    }

    public boolean isStarted() {
        return 0 != (this.state & 1);
    }

    public synchronized void setStarted(boolean started) {
        if (started) {
            this.state |= 1;
        } else {
            this.state &= -2;
        }

    }

    public boolean isConnected() {
        return 0 != (this.state & 2);
    }

    public synchronized void setConnected(boolean connected) {
        if (connected) {
            this.state |= 2;
        } else {
            this.state &= -3;
        }

    }

    public int getDeliveryMode() {
        return this.sessionMode;
    }

    public String getSessionID() {
        return this.sessionId;
    }

    public void setSessionId(String id) {
        this.sessionId = id;
    }

    public String getReplyChannel() {
        return this.tuneChannelAgent.getReplyChannel();
    }

    public int getSessionMode() {
        return this.sessionMode;
    }

    public void setSessionMode(int mode) {
        this.sessionMode = mode;
    }

    public long getDefaultTTL() {
        return this.defaultTTL;
    }

    public void setDefaultTTL(long ttl) {
        this.defaultTTL = ttl;
    }

    public void addMessageConsumer(MessageConsumer consumer) {
        this.messageConsumerAgent.addMessageConsumer(consumer);
    }

    public void removeMessageConsumer(MessageConsumer consumer) {
        this.messageConsumerAgent.removeMessageConsumer(consumer);
    }

    public void addSessionEventListener(SessionEventListener listener) {
        this.sessionEventAgent.addSessionEventListener(listener);
    }

    public void removeSessionEventListener(SessionEventListener listener) {
        this.sessionEventAgent.removeSessionEventListener(listener);
    }

    public void notifyMessage(Message refmsg, Message msg, Object hint) throws TrxException {
        this.messageConsumerAgent.notifyMessage(refmsg, msg, hint);
    }

    public void setSocket(Socket socket) {
        synchronized(this.socketMutex) {
            this.socket = socket;
        }
    }

    public void sendMessage(StreamSendMessage message) throws TrxException {
        this.sendMessageAgent.sendMessage(message);
    }

    public Message getMessage(long ttl) throws TrxException {
        return this.getMessageAgent.getMessage(ttl);
    }

    public void setAutoRecovery(boolean autoRecovery) {
        this.configuration.setAutoReconnect(autoRecovery);
        if (this.connection != null) {
            this.connection.setAutoReconnect(this.configuration.isAutoReconnect());
        }

    }

    public void addMsgTarget(MsgTarget target) {
        this.addMessageConsumer(target);
        this.addSessionEventListener(target);
        target.setSession(this);
    }

    public String getConnectString() {
        return this.connection == null ? null : this.connection.getConnectionString();
    }

    public boolean isAutoRecovery() {
        return this.connection != null ? this.connection.isAutoReconnect() : this.configuration.isAutoReconnect();
    }

    public boolean isResendOnError() {
        return this.connection != null ? this.connection.isResendOnError() : this.configuration.isResendOnError();
    }

    public boolean isSecure() {
        return this.secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public void setTrustStore(String store) {
        if (null != store && 0 < store.trim().length()) {
            System.setProperty("javax.net.ssl.trustStore", store);
        }

    }

    public boolean isInnerStationDeliveryMode() {
        return (this.getDeliveryMode() & 2) == 2;
    }

    public boolean isPushDeliveryMode() {
        return (this.getDeliveryMode() & 1) != 1;
    }

    public void sendConfirm(Object message, boolean confirm) throws TrxException {
        IOIStreamMessage streamMsg;
        if (message instanceof Message) {
            streamMsg = ((Message)message).getStreamMessage();
        } else {
            if (!(message instanceof IOIStreamMessage)) {
                throw new TrxException(8);
            }

            streamMsg = (IOIStreamMessage)message;
        }

        AckConfirm confirmMsg = new AckConfirm(streamMsg.getMessageKey());
        confirmMsg.setCode(confirm ? 1 : 0);
        confirmMsg.setConfirmSessionID(this.getSessionID());
        streamMsg.getMessageKey().setSessionID(this.getSessionID());
        this.send((IOIStreamMessage)confirmMsg);
        streamMsg.getMessageKey().setDeliveryType(DeliveryType.setConfirmedMessage(streamMsg.getMessageKey().getDeliveryType()));
    }

    public void sendMessage(IOIStreamMessage message) throws TrxException {
        short stream_id = message.getMessageKey().message_stream_id;
        if (stream_id == 1001) {
            this.sendMessageAgent.sendMessage((StreamSendMessage)message);
        }

    }

    public void send(byte[] data) throws TrxException {
        try {
            synchronized(this.socketMutex) {
                OutputStream os = this.socket.getOutputStream();
                os.write(data);
                os.flush();
            }
        } catch (Throwable var6) {
            throw new TrxException(9, var6);
        }
    }

    public void send(IOIStreamMessage msg) throws TrxException {
        if (!this.isConnected()) {
            throw new TrxException(5);
        } else {
            try {
                synchronized(this.socketMutex) {
                    OutputStream os = this.socket.getOutputStream();
                    BufferedOutputStream bos = new BufferedOutputStream(os, 1024);
                    msg.flush(bos);
                    os.flush();
                }
            } catch (Throwable var7) {
                throw new TrxException(9, var7);
            }
        }
    }

    public IOIStreamMessage recv() {
        try {
            InputStream in = this.socket.getInputStream();
            synchronized(in) {
                return IOIStreamMessage.createMessage(in);
            }
        } catch (Throwable var5) {
            var5.printStackTrace();
            return null;
        }
    }

    public void onConnected(ConnectionEvent event) {
        this.setSocket((Socket)event.getEndpoint());
        this.setConnected(true);
    }

    public void onDisconnected(ConnectionEvent event) {
        this.setConnected(false);
        this.setLoggedIn(false);
        this.setSocket((Socket)null);

    }

    public WaitHolder getAckForDefaultWaitHolder() {
        return this.ackForDefaultWaitHolder;
    }

    public WaitHolder getAckForReplyWaitHolder() {
        return this.ackForReplyWaitHolder;
    }

    public synchronized WaitHolder getAsyncRequestWaitHolder() {
        if (this.isAsyncRequestReplySystemInitialized()) {
            this.initializeAsyncRequestReplySystem();
        }

        return this.asyncRequestWaitHolder;
    }

    private boolean isAsyncRequestReplySystemInitialized() {
        return this.asyncRequestWaitHolder != null && this.asyncReplyHandler != null;
    }

    public WaitHolder getSyncRequestWaitHolder() {
        return this.syncRequestWaitHolder;
    }

    public Object[] getMessageConsumers() {
        return this.messageConsumerAgent.getMessageConsumers();
    }

    public String getSessionIdConfig() {
        return this.sessionIdConfig;
    }

    public void setSessionIdConfig(String string) {
        this.sessionIdConfig = string;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public boolean isLoggedIn() {
        synchronized(this.socketMutex) {
            return this.loggedIn;
        }
    }

    public void setLoggedIn(boolean loggedIn) {
        synchronized(this.socketMutex) {
            this.loggedIn = loggedIn;
        }
    }

    private void startManagementThread() {
        this.monitor.setPingInterval(this.connection.getPingInterval());
        this.monitor.setPingTimeOut(this.connection.getPingTimeout());
        this.monitor.start();
    }

    public void startIntervalPing(){
        this.monitor.setPingInterval(this.connection.getPingInterval());
        this.monitor.setPingTimeOut(this.connection.getPingTimeout());
        this.monitor.start();
    }

    public String getProductVersion() throws TrxException {
        return IOIStreamMessage.getResourceString("product.version");
    }

    public String getOSName() {
        return System.getProperty("os.name");
    }

    public String getPlatformName() {
        return "java";
    }
}
