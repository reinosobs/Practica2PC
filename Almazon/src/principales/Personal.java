package principales;

import java.util.ArrayList;

public class Personal {
	
	private static final int NUM_CLIENTES=1;
	private static final int NUM_EMP_RECOGE_PEDIDOS=1;
	private static final int NUM_EMP_LIMPIEZA=1;
	private static final int NUM_EMP_ADMINISTRATIVOS=1;
	private static final int NUM_EMP_EMPAQUETA_PEDIDOS=2;
	private static final int NUM_PLAYAS=2;
		

	private static ArrayList<Thread> hilos= new ArrayList<Thread>();
	
	private static Almazon almacen = new Almazon(NUM_PLAYAS, NUM_CLIENTES, NUM_EMP_RECOGE_PEDIDOS, NUM_EMP_EMPAQUETA_PEDIDOS, NUM_EMP_ADMINISTRATIVOS);
	
	
	public static void EmpleadoAdministrativo() {								//FALTA Tramitar pedido y Salir de almacen.
		while(true) {
			println("Duermo", 1000);
			almacen.entrarTrabajar();
			try {
				while(!almacen.finDeJornada()) {
					Pedido pedido=almacen.esperarPedido();
					double importe=pedido.calcularImporte();
					println("el numero de productos del pedido"+ pedido.getNumeroPedido()+" es: "+ pedido.getProductosPedido().size());
					
					almacen.darImporte(importe,pedido);
					
					
					almacen.tramitarPedido(pedido);
					
					almacen.enviarNotificacion(pedido);
				}
			}catch(Exception e) {}
			
			almacen.salir();
		}
	}
	
	public static void EmpleadoRecogePedidos() {
		while(true) {
			almacen.entrarTrabajar();
			println("Duermo", 5000);
			try {
				
				while(!almacen.finDeJornada()) {
					println("Esperando a la orden del pedido para recoger los productos solicitados");
					Pedido pedido= almacen.esperarPedidoParaRecoger();
					
					for(Catalogo producto:pedido.getProductosPedido()) {
						if(producto.hayStock()) {
							println("Producto en stock. Cogemos el producto");
							
						}
						else {
							println("Producto no en Stock");
							break;
						}
					}
					
					almacen.empaquetarPedido(pedido);
				}
				
			}catch(Exception e) {}
			almacen.salir();			
		}
	}
	
	public static void EmpleadoEmpaquetaPedidos() {
		
		while(true){
			almacen.entrarTrabajar();
			println("Duermo", 5000);
			try {
				
				while(!almacen.finDeJornada()) {
					println("Esperando orden de pedido y productos depositados en una playa");
					Pedido pedido=almacen.esperarPedidoParaEmpaquetar();								
					println("Tengo ya el pedido");
					Playa playa=almacen.cogerPlaya();
					println("Ya tengo el pedido y la playa");
					Paquete pedidoEmpaquetado= new Paquete();
					println("Cojo paquete para empaquetar");
					
					for(Catalogo productoPedido: pedido.getProductosPedido()) {
						if(productoPedido.hayStock()) {
							println("Producto al paquete");
							Producto producto= new Producto(productoPedido);
							pedidoEmpaquetado.addProducto(producto);
						}else {
							println("Pedido con producto erroneo");
							almacen.pedidoErroneo(pedido, pedidoEmpaquetado);
							break;
						}
					}
					println("Los pedidos que se han hecho en esta playa son: "+ playa.getPedidosHechos());
					println("Una vez todo empaquetado, salgo de la playa");
					almacen.soltarPlaya(playa);
					
					println("El pedido "+pedido.getNumeroPedido()+ " se encuentra disponible para enviar. Informo al administrador");
					almacen.enviarNotificacionAdministrador(pedido.getNumeroPedido(), pedido);
				}
			}catch(Exception e) {}
			
			almacen.salir();
				
		}
	}
	
