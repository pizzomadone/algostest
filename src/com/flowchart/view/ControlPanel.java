package com.flowchart.view;

import com.flowchart.executor.FlowchartExecutor;
import com.flowchart.model.Block;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Pannello di controllo per l'esecuzione del diagramma
 */
public class ControlPanel extends JPanel {
    private FlowchartExecutor executor;
    private FlowchartCanvas canvas;

    private JButton runButton;
    private JButton stepButton;
    private JButton stopButton;
    private JTextArea outputArea;
    private JTable variablesTable;
    private DefaultTableModel tableModel;

    public ControlPanel(FlowchartExecutor executor, FlowchartCanvas canvas) {
        this.executor = executor;
        this.canvas = canvas;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Controlli Esecuzione"));

        initializeComponents();
        setupExecutorListener();
    }

    private void initializeComponents() {
        // Pannello pulsanti
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        runButton = new JButton("Esegui Tutto");
        runButton.setBackground(new Color(76, 175, 80));
        runButton.setForeground(Color.WHITE);
        runButton.setFocusPainted(false);
        runButton.addActionListener(e -> startExecution(false));

        stepButton = new JButton("Passo-Passo");
        stepButton.setBackground(new Color(33, 150, 243));
        stepButton.setForeground(Color.WHITE);
        stepButton.setFocusPainted(false);
        stepButton.addActionListener(e -> {
            if (!executor.isRunning()) {
                startExecution(true);
            } else {
                executor.executeStep();
            }
        });

        stopButton = new JButton("Stop");
        stopButton.setBackground(new Color(244, 67, 54));
        stopButton.setForeground(Color.WHITE);
        stopButton.setFocusPainted(false);
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> stopExecution());

        JButton clearOutputButton = new JButton("Pulisci Output");
        clearOutputButton.addActionListener(e -> outputArea.setText(""));

        buttonPanel.add(runButton);
        buttonPanel.add(stepButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(clearOutputButton);

        add(buttonPanel, BorderLayout.NORTH);

        // Pannello centrale con output e variabili
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        // Area output
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder("Output Esecuzione"));
        centerPanel.add(outputScroll);

        // Tabella variabili
        tableModel = new DefaultTableModel(new String[]{"Variabile", "Valore"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        variablesTable = new JTable(tableModel);
        variablesTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane tableScroll = new JScrollPane(variablesTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Variabili"));
        centerPanel.add(tableScroll);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void setupExecutorListener() {
        executor.setListener(new FlowchartExecutor.ExecutionListener() {
            @Override
            public void onBlockExecuted(Block block, String output) {
                appendOutput(block.getType().getDisplayName() + ": " + output);
                highlightBlock(block);
            }

            @Override
            public void onExecutionComplete() {
                appendOutput("\n=== ESECUZIONE COMPLETATA ===\n");
                stopExecution();
                clearHighlight();
            }

            @Override
            public void onExecutionError(String error) {
                appendOutput("ERRORE: " + error);
                stopExecution();
                clearHighlight();
            }

            @Override
            public void onVariableChanged(String name, Object value) {
                updateVariablesTable();
            }
        });
    }

    private void startExecution(boolean stepByStep) {
        runButton.setEnabled(false);
        stopButton.setEnabled(true);
        outputArea.setText("");
        tableModel.setRowCount(0);

        appendOutput("=== INIZIO ESECUZIONE ===\n");

        // Avvia l'esecuzione in un thread separato per non bloccare l'UI
        new Thread(() -> executor.startExecution(stepByStep)).start();
    }

    private void stopExecution() {
        executor.stopExecution();
        runButton.setEnabled(true);
        stopButton.setEnabled(false);
        clearHighlight();
    }

    private void appendOutput(String text) {
        SwingUtilities.invokeLater(() -> {
            outputArea.append(text + "\n");
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        });
    }

    private void highlightBlock(Block block) {
        SwingUtilities.invokeLater(() -> {
            clearHighlight();
            block.setHighlighted(true);
            canvas.repaint();
        });
    }

    private void clearHighlight() {
        SwingUtilities.invokeLater(() -> {
            for (Block block : executor.getModel().getBlocks()) {
                block.setHighlighted(false);
            }
            canvas.repaint();
        });
    }

    private void updateVariablesTable() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            executor.getModel().getVariables().forEach((name, value) -> {
                tableModel.addRow(new Object[]{name, value});
            });
        });
    }

    public FlowchartExecutor getExecutor() {
        return executor;
    }
}
