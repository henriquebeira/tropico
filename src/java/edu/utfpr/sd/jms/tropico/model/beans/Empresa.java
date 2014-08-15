/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.utfpr.sd.jms.tropico.model.beans;

import java.io.Serializable;

/**
 * Bean para uma Empresa.
 * Beans sao classes que apenas possuem variaveis, construtora e metodos get/set.
 * @author henrique
 */
public class Empresa implements Serializable {
    
    private String name;
    private final String ID; //Chave para referencia
    private Integer value; //Valor em centavos (100 -> 1 R$)

    public Empresa(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public Empresa setName(String name) {
        this.name = name;
        
        return this;
    }

    public String getID() {
        return ID;
    }

    public Integer getValue() {
        return value;
    }

    public Empresa setValue(Integer value) {
        this.value = value;
        
        return this;
    }
    
    @Override
    public String toString() {
        return "R$ " + ((value != null ? value : 0) / 100.0);
    }
    
}
