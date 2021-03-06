package net.lshift.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.messagepatterns.unicast.*;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thin wrapper around the Java Message Patterns sender implementation so that
 * we benefit from the channel / connection reliability, whilst hiding the
 * Unicast abstractions that are not needed here.
 */
public class MessageSender {
    
    private static transient Logger LOG = LoggerFactory.getLogger(MessageSender.class);
    
    public static final String EXCHANGE_TYPE = "fanout";
    
    public static final boolean AUTO_DELETE = false;
    
    public static final boolean DURABLE = false;
    

    protected SenderImpl sender = new SenderImpl();

    public MessageSender(Connector connector, final String exchangeName) throws Exception {
        sender.setConnector(connector);
        sender.setExchangeName(exchangeName);
        
        addSenderSetupListener(new ChannelSetupListener() {
            public void channelSetup(Channel channel) throws IOException {
                channel.exchangeDeclare(exchangeName, EXCHANGE_TYPE, DURABLE, AUTO_DELETE, null);
            }
        });
    }
    
    public Connector getConnector() {
        return sender.getConnector();
    }

    public String getExchangeName() {
        return sender.getExchangeName();
    }

    public void setExchangeName(String exchangeName) {
        sender.setExchangeName(exchangeName);
    }

    public boolean isTransactional() {
        return sender.isTransactional();
    }

    public void setTransactional(boolean transactional) {
        sender.setTransactional(transactional);
    }

    public void addChannelSetupListener(ChannelSetupListener channelSetup) {
        sender.addSetupListener(channelSetup);
    }

    public void removeChannelSetupListener(ChannelSetupListener channelSetup) {
        sender.removeSetupListener(channelSetup);
    }

    public void addSenderSetupListener(ChannelSetupListener channelSetup) {
        sender.addSetupListener(channelSetup);
    }

    public void removeSenderSetupListener(ChannelSetupListener channelSetup) {
        sender.removeSetupListener(channelSetup);
    }

    public void addMessageSentListener(MessageSentListener listener) {
        sender.addMessageSentListener(listener);
    }

    public void removeMessageSentListener(MessageSentListener listener) {
        sender.removeMessageSentListener(listener);
    }

    public void start() throws Exception {
        LOG.debug("Starting MessageSender for exchange {}...", getExchangeName());
        sender.init();
        LOG.debug("MessageSender started.");
    }

    public void stop() throws IOException {
        LOG.debug("Stopping MessageSender for exchange {}...", getExchangeName());
        try {
            sender.deleteExchange();
        } catch(Exception e) {
            if(e instanceof IOException) {
                throw (IOException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
        
        sender.close();
        LOG.debug("MessageSender stoppped.");
    }

    public Message createMessage() {
        Message msg = sender.createMessage();
        
        // Set empty routing key as we want 1:N fanout delivery by default
        msg.setTo("");
        return msg;
    }

    public void send(Message m) throws Exception {
        sender.send(m);
    }
}