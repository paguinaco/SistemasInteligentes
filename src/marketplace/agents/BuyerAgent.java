package marketplace.agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BuyerAgent extends Agent {
	private String productoDeseado;
	private AID broker; // Ahora hablamos con el broker, no con los vendedores
	
	@Override
	protected void setup() {
		System.out.println("----------------------------------------");
		System.out.println("Comprador [" + getLocalName() + "] ha entrado al mercado");
		System.out.println(" [" + getLocalName() + "] Esperando órdenes de la interfaz...");
		
		addBehaviour(new CyclicBehaviour(this) {
			@Override
			public void action() {
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
				ACLMessage orden = myAgent.receive(mt);
				
				if (orden != null) {
					productoDeseado = orden.getContent();
					System.out.println(" [" + getLocalName() + "] ¡Orden de la interfaz recibida! Objetivo: " + productoDeseado);
					enviarMensajeInterfaz("Iniciando búsqueda de: " + productoDeseado + " a través del Broker...");
					
					buscarBrokerYPeticion();
				} else {
					block();
				}
			}
		});
	}


	private void enviarMensajeInterfaz(String texto) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.addReceiver(new AID("interfaz", AID.ISLOCALNAME));
		msg.setContent(texto);
		send(msg);
	}

	private void buscarBrokerYPeticion() {
		DFAgentDescription plantilla = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("broker-marketplace"); // Buscamos el servicio que creó Andrea
		plantilla.addServices(sd);
		
		try {
			DFAgentDescription[] result = DFService.search(this, plantilla);
			
			if (result.length > 0) {
				broker = result[0].getName(); // Asignamos el broker encontrado
				enviarMensajeInterfaz(" Broker encontrado. Delegando negociación...");
				
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				cfp.addReceiver(broker);
				cfp.setContent(productoDeseado);
				cfp.setConversationId("compra-venta");
				cfp.setReplyWith("cfp-comprador-" + System.currentTimeMillis()); 
				send(cfp);
				
				addBehaviour(new EsperarRespuestaBroker(cfp.getReplyWith()));
			} else {
				enviarMensajeInterfaz("ERROR: No hay ningún Broker disponible en el mercado.");
			}
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}

	
	private class EsperarRespuestaBroker extends Behaviour {
		private String idConversacion;
		private boolean terminado = false;

		public EsperarRespuestaBroker(String idConv) {
			this.idConversacion = idConv;
		}

		public void action() {
			MessageTemplate mt = MessageTemplate.and(
				MessageTemplate.MatchConversationId("compra-venta"),
				MessageTemplate.MatchInReplyTo(idConversacion)
			);
			
			ACLMessage respuesta = myAgent.receive(mt);
			
			if (respuesta != null) {
				if (respuesta.getPerformative() == ACLMessage.PROPOSE) {
					int precio = Integer.parseInt(respuesta.getContent());
					enviarMensajeInterfaz("✅ El Broker ha conseguido la mejor oferta por " + precio + "€");
					
					// Confirmamos la compra al broker (ACCEPT_PROPOSAL)
					ACLMessage aceptacion = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
					aceptacion.addReceiver(broker);
					aceptacion.setContent(productoDeseado);
					aceptacion.setConversationId("compra-venta");
					aceptacion.setInReplyTo(respuesta.getReplyWith());
					myAgent.send(aceptacion);
					
					enviarMensajeInterfaz("🎉 ¡Compra confirmada con éxito!");
					
				} else if (respuesta.getPerformative() == ACLMessage.REFUSE) {
					enviarMensajeInterfaz("❌ El Broker informa: Ningún vendedor tiene este producto.");
				}
				terminado = true;
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
		System.out.println(" Comprador [" + getLocalName()+ "] abandonando el mercado.");
		System.out.println("----------------------------------------");
	}
}