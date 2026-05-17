package marketplace.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.Hashtable;

public class SellerAgent extends Agent {
	private Hashtable<String, Integer> catalogo;

	@Override
	protected void setup() {
		System.out.println("----------------------------------------");
		System.out.println("Vendedor [" + getLocalName() + "] ha entrado en el mercado");
		
		catalogo = new Hashtable<>();
		catalogo.put("manzanas", (int) (Math.random() * 10) + 1);
		catalogo.put("peras", (int) (Math.random() * 10) + 1);
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID()); 
		
		ServiceDescription sd = new ServiceDescription();
		sd.setType("venta-productos");
		sd.setName("Mercado-JADE");
		dfd.addServices(sd); 
		
		try {
			DFService.register(this, dfd);
			System.out.println(" [" + getLocalName() + "] registrado en el DF.");
		} catch (FIPAException fe) {
			System.err.println("Error al registrar en el DF: " + fe.getMessage());
		}
		
		addBehaviour(new EscucharPeticiones());
		System.out.println("----------------------------------------");		
	}
	
	private class EscucharPeticiones extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			
			if (msg != null) {
				String producto = msg.getContent();
				System.out.println(" [" + getLocalName() + "] Recibido CFP por: " + producto);
				
				ACLMessage reply = msg.createReply();
				
				Integer precio = catalogo.get(producto.toLowerCase());
				if (precio != null) {
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent(String.valueOf(precio));
					System.out.println(" [" + getLocalName() + "] -> Ofreciendo " + producto + " por " + precio + " euros.");
				} else {
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("no-disponible");
					System.out.println(" [" + getLocalName() + "] -> Rechazando, no tengo " + producto);
				}
				myAgent.send(reply);
			} else {
				block();
			}
		}
	}
	
	@Override
	protected void takeDown() {
		try {
			DFService.deregister(this);
			System.out.println(" Vendedor [" + getLocalName() + "] se ha dado de baja del DF.");
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		System.out.println("Vendedor [" + getAID().getName() + "] terminado.");
	}

}

