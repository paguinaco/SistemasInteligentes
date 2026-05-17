package marketplace.agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;

public class BuyerAgent extends Agent {
	private String productoDeseado;
	private AID[] vendedores;
	
	@Override
	protected void setup() {
		System.out.println("----------------------------------------");
		System.out.println("Comprador [" + getLocalName() + "] ha entrado al mercado");
		
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			productoDeseado = (String) args[0];
			System.out.println("Objetivo: Comprar " + productoDeseado);
			
			addBehaviour(new TickerBehaviour(this, 10000) {
				@Override
				protected void onTick() {
					System.out.println(" [" + getLocalName() + "] Buscando vendedores en el DF...");
					
					DFAgentDescription plantilla = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("venta-productos");
					plantilla.addServices(sd);
					
					try {
						DFAgentDescription[] result = DFService.search(myAgent, plantilla);
						System.out.println(" Se han encontrado " + result.length + " vendedores.");
						vendedores = new AID[result.length];
						
						for (int i = 0; i < result.length; i++) {
							vendedores[i] = result[i].getName();
						}
						
						if (result.length > 0) {
							System.out.println(" [" + getLocalName() + "] Enviando petición de compra (CFP)...");
							ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
							for (int i = 0; i < vendedores.length; ++i) {
								cfp.addReceiver(vendedores[i]);
							}
							cfp.setContent(productoDeseado);
							cfp.setConversationId("compra-venta");
							cfp.setReplyWith("cfp" + System.currentTimeMillis()); 
							myAgent.send(cfp);
							
							myAgent.addBehaviour(new RecibirOfertas(vendedores.length, cfp.getReplyWith()));
							stop();
						}
					} catch (FIPAException fe) {
						fe.printStackTrace();
					}
				}
			});
		} else {
			System.out.println(" ERROR: No se ha especificado ningún producto para comprar.");
			doDelete();
		}
	}

	private class RecibirOfertas extends Behaviour {
		private int respuestasEsperadas;
		private int respuestasRecibidas = 0;
		private String idConversacion;
		
		private int mejorPrecio = 999999;
		private AID mejorVendedor = null;
		private boolean terminado = false;
		private ArrayList<AID> vendedoresQueProponen = new ArrayList<>();

		public RecibirOfertas(int esperadas, String idConv) {
			this.respuestasEsperadas = esperadas;
			this.idConversacion = idConv;
		}

		public void action() {
			MessageTemplate mt = MessageTemplate.and(
				MessageTemplate.MatchConversationId("compra-venta"),
				MessageTemplate.MatchInReplyTo(idConversacion)
			);
			
			ACLMessage respuesta = myAgent.receive(mt);
			
			if (respuesta != null) {
				respuestasRecibidas++;
				
				if (respuesta.getPerformative() == ACLMessage.PROPOSE) {
					int precio = Integer.parseInt(respuesta.getContent());
					System.out.println(" [" + getLocalName() + "] Oferta recibida de " + respuesta.getSender().getLocalName() + ": " + precio + " euros");
					
					vendedoresQueProponen.add(respuesta.getSender());
					
					if (mejorVendedor == null || precio < mejorPrecio) {
						mejorPrecio = precio;
						mejorVendedor = respuesta.getSender();
					}
				}
				
				if (respuestasRecibidas >= respuestasEsperadas) {
					if (mejorVendedor != null) {
						System.out.println(" [" + getLocalName() + "] ---> COMPRA REALIZADA a " + mejorVendedor.getLocalName() + " por " + mejorPrecio + " euros.");
						
						for (AID vendedor : vendedoresQueProponen) {
							ACLMessage orden = new ACLMessage(vendedor.equals(mejorVendedor) ? ACLMessage.ACCEPT_PROPOSAL : ACLMessage.REJECT_PROPOSAL);
							orden.addReceiver(vendedor);
							orden.setConversationId("compra-venta");
							orden.setContent(productoDeseado);
							myAgent.send(orden);
						}
					} else {
						System.out.println(" [" + getLocalName() + "] ---> Ningún vendedor tiene el producto.");
					}
					terminado = true;
				}
			} else {
				block();
			}
		}

		public boolean done() {
			return terminado;
		}
	}

	@Override
	protected void takeDown() {
		System.out.println(" Comprador [ " + getLocalName()+  "] abandonando el mercado.");
		System.out.println("---------------------------------------");


	}
}