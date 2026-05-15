package marketplace.agents;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class BuyerAgent extends Agent {
	private String productoDeseado;
	private AID[]vendedores;
	
	@Override
	protected void setup() {
		System.out.println("----------------------------------------");
		System.out.println("Comprador [" + getLocalName() + "] ha entrado al mercado");
		
		Object[]args = getArguments();
		if(args!=null&&args.length>0) {
			productoDeseado= (String) args[0];
			System.out.println("Objetivo: Comprar " + productoDeseado);
			
			addBehaviour(new TickerBehaviour(this, 10000){
				@Override
				protected void onTick() {
					System.out.println("  [" + getLocalName() + "] Buscando vendedores en el DF...");
					
					DFAgentDescription plantilla= new DFAgentDescription();
					ServiceDescription sd= new ServiceDescription();
					sd.setType("venta-productos");
					plantilla.addServices(sd);
					try {
						DFAgentDescription[]result= DFService.search(myAgent, plantilla);
						System.out.println(" Se han encontrado " + result.length + " vendedores." );
						vendedores= new AID[result.length];
						for (int i=0; i< result.length;i++) {
							vendedores[i]=result[i].getName();
							System.out.println( "   - Vendedor localizado: " +vendedores[i].getName());
						}
						
						if (result.length>0) {
							System.out.println("Búsqueda finalizada. El comprador ya sabe a quién contactar");
							stop();
						}
					} catch (FIPAException fe) {
						fe.printStackTrace();
					}
				}
			});
		} else {
			System.out.println(" ERROR: No se ha especificado ningún producto para comprar. ");
			doDelete();
		}

	}
	@Override
	protected void takeDown() {
		System.out.println(" Comprador [ " + getLocalName()+  "] abandonando el mercado.");
		System.out.println("---------------------------------------");

	}
	
}
