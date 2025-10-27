package com.flowchart.executor;

import com.flowchart.model.*;
import javax.swing.*;
import java.util.*;

/**
 * Esecutore del diagramma a blocchi con supporto per esecuzione passo-passo
 */
public class FlowchartExecutor {
    private FlowchartModel model;
    private Block currentBlock;
    private boolean running;
    private boolean stepByStep;
    private ExecutionListener listener;
    private Stack<Block> callStack;

    public interface ExecutionListener {
        void onBlockExecuted(Block block, String output);
        void onExecutionComplete();
        void onExecutionError(String error);
        void onVariableChanged(String name, Object value);
    }

    public FlowchartExecutor(FlowchartModel model) {
        this.model = model;
        this.callStack = new Stack<>();
        this.running = false;
        this.stepByStep = false;
    }

    public void setListener(ExecutionListener listener) {
        this.listener = listener;
    }

    public void startExecution(boolean stepByStep) {
        this.stepByStep = stepByStep;
        this.running = true;
        model.clearVariables();
        currentBlock = model.getStartBlock();

        if (currentBlock == null) {
            notifyError("Nessun blocco di INIZIO trovato!");
            return;
        }

        if (!stepByStep) {
            executeAll();
        } else {
            executeStep();
        }
    }

    public void executeStep() {
        if (!running || currentBlock == null) {
            return;
        }

        try {
            String output = executeBlock(currentBlock);
            notifyBlockExecuted(currentBlock, output);

            // Trova il prossimo blocco
            Block nextBlock = getNextBlock(currentBlock, output);

            if (nextBlock == null || currentBlock.getType() == BlockType.END) {
                stopExecution();
                notifyComplete();
            } else {
                currentBlock = nextBlock;
            }
        } catch (Exception e) {
            notifyError("Errore durante l'esecuzione: " + e.getMessage());
            stopExecution();
        }
    }

    private void executeAll() {
        while (running && currentBlock != null && currentBlock.getType() != BlockType.END) {
            try {
                String output = executeBlock(currentBlock);
                notifyBlockExecuted(currentBlock, output);

                Block nextBlock = getNextBlock(currentBlock, output);
                if (nextBlock == null) {
                    break;
                }
                currentBlock = nextBlock;

                // Piccola pausa per visualizzare l'esecuzione
                Thread.sleep(500);
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                notifyError("Errore durante l'esecuzione: " + e.getMessage());
                break;
            }
        }
        stopExecution();
        notifyComplete();
    }

    private String executeBlock(Block block) {
        switch (block.getType()) {
            case START:
                return "Inizio esecuzione";

            case END:
                return "Fine esecuzione";

            case PROCESS:
                return executeProcess(block);

            case INPUT:
                return executeInput(block);

            case OUTPUT:
                return executeOutput(block);

            case DECISION:
                return executeDecision(block);

            default:
                return "";
        }
    }

    private String executeProcess(Block block) {
        String text = block.getText().trim();

        // Supporta assegnazioni semplici: variabile = espressione
        if (text.contains("=")) {
            String[] parts = text.split("=", 2);
            String varName = parts[0].trim();
            String expression = parts[1].trim();

            try {
                Object value = evaluateExpression(expression);
                model.setVariable(varName, value);
                notifyVariableChanged(varName, value);
                return varName + " = " + value;
            } catch (Exception e) {
                return "Errore: " + e.getMessage();
            }
        }

        return "Eseguito: " + text;
    }

    private String executeInput(Block block) {
        String text = block.getText().trim();
        String varName = text.isEmpty() ? "input" : text;

        String input = JOptionPane.showInputDialog(null,
            "Inserisci valore per " + varName + ":",
            "Input",
            JOptionPane.QUESTION_MESSAGE);

        if (input != null) {
            // Prova a convertire in numero
            try {
                if (input.contains(".")) {
                    model.setVariable(varName, Double.parseDouble(input));
                } else {
                    model.setVariable(varName, Integer.parseInt(input));
                }
            } catch (NumberFormatException e) {
                model.setVariable(varName, input);
            }
            notifyVariableChanged(varName, model.getVariable(varName));
            return varName + " = " + model.getVariable(varName);
        }

        return "Input annullato";
    }

    private String executeOutput(Block block) {
        String text = block.getText().trim();

        try {
            Object value = evaluateExpression(text);
            JOptionPane.showMessageDialog(null,
                value.toString(),
                "Output",
                JOptionPane.INFORMATION_MESSAGE);
            return "Output: " + value;
        } catch (Exception e) {
            return "Errore output: " + e.getMessage();
        }
    }

    private String executeDecision(Block block) {
        String condition = block.getText().trim();

        try {
            boolean result = evaluateCondition(condition);
            return result ? "VERO" : "FALSO";
        } catch (Exception e) {
            return "Errore condizione: " + e.getMessage();
        }
    }

