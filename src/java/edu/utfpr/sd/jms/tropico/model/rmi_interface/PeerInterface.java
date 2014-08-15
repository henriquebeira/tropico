/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.utfpr.sd.jms.tropico.model.rmi_interface;

import edu.utfpr.sd.jms.tropico.model.beans.Operacao;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface que determina a todos os Peers possuirem um metodo de negociacao.
 * @author henrique
 */
public interface PeerInterface extends Remote{
    public Boolean closeDeal(Operacao operacao) throws RemoteException;
}
