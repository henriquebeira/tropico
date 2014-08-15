/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.utfpr.sd.jms.tropico.controller;

import java.util.Calendar;
import edu.utfpr.sd.jms.tropico.model.beans.Empresa;
import edu.utfpr.sd.jms.tropico.model.beans.Operacao;
import edu.utfpr.sd.jms.tropico.util.MutableValue;

/**
 * Classe que encapsula o Bean Empresa para disponibilizar mecanismos de
 * controle para o servidor.
 *
 * @author Henriques
 */
public class CompanyManager {

    private Controller controller;

    //Bean Empresa encapsulado.
    private final Empresa empresa;

    // Operacoes de disponiveis.
    private Operacao compra;
    private Operacao venda;
    private MutableValue<Integer> quantity;

    /**
     * Construtora da classe. Recebe o Bean da empresa que ira encapsular.
     *
     * @param controller controladora da empresa.
     * @param empresa Bean que sera encapsulado.
     */
    public CompanyManager(Controller controller, Empresa empresa) {
        this.controller = controller;
        this.empresa = empresa;

        compra = new Operacao(true, empresa.getID(), Calendar.getInstance());
        venda = new Operacao(false, empresa.getID(), Calendar.getInstance());
        quantity = new MutableValue<>(0);
    }

    /**
     * Adiciona a quantidade de acoes.
     *
     * @param quantity quantidade de acoes.
     */
    public void setQuantity(Integer quantity) {
        this.quantity.setValue(Math.abs(quantity));
    }
    
    /**
     * Incrementa a quantidade de acoes.
     *
     * @param quantity quantidade de acoes.
     */
    public void incQuantity(Integer quantity) {
        this.quantity.setValue(Math.abs(this.quantity.getValue() + quantity));
    }

    /**
     * Incrementa uma quantidade j� existente.
     *
     * @param increment incremento.
     */
    public void incQuantity(int increment) {
        setQuantity(quantity.getValue() + increment);
    }

    public Integer getQuantity() {
        return quantity.getValue();
    }

    public MutableValue<Integer> getQuantityMutable() {
        return quantity;
    }

    /**
     * Permite o acesso a uma empresa.
     *
     * @return Empresa encapsulada
     */
    public Empresa getEmpresa() {
        return empresa;
    }

    /**
     * Sobreencrever o tipo de operacao.
     *
     * @param op operacao vinda da interface grafica.
     */
    public void overrideOperation(Operacao op) {
        if (op.isCompra()) {
            compra = op;
        } else {
            venda = op;
        }
    }

    /**
     * Valida se a operacao vinda por JMS de venda, ou compra, pode ser realizada.
     *
     * @param op Operacao vinda de outro Peer.
     * @return Operacao realizada, ou nulo.
     */
    public Operacao evaluateJMSOffer(Operacao op) {
        if (op.isCompra()) {
            if (venda.getQuantidade() > 0) {
                if (venda.getPrecoUnitarioDesejado() <= op.getPrecoUnitarioDesejado()) {
                    Integer price, quant;

                    quant = Math.min(venda.getQuantidade(), op.getQuantidade());
                    price = (venda.getPrecoUnitarioDesejado() + op.getPrecoUnitarioDesejado()) / 2;

                    op.setPrecoUnitarioDesejado(price);
                    op.setQuantidade(quant);
//                    op.setReference(venda.getReference());

                    return op;
                }
            }
        } else {
            if (compra.getQuantidade() > 0) {
                if (compra.getPrecoUnitarioDesejado() >= op.getPrecoUnitarioDesejado()) {
                    Integer price, quant;

                    quant = Math.min(compra.getQuantidade(), op.getQuantidade());
                    price = (compra.getPrecoUnitarioDesejado() + op.getPrecoUnitarioDesejado()) / 2;

                    op.setPrecoUnitarioDesejado(price);
                    op.setQuantidade(quant);
//                    op.setReference(compra.getReference());

                    return op;
                }
            }
        }

        return null;
    }

    /**
    * Valida se a operacao vnida por JMS de venda, ou compra, pode ser realizada.
    * @param op Operacao vinda de outro Peer.
    * @return Operacao realizada, ou nulo.
    */
    public Boolean validateRMIOffer(Operacao op) {
        if(op.isCompra()){
            if(op.getQuantidade() <= compra.getQuantidade()){
                compra.setQuantidade(compra.getQuantidade() - op.getQuantidade());
                incQuantity(op.getQuantidade());
                
                return true;
            }
        }else{
            if(op.getQuantidade() <= venda.getQuantidade()){
                venda.setQuantidade(venda.getQuantidade() - op.getQuantidade());
                incQuantity(op.getQuantidade() * (-1));
                
                return true;
            }
        }
        
        return false;
    }
        
}
