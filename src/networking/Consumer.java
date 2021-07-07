package networking;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQQueueBrowser;
import utils.ClientDataSingleton;

import javax.jms.*;

public class Consumer {

    private MessageConsumer consumer;
    private Session session;
    private Destination destination;
    private boolean consumerStarted;

    public Consumer() {
        consumerStarted = false;
    }

    public void startConsumer() throws JMSException {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnectionFactory.DEFAULT_BROKER_URL);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        destination = session.createQueue(ClientDataSingleton.getInstance().userID);

        consumer = session.createConsumer(destination);
        consumerStarted = true;
    }

    public String consumeMessage() throws JMSException, IllegalAccessException {
        if (!consumerStarted) throw new IllegalAccessException();
        Message message = consumer.receive();

        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            String text = textMessage.getText();
            System.out.println("Recebido: " + text);
            return text;
        } else {
            System.out.println("Recebido: " + message);
            return "Not text message!";
        }
    }

    public boolean hasMessages() throws JMSException {
        QueueBrowser browser = session.createBrowser((Queue) destination);
        return browser.getEnumeration().hasMoreElements();
    }
}
