package service;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "java:/jms/queue/PetQueue")
    },
    name = "JMSConsumer",
    messageListenerInterface = MessageListener.class
)
public class JMSConsumer implements MessageListener {
    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                log.info(((TextMessage) message).getText());
            }
        } catch (Exception e) {
            log.error("Error processing JMS message", e);
        }
    }
}