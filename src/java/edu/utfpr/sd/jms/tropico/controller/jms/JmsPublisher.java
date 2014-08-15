package edu.utfpr.sd.jms.tropico.controller.jms;

import edu.utfpr.sd.jms.tropico.controller.rmi.PeerImpl;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicPublisher;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Classe que recebe uma string e publica no topico "topic/acoes"
 */
public class JmsPublisher {

    private TopicPublisher publisher;
    private TopicSession session;
    private TopicConnection connect;
    
    private JmsSubscriber subscribe;

    public JmsPublisher(PeerImpl peer) throws JMSException, NamingException {

        // Conexao com o topico
        Context jndiContext = new InitialContext();
        TopicConnectionFactory factory = (TopicConnectionFactory) jndiContext.lookup("ConnectionFactory");
        Topic topic = (Topic) jndiContext.lookup("topic/acoes");
        this.connect = factory.createTopicConnection();
        this.session = connect.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        this.publisher = session.createPublisher(topic);
        
        subscribe = new JmsSubscriber(peer);
    }

    /**
     * Publica a mensagem no topico "topic/acoes"
     *
     * @param message a ser publicada no topico.
     * @throws JMSException caso haja algum erro de conexao com o topico.
     */
    private void publish(String message) {
//        System.out.println("Send message: <!" + message + "!>");
        try {
            TextMessage textMsg = this.session.createTextMessage();
            textMsg.setText(message);
            this.publisher.publish(textMsg);
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Fecha a conexao com o topico.
     *
     * @throws JMSException caso haja algum erro de conexao com o topico.
     */
    public void close() throws JMSException {
        this.connect.close();
    }

    /**
     * Publicacao de uma VENDA no topico.
     * @param message operacao a ser divulgada.
     */
    public void publishSell(String message) {
        publish("venda/" + message);
    }
    
    /**
     * Publicacao de uma COMPRA no topico.
     * @param message operacao a ser divulgada.
     */
    public void publishBuy(String message) {
        publish("compra/" + message);
    }
    
}