    private Object evaluateExpression(String expression) throws Exception {
        expression = expression.trim();

        // Se è una variabile, restituisci il suo valore
        if (model.getVariables().containsKey(expression)) {
            return model.getVariables().get(expression);
        }

        // Se è un numero
        try {
            if (expression.contains(".")) {
                return Double.parseDouble(expression);
            } else {
                return Integer.parseInt(expression);
            }
        } catch (NumberFormatException e) {
            // Non è un numero semplice, prova a valutare come espressione
        }

        // Valutazione espressioni semplici
        return evaluateSimpleExpression(expression);
    }

    private Object evaluateSimpleExpression(String expression) throws Exception {
        // Sostituisci le variabili con i loro valori
        for (Map.Entry<String, Object> entry : model.getVariables().entrySet()) {
            expression = expression.replace(entry.getKey(), entry.getValue().toString());
        }

        // Valuta espressioni matematiche semplici
        if (expression.contains("+")) {
            String[] parts = expression.split("\\+");
            double result = 0;
            for (String part : parts) {
                result += Double.parseDouble(part.trim());
            }
            return result;
        } else if (expression.contains("-") && !expression.startsWith("-")) {
            String[] parts = expression.split("-");
            double result = Double.parseDouble(parts[0].trim());
            for (int i = 1; i < parts.length; i++) {
                result -= Double.parseDouble(parts[i].trim());
            }
            return result;
        } else if (expression.contains("*")) {
            String[] parts = expression.split("\\*");
            double result = 1;
            for (String part : parts) {
                result *= Double.parseDouble(part.trim());
            }
            return result;
        } else if (expression.contains("/")) {
            String[] parts = expression.split("/");
            double result = Double.parseDouble(parts[0].trim());
            for (int i = 1; i < parts.length; i++) {
                result /= Double.parseDouble(parts[i].trim());
            }
            return result;
        }

        // Se è una stringa tra virgolette
        if (expression.startsWith("\"") && expression.endsWith("\"")) {
            return expression.substring(1, expression.length() - 1);
        }

        return expression;
    }

    private boolean evaluateCondition(String condition) throws Exception {
        condition = condition.trim();

        // Sostituisci le variabili con i loro valori
        for (Map.Entry<String, Object> entry : model.getVariables().entrySet()) {
            condition = condition.replace(entry.getKey(), entry.getValue().toString());
        }

        // Valuta condizioni semplici
        if (condition.contains("==")) {
            String[] parts = condition.split("==");
            return parts[0].trim().equals(parts[1].trim());
        } else if (condition.contains("!=")) {
            String[] parts = condition.split("!=");
            return !parts[0].trim().equals(parts[1].trim());
        } else if (condition.contains(">=")) {
            String[] parts = condition.split(">=");
            return Double.parseDouble(parts[0].trim()) >= Double.parseDouble(parts[1].trim());
        } else if (condition.contains("<=")) {
            String[] parts = condition.split("<=");
            return Double.parseDouble(parts[0].trim()) <= Double.parseDouble(parts[1].trim());
        } else if (condition.contains(">")) {
            String[] parts = condition.split(">");
            return Double.parseDouble(parts[0].trim()) > Double.parseDouble(parts[1].trim());
        } else if (condition.contains("<")) {
            String[] parts = condition.split("<");
            return Double.parseDouble(parts[0].trim()) < Double.parseDouble(parts[1].trim());
        }

        return false;
    }

    private Block getNextBlock(Block block, String output) {
        List<Connection> connections = block.getOutgoingConnections();

        if (connections.isEmpty()) {
            return null;
        }

        // Per blocchi decisionali, cerca la connessione appropriata
        if (block.getType() == BlockType.DECISION) {
            for (Connection conn : connections) {
                String label = conn.getLabel().toUpperCase();
                if ((output.equals("VERO") && (label.contains("SI") || label.contains("TRUE") || label.contains("VERO"))) ||
                    (output.equals("FALSO") && (label.contains("NO") || label.contains("FALSE") || label.contains("FALSO")))) {
                    return conn.getTargetBlock();
                }
            }
        }

        // Per altri blocchi, prendi la prima connessione
        return connections.get(0).getTargetBlock();
    }

    public void stopExecution() {
        running = false;
        currentBlock = null;
    }

    public boolean isRunning() {
        return running;
    }

    public Block getCurrentBlock() {
        return currentBlock;
    }

    public FlowchartModel getModel() {
        return model;
    }

    private void notifyBlockExecuted(Block block, String output) {
        if (listener != null) {
            SwingUtilities.invokeLater(() -> listener.onBlockExecuted(block, output));
        }
    }

    private void notifyComplete() {
        if (listener != null) {
            SwingUtilities.invokeLater(() -> listener.onExecutionComplete());
        }
    }

    private void notifyError(String error) {
        if (listener != null) {
            SwingUtilities.invokeLater(() -> listener.onExecutionError(error));
        }
    }

    private void notifyVariableChanged(String name, Object value) {
        if (listener != null) {
            SwingUtilities.invokeLater(() -> listener.onVariableChanged(name, value));
        }
    }
}
