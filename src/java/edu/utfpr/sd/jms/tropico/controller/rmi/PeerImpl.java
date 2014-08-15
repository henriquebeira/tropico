/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.utfpr.sd.jms.tropico.controller.rmi;

import edu.utfpr.sd.jms.tropico.controller.CompanyManager;
import edu.utfpr.sd.jms.tropico.controller.Controller;
import edu.utfpr.sd.jms.tropico.controller.jms.JmsPublisher;
import edu.utfpr.sd.jms.tropico.model.beans.Empresa;
import edu.utfpr.sd.jms.tropico.model.beans.Operacao;
import edu.utfpr.sd.jms.tropico.model.beans.ReferencePath;
import edu.utfpr.sd.jms.tropico.model.rmi_interface.PeerInterface;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.swing.JOptionPane;

/**
 * Classe principal de um Peer.
 *
 * @author henriques
 */
public class PeerImpl extends UnicastRemoteObject implements PeerInterface {

    private Controller controller;
    private JmsPublisher publisher;

    private ReferencePath ref;

    /**
     * Construtora da classe. Registro de nome do processo no servico de nomes.
     * Criacao do controle do Peer, e do seu publicador.
     *
     * @throws RemoteException
     */
    public PeerImpl() throws RemoteException {
        try {
            String answer = JOptionPane.showInputDialog("Qual o nome unico deste processo?");
            Registry reg;

            try {
                reg = LocateRegistry.createRegistry(1099);
            } catch (ExportException export) {
            }

            reg = LocateRegistry.getRegistry();
            reg.bind(answer, this);
            ref = new ReferencePath().setName(answer).setIP(InetAddress.getLocalHost().getHostAddress()).setPort(1099);

            controller = new Controller(this, answer);
            publisher = new JmsPublisher(this);

        } catch (JMSException ex) {
            Logger.getLogger(PeerImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NamingException ex) {
            Logger.getLogger(PeerImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(PeerImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AlreadyBoundException ex) {
            Logger.getLogger(PeerImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AccessException ex) {
            Logger.getLogger(PeerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Retorna a referencia completa de um Peer.
     *
     * @return a referencia de um Peer.
     */
    public ReferencePath getReference() {
        return ref;
    }

    /**
     * Metodo que recebe uma oferta de outro Peer.
     *
     * @param operacao Informacoes de uma operacao.
     * @return
     * @throws RemoteException
     */
    @Override
    public Boolean closeDeal(Operacao operacao) throws RemoteException {
        Boolean answer = controller.receiveRMIMessage(operacao);

        System.out.println("Received operation/request: " + operacao + " | ansRMI: " + answer);

        if (answer) {
            notifyUser(operacao, (operacao.isCompra()));

            return true;
        }

        return false;
    }

    /**
     * Adicao de empresa no controle de um Peer.
     *
     * @param emp dados de uma empresa.
     */
    private void addEmpresa(Empresa emp) {
        controller.addCompany(emp);
    }

    /**
     * Main da aplicacao Tropico.
     *
     * @param args
     */
    public static void main(String... args) {
        try {
            PeerImpl peer = new PeerImpl();

            peer.addEmpresa(new Empresa("PB568A").setName("PanBas").setValue(322));
            peer.addEmpresa(new Empresa("EA851A").setName("EACon").setValue(422));
            peer.addEmpresa(new Empresa("TB854A").setName("Tabuu").setValue(123));
            peer.addEmpresa(new Empresa("YU852A").setName("YoUi").setValue(273));
            peer.addEmpresa(new Empresa("RT652A").setName("RaTimbum").setValue(89));
            peer.addEmpresa(new Empresa("XM965A").setName("XMLinha").setValue(186));

            peer.startSimulation();

        } catch (RemoteException ex) {
            Logger.getLogger(PeerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Notificacao de operacao realizada.
     *
     * @param answer resposta com a operacao realizada.
     */
    private void notifyUser(Operacao answer, Boolean isCompra) {
        controller.repaintFrame();
        if (answer != null) {
            String info = "Operacao de ";
            if (isCompra) {
                info += "compra";
            } else {
                info += "venda";
            }

            info += " realizada com sucesso. " + answer.getQuantidade() + " acoes da empresa " + answer.getCompanyID()
                    + " foram negociadas a um valor unitario de R$ " + (answer.getPrecoUnitarioDesejado()) / 100.0 + ", totalizando "
                    + "R$ " + ((answer.getPrecoUnitarioDesejado() * answer.getQuantidade()) / 100.0);
            JOptionPane.showMessageDialog(null, info, "Operacao concluida", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Metodo que checa se uma operacao postada em um quadro de mensagens eh
     * util.
     *
     * @param operation Operacao vinda do topico.
     */
    public void checkOperation(Operacao operation) {
        if (operation.getReference().equals(ref)) {
            return;
        }

        Operacao answer = controller.receiveJMSMessage(operation);
        if (answer != null) {
            try {
                Registry reg = LocateRegistry.getRegistry(answer.getReference().getIP(), answer.getReference().getPort());
                PeerInterface peer = (PeerInterface) reg.lookup(answer.getReference().getName());
                Boolean ans = peer.closeDeal(answer);

                System.out.println("Received operation/request: " + operation + " | ansJMS: " + answer);

                if (ans) {
                    CompanyManager company = controller.getCompany(answer.getCompanyID());
                    if (answer.isCompra()) {
                        company.setQuantity(company.getQuantity() - answer.getQuantidade());
                    } else {
                        company.setQuantity(company.getQuantity() + answer.getQuantidade());
                    }

                    notifyUser(answer, !answer.isCompra());
                }

                return;

            } catch (RemoteException ex) {
                Logger.getLogger(PeerImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NotBoundException ex) {
                Logger.getLogger(PeerImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("Erro ao tentar contatar peer.");
        }
    }

    /**
     * Anunciar compra ou venda.
     *
     * @param spamPool lista com operacoes.
     */
    public void spam(ArrayList<Operacao> spamPool) {
        for (Operacao op : spamPool) {
            if (op.getQuantidade() > 0) {
                if (op.isCompra()) {
                    publisher.publishBuy(op.toString());
                } else {
                    publisher.publishSell(op.toString());
                }
            }
        }
    }

    /**
     * Controle inicializa a quantidade de acoes de uma empresa.
     */
    private void startSimulation() {
        controller.startSimulation();
    }
}
