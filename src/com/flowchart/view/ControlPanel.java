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
        Color runColor = new Color(46, 125, 50);
        runButton.setBackground(runColor);
        runButton.setForeground(Color.WHITE);
        runButton.setFocusPainted(false);
        runButton.setOpaque(true);
        runButton.setContentAreaFilled(true);
        runButton.setBorderPainted(false);
        runButton.setFont(new Font("Arial", Font.BOLD, 12));
        runButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        runButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(runColor.darker(), 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        runButton.addChangeListener(e -> {
            if (runButton.isEnabled()) {
                if (runButton.getModel().isPressed()) {
                    runButton.setBackground(runColor.darker());
                } else if (runButton.getModel().isRollover()) {
                    runButton.setBackground(runColor.brighter());
                } else {
                    runButton.setBackground(runColor);
                }
            }
        });
        runButton.addActionListener(e -> startExecution(false));

        stepButton = new JButton("Passo-Passo");
        Color stepColor = new Color(21, 101, 192);
        stepButton.setBackground(stepColor);
        stepButton.setForeground(Color.WHITE);
        stepButton.setFocusPainted(false);
        stepButton.setOpaque(true);
        stepButton.setContentAreaFilled(true);
        stepButton.setBorderPainted(false);
        stepButton.setFont(new Font("Arial", Font.BOLD, 12));
        stepButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        stepButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(stepColor.darker(), 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        stepButton.addChangeListener(e -> {
            if (stepButton.getModel().isPressed()) {
                stepButton.setBackground(stepColor.darker());
            } else if (stepButton.getModel().isRollover()) {
                stepButton.setBackground(stepColor.brighter());
            } else {
                stepButton.setBackground(stepColor);
            }
        });
        stepButton.addActionListener(e -> {
            if (!executor.isRunning()) {
                startExecution(true);
            } else {
                executor.executeStep();
            }
        });

        stopButton = new JButton("Stop");
        Color stopColor = new Color(198, 40, 40);
        stopButton.setBackground(stopColor);
        stopButton.setForeground(Color.WHITE);
        stopButton.setFocusPainted(false);
        stopButton.setOpaque(true);
        stopButton.setContentAreaFilled(true);
        stopButton.setBorderPainted(false);
        stopButton.setFont(new Font("Arial", Font.BOLD, 12));
        stopButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        stopButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(stopColor.darker(), 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        stopButton.setEnabled(false);
        stopButton.addChangeListener(e -> {
            if (stopButton.isEnabled()) {
                if (stopButton.getModel().isPressed()) {
                    stopButton.setBackground(stopColor.darker());
                } else if (stopButton.getModel().isRollover()) {
                    stopButton.setBackground(stopColor.brighter());
                } else {
                    stopButton.setBackground(stopColor);
                }
            }
        });
        stopButton.addActionListener(e -> stopExecution());

        JButton clearOutputButton = new JButton("Pulisci Output");
        Color clearColor = new Color(97, 97, 97);
        clearOutputButton.setBackground(clearColor);
        clearOutputButton.setForeground(Color.WHITE);
        clearOutputButton.setFocusPainted(false);
        clearOutputButton.setOpaque(true);
        clearOutputButton.setContentAreaFilled(true);
        clearOutputButton.setBorderPainted(false);
        clearOutputButton.setFont(new Font("Arial", Font.BOLD, 11));
        clearOutputButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearOutputButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(clearColor.darker(), 2),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
        clearOutputButton.addChangeListener(e -> {
            if (clearOutputButton.getModel().isPressed()) {
                clearOutputButton.setBackground(clearColor.darker());
            } else if (clearOutputButton.getModel().isRollover()) {
                clearOutputButton.setBackground(clearColor.brighter());
            } else {
                clearOutputButton.setBackground(clearColor);
            }
        });
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
                // Filtra le variabili interne (che iniziano con __)
                if (!name.startsWith("__")) {
                    tableModel.addRow(new Object[]{name, value});
                }
            });
        });
    }

    public FlowchartExecutor getExecutor() {
        return executor;
    }
}
