package marketplace.agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class InterfazAgent extends Agent {

    private JFrame ventana;
    private JTextField inputProducto;
    private JTextArea pantallaResultados;

    @Override
    protected void setup() {
        System.out.println("💻 Agente de Interfaz [" + getLocalName() + "] arrancado.");

        SwingUtilities.invokeLater(() -> construirInterfaz());

        // Escuchar lo que nos envíe el BuyerAgent y pintarlo en la ventana
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive();
                if (msg != null) {
                    pantallaResultados.append(msg.getContent() + "\n");
                    pantallaResultados.setCaretPosition(pantallaResultados.getDocument().getLength());
                } else {
                    block();
                }
            }
        });
    }

    private void construirInterfaz() {
        ventana = new JFrame("Mercado Inteligente (Con Broker) - Panel de Control");
        ventana.setSize(600, 450);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setLayout(new BorderLayout());

        JPanel panelNorte = new JPanel();
        panelNorte.setBackground(new Color(230, 240, 255));
        panelNorte.add(new JLabel("¿Qué producto deseas comprar?"));
        
        inputProducto = new JTextField(15);
        panelNorte.add(inputProducto);
        
        JButton btnBuscar = new JButton("Iniciar Negociación");
        btnBuscar.setBackground(new Color(100, 150, 240));
        btnBuscar.setForeground(Color.WHITE);
        panelNorte.add(btnBuscar);

        pantallaResultados = new JTextArea();
        pantallaResultados.setEditable(false);
        pantallaResultados.setFont(new Font("Consolas", Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(pantallaResultados);
        
        ventana.add(panelNorte, BorderLayout.NORTH);
        ventana.add(scroll, BorderLayout.CENTER);

        btnBuscar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String producto = inputProducto.getText().trim();
                if (!producto.isEmpty()) {
                    pantallaResultados.append("🚀 Solicitando buscar: " + producto + "...\n");
                    
                    addBehaviour(new OneShotBehaviour() {
                        @Override
                        public void action() {
                            ACLMessage orden = new ACLMessage(ACLMessage.REQUEST);
                            orden.addReceiver(new AID("comprador1", AID.ISLOCALNAME));
                            orden.setContent(producto);
                            myAgent.send(orden);
                        }
                    });
                    inputProducto.setText("");
                }
            }
        });

        ventana.setVisible(true);
    }
}