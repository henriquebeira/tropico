/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.utfpr.sd.jms.tropico.model.beans;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * Bean com a referencia completa de um Peer.
 *
 * @author Henrique
 */
public class ReferencePath implements Serializable {

    private String IP;
    private Integer port;
    private String name;

    public String getIP() {
        return IP;
    }

    public ReferencePath setIP(String IP) {
        this.IP = IP;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public ReferencePath setPort(Integer port) {
        this.port = port;
        return this;
    }

    public String getName() {
        return name;
    }

    public ReferencePath setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Retorna a referencia completa de um Peer.
     *
     * @return
     */
    @Override
    public String toString() {
        return "<" + IP + ";" + port + ";" + name + ">";
    }

    /**
     * Metodo que compara um objeto com a referencia para ver se sao iguais.
     * @param o o objeto a ser checado
     * @return verdadeiro se o objeto representar a mesma informacao que a referencia. Falso do contrario.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof ReferencePath) {
            if (((ReferencePath) o).IP.equals(IP)) {
                if (((ReferencePath) o).name.equals(name)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * Transformar de string, da mensagem via JMS, para os seus respectivos
     * tipos de dados.
     *
     * @param toParse mensagem vinda como String.
     * @return Objeto do tipo Referencia.
     */
    public static ReferencePath parseFromString(String toParse) {
//        System.out.println("Parse Reference: " + toParse);
        if (toParse.charAt(0) == '<' && toParse.charAt(toParse.length() - 1) == '>') {
            String auxParser = toParse.substring(1, toParse.length() - 1);

            StringTokenizer token = new StringTokenizer(auxParser, ";");

            if (token.countTokens() == 3) {
                String IP = token.nextToken();
                Integer port = Integer.parseInt(token.nextToken());
                String name = token.nextToken();

                return new ReferencePath().setIP(IP).setPort(port).setName(name);
            }
        }

        return null;
    }
}
