package com.flowchart.view;

import com.flowchart.executor.FlowchartExecutor;
import com.flowchart.model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Pannello di controllo semplificato per il nuovo sistema
 */
public class ControlPanelSimple extends JPanel {
    private FlowchartExecutor executor;
    private FlowchartCanvasNew canvas;
    private FlowchartTree tree;

    private JButton runButton;
    private JButton stepButton;
    private JButton stopButton;
    private JTextArea outputArea;
    private JTable variablesTable;
    private DefaultTableModel tableModel;

    public ControlPanelSimple(FlowchartExecutor executor, FlowchartCanvasNew canvas, FlowchartTree tree) {
        this.executor = executor;
        this.canvas = canvas;
        this.tree = tree;

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Controlli Esecuzione"));

        initializeComponents();
        setupExecutorListener();
    }

    private void initializeComponents() {
        // Pannello pulsanti
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        runButton = createStyledButton("Esegui Tutto", new Color(46, 125, 50));
        runButton.addActionListener(e -> startExecution(false));

        stepButton = createStyledButton("Passo-Passo", new Color(21, 101, 192));
        stepButton.addActionListener(e -> {
            if (!executor.isRunning()) {
                startExecution(true);
            } else {
                executor.executeStep();
            }
        });

        stopButton = createStyledButton("Stop", new Color(198, 40, 40));
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> stopExecution());

        buttonPanel.add(runButton);
        buttonPanel.add(stepButton);
        buttonPanel.add(stopButton);

        // Area di output
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder("Output"));
        outputScroll.setPreferredSize(new Dimension(280, 200));

        // Tabella variabili
        tableModel = new DefaultTableModel(new String[]{"Variabile", "Valore"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        variablesTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(variablesTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Variabili"));
        tableScroll.setPreferredSize(new Dimension(280, 200));

        // Layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(buttonPanel, BorderLayout.NORTH);
        topPanel.add(outputScroll, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(tableScroll, BorderLayout.CENTER);
    }

    private JButton createStyledButton(String text, Color baseColor) {
        JButton button = new JButton(text);
        button.setBackground(baseColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(baseColor.darker(), 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return button;
    }

    private void setupExecutorListener() {
        executor.setListener(new FlowchartExecutor.ExecutionListener() {
            @Override
            public void onBlockExecuted(Block block, String output) {
                if (output != null && !output.isEmpty()) {
                    outputArea.append(output + "\n");
                }
                canvas.repaint();
            }

            @Override
            public void onExecutionComplete() {
                runButton.setEnabled(true);
                stepButton.setEnabled(true);
                stopButton.setEnabled(false);
                outputArea.append("\n=== Esecuzione completata ===\n");
                canvas.repaint();
            }

            @Override
            public void onExecutionError(String error) {
                runButton.setEnabled(true);
                stepButton.setEnabled(true);
                stopButton.setEnabled(false);
                outputArea.append("\nERRORE: " + error + "\n");
                JOptionPane.showMessageDialog(
                    ControlPanelSimple.this,
                    error,
                    "Errore di Esecuzione",
                    JOptionPane.ERROR_MESSAGE
                );
            }

            @Override
            public void onVariableChanged(String name, Object value) {
                updateVariablesTable();
            }
        });
    }

    private void startExecution(boolean stepByStep) {
        // Sincronizza il model con il tree
        syncModelWithTree();

        outputArea.setText("");
        tableModel.setRowCount(0);

        runButton.setEnabled(false);
        stepButton.setEnabled(stepByStep);
        stopButton.setEnabled(true);

        executor.startExecution(stepByStep);
    }

    private void stopExecution() {
        executor.stopExecution();
        runButton.setEnabled(true);
        stepButton.setEnabled(true);
        stopButton.setEnabled(false);
        canvas.repaint();
    }

    private void syncModelWithTree() {
        // Pulisci il model e aggiungi i blocchi dal tree
        FlowchartModel model = executor.getModel();
        model.clear();

        for (Block block : tree.getAllBlocks()) {
            model.addBlock(block);
        }
    }

    private void updateVariablesTable() {
        tableModel.setRowCount(0);
        executor.getModel().getVariables().forEach((name, value) -> {
            tableModel.addRow(new Object[]{name, value});
        });
    }
}
