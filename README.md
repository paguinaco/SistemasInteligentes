# Marketplace Inteligente en JADE

**GRUPO SISTEMAS INTELIGENTES, CURSOS 2025-2026**
* Pelayo Aguinaco Álvarez, 210200
* Rafael Coloma Muro, 210072
* Andrea Avilés Cruz, 220056

Repositorio GitHub: https://github.com/paguinaco/SistemasInteligentes.git
-----------------------------------
## Descripción del Proyecto y Evolución
Este proyecto es sobre un sistema multiagente desarrollado mediante JADE en el que se simula un mercado
virtual automatizado.
En nuestra propuesta inicial planteamos que el Agente Comprador leyera una lsita de productos desde un fichero externo.
Sin embargo, para cumplir con los requisitos de la práctica, hemos evolucionado la arquitectura mediante la implementación
de una clase llamada InterfazAgent, que es una interfaz gráfica hecha con Java Swing así como un BrokerAgent el cual actúa
de intermediario, por lo tanto el flujo de este programa es Interfaz (usuario)--> Comprador (Buyer)--> Negociador(Broker)--> 
Vendedor (Seller).
Además, esta arquitectura permite una negociación autónoma mediante mensajes FIPA-ACL y uso avanzado de filtros en modo
bloqueante

---
## Instrucciones de instalación y dependencias necesarias
Para compilar y ejecutar este proyecto, se debe contar con:
* **Java Development Kit(JDK):** al menos la versión 8 o superior
*  **IDE:** Eclipse IDE for Java Developers
*  **JADE Framework:** Librería 'jade.jar' que se necesita para el Build Path

**Captura de dependencias configuradas en Eclipse:**
<img width="708" height="285" alt="{7C261824-550C-4C73-B815-42557F08D1E6}" src="https://github.com/user-attachments/assets/843640fd-ca6a-4fac-b818-dfe4f4961255" />

## Instrucciones de ejecución
1. Se deberá clonar el repositorio en el entorno local
2. Abrir el proyecto en Eclipse y asegurar que 'jade.jar' está añadido al Build Path.
3. Ir al menú superior: 'Run > Run Configurations...'
4. Una vez ahí, se debe crear una nueva configuración de tipo Java Application y seleccionar 'jade.Boot'
5. En la pestaña **Arguments** , se debe introducir (al menos para probar que funciona) la siguiente línea:
`-gui broker:marketplace.agents.BrokerAgent;vendedor1:marketplace.agents.SellerAgent;vendedor2:marketplace.agents.SellerAgent;comprador1:marketplace.agents.BuyerAgent;interfaz:marketplace.agents.InterfazAgent`
6. Hscer clic en **run** donde aparecerá la consola de JADE y nuestra ventana de Panel de Control

---------
## Datos de ejemplo para ejecutar la práctica
Una vez que la interfaz esté abierta, el usuario puede interactuar con el sistema introduciendo productos.
Los Agentes vendedores inicializar su catálogo aleatoriamente con stock de prueba.
Para que se pueda ver una negociación exitosa, se introduce en la caja de texto
* manzanas
* peras
* También funciona con otras opciones que están en el código.
 
El sistema buscará ofertas, comparará precios a través del broker y devolverá el resultado por pantalla con pausas
simulando tiempo real de red. Únicamente existe un único BrokerAgent, aunque podrían existir más pero al tratarse de un
mercado sencillo hemos decidido poner solo uno; BuyerAgent pueden existir más en JADE pero no en la interfaz que hemos realizado;
SellerAgent puede haber muchos más (dependerá de lo que pongamos en Arguments, lo explicado en el apartado anterior)
y el InterfazAgent solo uno.

## Detalles sobre nuestra práctica
* Únicamente existe un único BrokerAgent, aunque podrían existir más pero al tratarse de un
mercado sencillo hemos decidido poner solo uno; BuyerAgent pueden existir más en JADE pero no en la interfaz que hemos realizado;
SellerAgent puede haber muchos más (dependerá de lo que pongamos en Arguments, lo explicado en el apartado anterior)
y el InterfazAgent solo uno.
* Puede ser que varios vendedores dispongan de un producto al mismo precio, en ese caso se usa el concepto de
'First-Come, First-Served', es decir, si el vendedor1 vende peras a 3€ y el vendedor 4 igual, el producto lo venderá el vendedor 1
al estar primero por la condición que pusimos de if(precio< mejorPrecio).

## Diagrama de la arquitectura del sistema
<img width="771" height="556" alt="DiagramaSSII arquitectura" src="https://github.com/user-attachments/assets/4dd9afaf-e583-455f-abff-44be953de11d" />

## Declaración de uso de IA
Tal y como se pide en el enunciado de la práctica, declaramos que se ha utilizado asistencia de IA generativa durante el desarrollo 
de la práctica para ciertas dudas:
* **Solucionado de conflictos en Git**: para solucionar errores que nos aparecían del tipo 'non-fast-forward' y 'Merge Conficts' cuando
* integrabamos nuestro trabajo.
* **Mejoras** : generación de pausas para que se simulase como si fuese algo más realista, ya que al principio cuando probábamos la interfaz
* gráfica, aparecía todo al instante y no era tan realista (doWait())
* **Conflictos del buildpath**: ayuda a corrgir problemas de configuración del 'Build Path' relacionados con el 'jade.jar'
Hemos usado la IA exclusivamente como herramienta de apoyo, solución de conflictos y consulta técnica; el resto ha sido realizado por nsosotros
mismos.
