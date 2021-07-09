package networking;

import org.apache.activemq.ActiveMQConnectionFactory;
import utils.ClientDataSingleton;

import javax.jms.*;

public class Producer {

    private Session session;
    private boolean producerStarted;

    public Producer() {
        producerStarted = false;
    }

    public void startProducer() throws JMSException {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnectionFactory.DEFAULT_BROKER_URL);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        producerStarted = true;
    }

    public void produceMessage(String destinationQueueID, String message) throws JMSException, IllegalAccessException {
        if (!producerStarted) throw new IllegalAccessException();

        Destination destination = session.createQueue(destinationQueueID);

        MessageProducer producer = session.createProducer(destination);
        TextMessage textMessage = session.createTextMessage(message);

        producer.send(textMessage);

        System.out.println("Messagem: '" + textMessage.getText() + ", Enviada para a Fila");
    }

}
