/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.utfpr.sd.jms.tropico.model.beans;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

/**
 * Bean para uma Operacao.
 * Beans sao classes que apenas possuem variaveis, construtora e metodos get/set.
 * @author henrique
 */
public class Operacao implements Serializable {
    
    private boolean isCompra;
    private String companyID;
    private Integer quantidade;
    private Integer precoUnitarioDesejado;
    private Calendar expireDate;
    
    private ReferencePath reference;

    public Operacao(boolean isCompra, String company, Calendar expireDate) {
        this.isCompra = isCompra;
        this.companyID = company;
        this.expireDate = expireDate;
        
        quantidade = 0;
        precoUnitarioDesejado = 0;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public Operacao setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
        
        return this;
    }

    public Integer getPrecoUnitarioDesejado() {
        return precoUnitarioDesejado;
    }

    public Operacao setPrecoUnitarioDesejado(Integer precoUnitarioDesejado) {
        this.precoUnitarioDesejado = precoUnitarioDesejado;
        
        return this;
    }

    public boolean isCompra() {
        return isCompra;
    }

    public String getCompanyID() {
        return companyID;
    }

    public Calendar getExpireDate() {
        return expireDate;
    }

    public ReferencePath getReference() {
        return reference;
    }
    
    public Operacao setIsCompra(Boolean bool){
        isCompra = bool;
        
        return this;
    }

    /**
     * Indica a referencia de uma operacao.
     * @param reference
     * @return 
     */
    public Operacao setReference(ReferencePath reference) {
        this.reference = reference;
        return this;
    }

    /**
     * Montagem da mensagem que sera enviada ao topico.
     * @return string com a mensagem de operacao.
     */
    @Override
    public String toString() {
        return "<" + reference.toString() + ":" + companyID + ":" + quantidade + ":" + precoUnitarioDesejado + ":" + isCompra + ":" + expireDate.getTimeInMillis() + ">";
    }
    
    /**
     * Transformar de string, da mensagem via JMS, para os seus respectivos tipos de dados.
     * @param toParse mensagem vinda como String.
     * @return Objeto do tipo Operacao.
     */
    public static Operacao parseFromString(String toParse) {
        if (toParse.charAt(0) == '<' && toParse.charAt(toParse.length() - 1) == '>') {
            String auxParser = toParse.substring(1, toParse.length() - 1);

            StringTokenizer token = new StringTokenizer(auxParser, ":");

            if (token.countTokens() >= 6) {
                ReferencePath reference = ReferencePath.parseFromString(token.nextToken());
                String ID = token.nextToken();
                Integer quant = Integer.parseInt(token.nextToken());
                Integer preco = Integer.parseInt(token.nextToken());
                Boolean isCompr = Boolean.parseBoolean(token.nextToken());
                
                Calendar cal = new GregorianCalendar();
                cal.setTimeInMillis(Long.parseLong(token.nextToken()));
                
                return new Operacao(isCompr, ID, cal).setPrecoUnitarioDesejado(preco).setQuantidade(quant).setReference(reference);
            }
        }

        return null;
    }
}
