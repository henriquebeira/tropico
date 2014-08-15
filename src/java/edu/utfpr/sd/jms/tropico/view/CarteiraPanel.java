/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.utfpr.sd.jms.tropico.view;

import edu.utfpr.sd.jms.tropico.controller.CompanyManager;
import edu.utfpr.sd.jms.tropico.controller.Controller;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Calendar;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import edu.utfpr.sd.jms.tropico.model.beans.Empresa;
import edu.utfpr.sd.jms.tropico.model.beans.Operacao;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SpinnerDateModel;
import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import net.sourceforge.jdatepicker.impl.UtilCalendarModel;

/**
 * Classe da janela de operacoes.
 *
 * @author henrique
 */
public class CarteiraPanel extends JFrame {

    private final JButton addOperation;
    private final JSpinner quantity;
    private final JSpinner price;
    private final JComboBox<String> operationID;
    private final JComboBox<String> tipo;
    private final JTable monitored;
    private final String[] tableHeader = new String[]{"ID", "Nome", "Valor Unitario", "Quantidade em posse"};
    private final JDatePickerImpl datePicker;
    private final JSpinner hourSpinner;
    
    private final Controller control;

    /**
     * Construtora da classe.
     * @param control Controle de um Peer.
     */
    public CarteiraPanel(Controller control, String peerName) {
        super();
        this.setLayout(new BorderLayout());
        this.setSize(900, 600);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        this.control = control;

        monitored = new JTable(new DefaultTableModel(new Object[0][0], tableHeader));

        quantity = new JSpinner(new SpinnerNumberModel(0, 0, 1000000000, 1));
        price = new JSpinner(new SpinnerNumberModel(0, 0, 1000000, 0.1));
        operationID = new JComboBox<>();
        tipo = new JComboBox<String>(new String[]{"Comprar", "Vender"});
        addOperation = new JButton("Registrar!");
        datePicker = new JDatePickerImpl(new JDatePanelImpl(new UtilCalendarModel(Calendar.getInstance())));
        hourSpinner = new JSpinner(new SpinnerDateModel());
        hourSpinner.setEditor(new JSpinner.DateEditor(hourSpinner, "HH:mm:ss"));
        hourSpinner.setValue(new Date());

        addOperation.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent me) {
                register();
            }
        });

        this.add(new JScrollPane(monitored));

        JPanel axPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        axPanel.add(operationID);
        axPanel.add(tipo);
        axPanel.add(price);
        axPanel.add(quantity);
        axPanel.add(datePicker);
        axPanel.add(hourSpinner);
        axPanel.add(addOperation);

        this.add(axPanel, BorderLayout.SOUTH);
        this.setTitle(peerName);
        this.setVisible(true);
    }

    /**
     * Metodo captura uma operacao feita na janela.
     */
    void register() {
        System.out.println("Tentativa de registrar um novo operacao...");
        if(!control.addOperation(new Operacao(getOperationType(), operationID.getSelectedItem().toString(), getInputTime()).setPrecoUnitarioDesejado((int)((double)price.getValue() * 100)).setQuantidade((int)quantity.getValue()))){
            JOptionPane.showConfirmDialog(null, "Erro ao tentar criar operacoes. Possiveis erros incluem falta de acoes.");
        }
    }

    /**
     * Metodo que adiciona o estoque das acoes de uma empresa na tabela de monitoramento.
     *
     * @param estoque de uma empresa desejada.
     */
    public void addMonitoredCompany(CompanyManager estoque) {
        Empresa emp = estoque.getEmpresa();
        
        operationID.addItem(emp.getID());

        ((DefaultTableModel) monitored.getModel()).addRow(new Object[]{emp.getID(), emp.getName(), emp, estoque.getQuantityMutable()});
    }

    /**
     * Metodo retorna o tipo de uma operacao.
     *
     * @return true se for "Comprar", false caso contrario.
     */
    private boolean getOperationType() {
        switch (tipo.getSelectedItem().toString()) {
            case "Comprar":
                return true;
            case "Vender":
                return false;
        }

        throw new RuntimeException("OMG Wrong option");
    }
    
    private Calendar getInputTime(){
        Calendar baseTime = (Calendar) datePicker.getModel().getValue();
        Date time = (Date) hourSpinner.getValue();
        
        baseTime.set(Calendar.HOUR_OF_DAY, time.getHours());
        baseTime.set(Calendar.MINUTE, time.getMinutes());
        baseTime.set(Calendar.SECOND, time.getSeconds());
        
        return baseTime;
    }
}