	private static void empLimpieza() {
		while (true) {
		println("Dormir, Ir al gimnasio...", 1000);
		// Esperar hasta poder entrar como empleado
		almacen.entrarTrabajar();
		try {
			while (!almacen.finDeJornada()) {
				
				almacen.LimpiarPlaya();
				println("Limpiando playas ",500);

			}
		}catch (Exception e) {

			almacen.salir();
		}
		almacen.salir();
	}
	
	}

	private static void cliente() {
		double dinero=500;
		
		while(true) {
			println("hacer mi vida", 100);
			
			try {
				
				Pedido pedido =Pedido.crearPedidoRandom();
				println("Voy a hacer un pedido");
				almacen.hacerPedido(pedido);
				
				double importe = almacen.esperarImporte(pedido);
				println("El pedido me cuesta un importe de: "+ importe);
				if(importe>dinero) {
					almacen.salir();
					break;
				}
				
				dinero-=importe;
				println("El pedido cuesta "+ importe);
				importe=0;
				println("Me queda "+ dinero+ " euros todavía.");
				
				Pedido pedidoNotificado = almacen.esperarNotificacionAdministrador(pedido);
				
				println("Recibida notificación por parte del administrador: Pedido " + pedidoNotificado.getNumeroPedido()+" en camino.");
				
			}catch (Exception e) {}
		}
	}
	
	private static void encargado() {
		while(true) {
			println("Dormir, pasear,...", 500);
			try {
				almacen.abrir();
				println("Organizar el día", 500);
				
				//Resumen del almacen.
				almacen.supervisionAlmacen();
				
				//Interrumpimos todos los hilos para inicializarlos por el cambio de turno.
				for (Thread t: hilos) {
					t.interrupt();
				}
				
				almacen.cambioTurno();
				
				//Inicializamos tras el cambio de turno.
				for(Thread t: hilos) {
					t.start();
				}
				
				//Supervisamos el almacen del nuevo turno.
				almacen.supervisionAlmacen();
								
				almacen.echarCiere();
				
				//Interrumpimos para el cierre del almacen.
				for (Thread t: hilos) {
					t.interrupt();
				}
				
				almacen.cerrarAlmacen();		//inicializar todo.
				
			}catch(Exception e) {
				
			}
			
			println("Almacen cerrado");
		}
		
	}
	
		
	//MÉTODOS BÁSICOS.
	private static void println(String mensaje, long sleepMillis) {
		sleepRandom(sleepMillis);
		System.out.println(Thread.currentThread().getName()+ ": "+ mensaje);
	}
	public static void println(String mensaje) {
		System.out.println(Thread.currentThread().getName()+ ": "+mensaje);
	}
	public static void sleepRandom(long millis) {
		sleep((long) (Math.random()*millis));
	}
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		}catch(InterruptedException e) {}
	}
	
	
	//-----------------------------MAIN-----------------------------------------------
	
	public static void createThreads(int numThreads, String name, Runnable r) {
		for(int i=0; i<numThreads; i++) {
			Thread t= new Thread(r,name+ "_"+ i);
			hilos.add(t);
			t.start();
		}
	}
	
	public static void main(String[] args) {
		createThreads(NUM_CLIENTES, "Cliente", ()-> cliente());
		createThreads(NUM_EMP_RECOGE_PEDIDOS, "RecogePedidos", ()-> EmpleadoRecogePedidos());
		createThreads(NUM_EMP_EMPAQUETA_PEDIDOS, "EmpaquetaPedidos", ()-> EmpleadoEmpaquetaPedidos());
		createThreads(NUM_EMP_ADMINISTRATIVOS, "Administrador", ()-> EmpleadoAdministrativo());
		createThreads(NUM_EMP_LIMPIEZA, "EmpLimpieza", ()-> empLimpieza());
		new Thread(()->encargado(), "Encargado").start();
			
	}	
	
}
