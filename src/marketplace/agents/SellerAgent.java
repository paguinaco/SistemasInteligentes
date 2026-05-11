package marketplace.agents;
import jade.core.Agent;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class SellerAgent extends Agent{
	@Override
	protected void setup() {
		System.out.println("----------------------------------------");
		System.out.println("Vendedor [" + getLocalName() + "] ha entrado en el mercado");
		
		//Registro en el DF Directory Facilitator
		DFAgentDescription dfd=new DFAgentDescription();
		dfd.setName(getAID()); //identidad real en JADE
		
		ServiceDescription sd=new ServiceDescription();
		sd.setType("venta-productos");
		sd.setName("Mercado-JADE");
		
		dfd.addServices(sd); //Servicio añadido a la descripción del agente
		//Intento de registro en DF
		try {
			DFService.register(this, dfd);
			System.out.println(" [" + getLocalName() + "] registrado en las Páginas Amarillas (DF)");
			
		}catch (FIPAException fe) {
			System.err.println("Error al registrar en el DF: " + fe.getMessage());
			
		}
		System.out.println("----------------------------------------");		
	}
	
	@Override
	protected void takeDown() {
		try {
			DFService.deregister(this);
			System.out.println(" Vendedor [" +getLocalName() + " se ha dado de baja del DF.");
			
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		System.out.println("Vendedor [ " + getAID().getName() + " ] terminado.");
	}
	

}
