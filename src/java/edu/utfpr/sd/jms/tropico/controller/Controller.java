/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.utfpr.sd.jms.tropico.controller;

import edu.utfpr.sd.jms.tropico.controller.rmi.PeerImpl;
import edu.utfpr.sd.jms.tropico.model.beans.Empresa;
import edu.utfpr.sd.jms.tropico.model.beans.Operacao;
import edu.utfpr.sd.jms.tropico.model.beans.ReferencePath;
import edu.utfpr.sd.jms.tropico.view.CarteiraPanel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controle de um Peer.
 *
 * @author Henrique
 */
public class Controller {

    private PeerImpl peer;

    private ArrayList<Operacao> spamPool;
    private HashMap<String, CompanyManager> companyByID;

    private ArrayList<String> companyIDs;

    private CarteiraPanel view;

    /**
     * Construtora da classe. Inicializacao da interface grafica, e da Thread de
     * Spam.
     *
     * @param peer Peer inicializado.
     */
    public Controller(PeerImpl peer, String peerName) {
        this.peer = peer;
        view = new CarteiraPanel(this, peerName);

        spamPool = new ArrayList<>();
        companyByID = new HashMap<>();
        companyIDs = new ArrayList<>();

        new SPAM().start();
    }

    /**
     * Adicao de empresas de um Peer.
     *
     * @param emp empresa criada na inicializacao.
     */
    public void addCompany(Empresa emp) {
        companyByID.put(emp.getID(), new CompanyManager(this, emp));
        companyIDs.add(emp.getID());
        view.addMonitoredCompany(companyByID.get(emp.getID()));
    }

    /**
     * retorna a empresa.
     *
     * @param id id da empresa procurada.
     */
    public CompanyManager getCompany(String id) {
        return companyByID.get(id);
    }

    /**
     * Metodo que um gerenciador de empresas eh requisitado para realizar uma
     * operacao.
     *
     * @param operation Operacao vinda de outro Peer.
     * @return retorna a operacao realizada, ou nulo.
     */
    public Operacao receiveJMSMessage(Operacao operation) {
        return companyByID.get(operation.getCompanyID()).evaluateJMSOffer(operation);
    }

    /**
     * Metodo que um gerenciador de empresas eh requisitado para realizar uma
     * operacao.
     *
     * @param operation Operacao vinda de outro Peer.
     * @return retorna a operacao realizada, ou nulo.
     */
    public Boolean receiveRMIMessage(Operacao operation) {
        if (companyByID.get(operation.getCompanyID()).validateRMIOffer(operation)) {
            return true;
        }

        return false;
    }

    /**
     * Adicao na lista para promover operacoes, vinda da GUI.
     *
     * @param op novo registro de operacao.
     * @return
     */
    public boolean addOperation(Operacao op) {
        op.setReference(getReference());

        if (!op.isCompra()) {
            if (companyByID.get(op.getCompanyID()).getQuantity() < op.getQuantidade()) {
                return false;
            }
        }

        companyByID.get(op.getCompanyID()).overrideOperation(op);
        spamPool.add(op);

        return true;
    }

    /**
     * Repassa a lista de operacoes para o controlador do Peer.
     */
    void spammer() {
        System.out.println("Spam!!!!");
        peer.spam(spamPool);
    }

    /**
     * Retorno da referencia de um Peer, vindo do PeerImpl.
     *
     * @return
     */
    private ReferencePath getReference() {
        return peer.getReference();
    }

    /**
     * Inicializa a quantidade de acoes de cada empresa.
     */
    public void startSimulation() {
        int howMany = (int) Math.max(companyByID.size() * 0.25, 1);

        for (int k = 0; k < howMany; k++) {
            int whichOne = Math.round((float) Math.random() * (companyByID.size() - 1));

            companyByID.get(companyIDs.get(whichOne)).incQuantity((int) ((Math.random() * 11) + 1));
        }
    }

    /**
    * Renovar a GUI do Peer.
    */
    public void repaintFrame() {
        view.revalidate();
        view.repaint();
    }

    class SPAM extends Thread {

        /**
         * Anuncio das operacoes no intervalo de tempo relativo a 2 segundos ate
         * 12 segundos.
         */
        @Override
        public void run() {
            while (true) {
                spammer();
                try {
                    sleep((long) ((Math.random() * 10000) + 2000));
                } catch (InterruptedException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

    }
}
