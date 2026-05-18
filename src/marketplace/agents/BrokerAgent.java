package marketplace.agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.FIPAException;

public class BrokerAgent extends Agent {

	@Override
	protected void setup() {
		System.out.println("----------------------------------------");
		System.out.println("Broker [" + getLocalName() + "] iniciado.");

		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());

		ServiceDescription sd = new ServiceDescription();
		sd.setType("broker-marketplace");
		sd.setName("Broker-JADE");

		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
			System.out.println(" [" + getLocalName() + "] registrado en el DF.");
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		addBehaviour(new RecibirPeticionesCompradores());

		System.out.println("----------------------------------------");
	}
	
	private void enviarMensajeInterfaz(String texto) {
		ACLMessage msg= new ACLMessage(ACLMessage.INFORM);
		msg.addReceiver(new AID("interfaz", AID.ISLOCALNAME));
		msg.setContent(texto);
		send(msg);
	}

	private class RecibirPeticionesCompradores extends CyclicBehaviour {
		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.and(
				MessageTemplate.MatchConversationId("compra-venta"),
				MessageTemplate.MatchPerformative(ACLMessage.CFP)
			);

			ACLMessage msg = myAgent.receive(mt);

			if (msg != null) {
				String producto = msg.getContent();
				AID comprador = msg.getSender();
				String replyWithComprador = msg.getReplyWith();

				System.out.println("\n[BROKER] Petición recibida de " + comprador.getLocalName());
				System.out.println("[BROKER] Producto solicitado: " + producto);

				myAgent.addBehaviour(new NegociarConVendedores(producto, comprador, replyWithComprador));
			} else {
				block();
			}
		}
	}

	private class NegociarConVendedores extends Behaviour {
		private String producto;
		private AID comprador;
		private String replyWithComprador;

		private AID[] vendedores;
		private int respuestasEsperadas = 0;
		private int respuestasRecibidas = 0;

		private int mejorPrecio = Integer.MAX_VALUE;
		private AID mejorVendedor = null;

		private String idCFPVendedores;
		private String idOfertaComprador;

		private int paso = 0;
		private boolean terminado = false;

		public NegociarConVendedores(String producto, AID comprador, String replyWithComprador) {
			this.producto = producto;
			this.comprador = comprador;
			this.replyWithComprador = replyWithComprador;
		}

		@Override
		public void action() {
			switch (paso) {
				case 0:
					buscarYEnviarCFP();
					break;
				case 1:
					recibirOfertas();
					break;
				case 2:
					esperarRespuestaComprador();
					break;
			}
		}

		private void buscarYEnviarCFP() {
			DFAgentDescription plantilla = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("venta-productos");
			plantilla.addServices(sd);

			try {
				DFAgentDescription[] result = DFService.search(myAgent, plantilla);
				System.out.println("[BROKER] Vendedores encontrados: " + result.length);

				if (result.length == 0) {
					enviarRefuseAlComprador();
					terminado = true;
					return;
				}

				vendedores = new AID[result.length];
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);

				for (int i = 0; i < result.length; i++) {
					vendedores[i] = result[i].getName();
					cfp.addReceiver(vendedores[i]);
				}

				idCFPVendedores = "cfp-broker-" + System.currentTimeMillis();

				cfp.setContent(producto);
				cfp.setConversationId("compra-venta");
				cfp.setReplyWith(idCFPVendedores);

				respuestasEsperadas = vendedores.length;

				System.out.println("[BROKER] Enviando CFP a vendedores por: " + producto);
				myAgent.send(cfp);

				paso = 1;

			} catch (FIPAException e) {
				e.printStackTrace();
				enviarRefuseAlComprador();
				terminado = true;
			}
		}

		private void recibirOfertas() {
			MessageTemplate mt = MessageTemplate.and(
				MessageTemplate.MatchConversationId("compra-venta"),
				MessageTemplate.MatchInReplyTo(idCFPVendedores)
			);

			ACLMessage respuesta = myAgent.receive(mt);

			if (respuesta != null) {
				respuestasRecibidas++;

				if (respuesta.getPerformative() == ACLMessage.PROPOSE) {
					int precio = Integer.parseInt(respuesta.getContent());

					System.out.println("[BROKER] Oferta de " + respuesta.getSender().getLocalName() + ": " + precio + " euros");

					if (precio < mejorPrecio) {
						mejorPrecio = precio;
						mejorVendedor = respuesta.getSender();
					}
				}

				if (respuestasRecibidas >= respuestasEsperadas) {
					if (mejorVendedor != null) {
						enviarMejorOfertaAlComprador();
						paso = 2;
					} else {
						enviarRefuseAlComprador();
						terminado = true;
					}
				}
			} else {
				block();
			}
		}

		private void enviarMejorOfertaAlComprador() {
			idOfertaComprador = "oferta-broker-" + System.currentTimeMillis();

			ACLMessage respuestaComprador = new ACLMessage(ACLMessage.PROPOSE);
			respuestaComprador.addReceiver(comprador);
			respuestaComprador.setContent(String.valueOf(mejorPrecio));
			respuestaComprador.setConversationId("compra-venta");
			respuestaComprador.setInReplyTo(replyWithComprador);
			respuestaComprador.setReplyWith(idOfertaComprador);

			System.out.println("[BROKER] Mejor oferta: " + mejorVendedor.getLocalName() + " por " + mejorPrecio + " euros");
			System.out.println("[BROKER] Enviando mejor oferta al comprador.");

			myAgent.send(respuestaComprador);
		}

		private void esperarRespuestaComprador() {
			MessageTemplate mt = MessageTemplate.and(
				MessageTemplate.MatchConversationId("compra-venta"),
				MessageTemplate.MatchInReplyTo(idOfertaComprador)
			);

			ACLMessage respuesta = myAgent.receive(mt);

			if (respuesta != null) {
				if (respuesta.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
					System.out.println("[BROKER] El comprador ha aceptado la oferta.");
					confirmarVendedorGanador();
					rechazarRestoVendedores();
					System.out.println("[BROKER] Venta confirmada con " + mejorVendedor.getLocalName() + ".");
				} else if (respuesta.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
					System.out.println("[BROKER] El comprador ha rechazado la oferta.");
					rechazarRestoVendedores();
				}

				terminado = true;
			} else {
				block();
			}
		}

		private void confirmarVendedorGanador() {
			ACLMessage confirmacion = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
			confirmacion.addReceiver(mejorVendedor);
			confirmacion.setContent(producto);
			confirmacion.setConversationId("compra-venta");
			myAgent.send(confirmacion);
		}

		private void rechazarRestoVendedores() {
			if (vendedores == null) return;

			for (AID vendedor : vendedores) {
				if (!vendedor.equals(mejorVendedor)) {
					ACLMessage rechazo = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
					rechazo.addReceiver(vendedor);
					rechazo.setContent(producto);
					rechazo.setConversationId("compra-venta");
					myAgent.send(rechazo);
				}
			}
		}

		private void enviarRefuseAlComprador() {
			ACLMessage respuestaComprador = new ACLMessage(ACLMessage.REFUSE);
			respuestaComprador.addReceiver(comprador);
			respuestaComprador.setContent("no-disponible");
			respuestaComprador.setConversationId("compra-venta");
			respuestaComprador.setInReplyTo(replyWithComprador);

			System.out.println("[BROKER] No hay ofertas disponibles.");
			myAgent.send(respuestaComprador);
		}

		@Override
		public boolean done() {
			return terminado;
		}
	}

	@Override
	protected void takeDown() {
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		System.out.println("Broker [" + getLocalName() + "] terminado.");
	}
}
//prueba
