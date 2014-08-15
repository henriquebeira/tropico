package edu.utfpr.sd.jms.tropico.controller.jms;

import edu.utfpr.sd.jms.tropico.controller.rmi.PeerImpl;
import edu.utfpr.sd.jms.tropico.model.beans.Operacao;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.jms.Session;
import javax.jms.MessageListener;

/**
 * Classe que escuta as mensagens publicadas no topico JMS. 
 */
public class JmsSubscriber implements MessageListener {

    private TopicConnection connect;
    private PeerImpl peer;

    public JmsSubscriber(PeerImpl peer) throws JMSException, NamingException {
        
        this.peer = peer;
        
        // Conexao ao topico
        Context jndiContext = new InitialContext();
        TopicConnectionFactory factory = (TopicConnectionFactory) jndiContext.lookup("ConnectionFactory");
        Topic topic = (Topic) jndiContext.lookup("topic/acoes");
        this.connect = factory.createTopicConnection();
        TopicSession session = connect.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        TopicSubscriber subscriber = session.createSubscriber(topic);
        subscriber.setMessageListener(this);
        
        connect.start();
    }

    /**
     * Ao receber uma mensagem do topico, verifica-se a operacao.
     * @param message mensagem que contem uma operacao.
     */
    @Override
    public void onMessage(Message message) {
        try {
            TextMessage textMsg = (TextMessage) message;
            String text = textMsg.getText();
            
            System.out.println(text);
            
            String mode[] = text.split("/");
            
            if(mode.length  < 2){
                System.out.println("Erro ao tentar interpretar mensagem.");
                return;
            }
            
            if(mode[0].equals("venda")){
                Operacao operation = Operacao.parseFromString(text.substring(6));
                
                showMessage(operation, "venda");
                
                if(operation != null){
                    if(!operation.isCompra()){
                        peer.checkOperation(operation);
                    }
                }
                
                return;
            }
            if(mode[0].equals("compra")){
                Operacao operation = Operacao.parseFromString(text.substring(7));
                
                showMessage(operation, "compra");
                
                if(operation != null){
                    if(operation.isCompra()){
                        peer.checkOperation(operation);
                    }
                }
                
                return;
            }
            
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
    }
    
    private void showMessage(Operacao operacao, String tipo){
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        if(tipo.equals("venda")){
            System.out.println("Operação de venda recebida. \n"
                    + "Identificador da Empresa: " + operacao.getCompanyID()
                    + "Preco unitario: " + operacao.getPrecoUnitarioDesejado()/100 + "\n"
                    + "Quantidade ofertada: " + operacao.getQuantidade() + "\n"
                    + "Falar com: IP- " + operacao.getReference().getIP() + ":" + operacao.getReference().getPort() + "\n"
                    + "Procurar por: " + operacao.getReference().getName() + "\n"
                    + "Expira em: " + format.format(operacao.getExpireDate().getTime()));
        }
        
        if(tipo.equals("compra")){
            System.out.println("Operação de compra recebida. \n "
                    + "Identificador da Empresa: " + operacao.getCompanyID() + "\n"
                    + "Preco unitario: " + operacao.getPrecoUnitarioDesejado()/100 + "\n"
                    + "Quantidade desejada: " + operacao.getQuantidade() + "\n"
                    + "Falar com: IP- " + operacao.getReference().getIP() + ":" + operacao.getReference().getPort() + "\n"
                    + "Procurar por: " + operacao.getReference().getName() + "\n"
                    + "Expira em: " + format.format(operacao.getExpireDate().getTime()));
        }
    }
}
