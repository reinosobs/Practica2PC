package Principales;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import macking.MacKing;
import macking.ProductoPedido;


public class Personal {
	
	//Constantes de empleados, clientes y playas
	private static final int NUM_CLIENTES = 10;
	private static final int NUM_EMP_RECOGE_PEDIDOS = 5;
	private static final int NUM_EMP_EMPAQUETA_PEDIDOS= 5;
	private static final int NUM_EMP_ADMINISTRATIVOS = 5;
	private static final int NUM_EMP_LIMPIEZA = 2;
	private static final int NUM_PLAYAS = 5;
	
	
	
	private static ArrayList<Thread> hilos = new ArrayList<Thread>();
	
	
	private static Almazon almacen = new Almazon(NUM_PLAYAS, NUM_CLIENTES, NUM_EMP_RECOGE_PEDIDOS, NUM_EMP_EMPAQUETA_PEDIDOS, NUM_EMP_ADMINISTRATIVOS);

	
	public void EmpleadoAdministrativo() {
		while(true) {
			//System.out.println("Haciendo su vida...",500);
			almacen.entradaEmpleados();//hora de entrada de los empleados
			try {
				
				//double caja=0;
				while(!almacen.esFinJornada()) {//mientras el encargado no diga que se acaba la jornada, sigue trabajando
					Pedido pedido= almacen.esperarPedido();//esperamos a que lleguen los pedidos
					double importe= pedido.calcularImporte();//calculamos el importe del pedido que nos llega
					almacen.darImporte(importe,pedido);//Le damos el importe que debe de pagar el cliente de un pedido determinado
					//caja +=importe;
					//println("La caja tiene " + caja + " â‚¬");
					
					almacen.tramitarPedido(pedido);
					
					almacen.enviarNotificacion(pedido);
				}
			}catch (Exception e) {
				
			}
			almacen.salir();//cuando acabe la jornada, vamos saliendo para que el encargado cierre el almacen
		}
	}
	
	public void EmpleadoRecogePedidos() {
		while(true) {
			//System.out.println("Haciendo su vida...",500);
			almacen.entradaEmpleados();//hora de entrada de los empleados
			try {
			
				while(!almacen.esFinJornada()) {//mientras el encargado no diga que se acaba la jornada, sigue trabajando
					println("Esperando a la orden de pedido para recoger los productos solicitados")
					Pedido pedido= almacen.esperarPedido();//cogemos los pedidos de la cola de ordenes de pedidos
					for(Catalogo producto: pedido.getProductosPedido()) {
						if(producto.hayStock()) {
							println("Producto en stock. Cogemos el producto");
							almacen.cogerPlaya(pedido);//cogemos una playa, para determinado pedido, para ir depositando los productos en esa playa.
						}
					}
					println("Productos del pedido depositados en la playa");
					almacen.empaquetarPedido(pedido);//Cuando terminamos de coger los productos del pedido, damos la orden al empaquetador para que vaya a la playa a empaquetar el pedido
					
				}
			}catch (Exception e) {
				
			}
			almacen.salir();//cuando acabe la jornada, vamos saliendo para que el encargado cierre el almacen
		}
	}
	
	public void EmpleadoEmpaquetaPedidos() {
		while(true) {
			//System.out.println("Haciendo su vida...",500);
			almacen.entradaEmpleados();//hora de entrada de los empleados
			try {
			
				while(!almacen.esFinJornada()) {//mientras el encargado no diga que se acaba la jornada, sigue trabajando
					println("Esperando orden de pedido y productos depositados en una playa");
					Playa playa= almacen.esperarPedidoCompletoPlaya();//Esperamos que en una playa esten todos los productos de un pedido para obtener la orden de empaquetar
					Pedido pedido= playa.getPedido();//cogemos el pedido de la playa para empaquetarlo
					Paquete pedidoEmpaquetado= new Paquete();//cojemos una "caja/paquete" donde meteremos todos los productos del pedido
					for(Catalogo productoPedido : pedido.getProductosPedido()) {
						if(productoPedido.hayStock()) {//Con esta comprobacion el empleado comprueba de que no hay errores
							pedidoEmpaquetado.addProducto(productoPedido);
						}else {//Condicion en caso de que sea erroneo
//--------------------------------------------Revisar--------------------------------------------------
							println("Pedido con producto erroneo");
							almacen.pedidoErroneo(pedido,pedidoEmpaquetado);
							break;
						}
						//--------------------------------------------Revisar--------------------------------------------------						
					}
					println("El pedido "+pedido.getNumeroPedido()+ " está disponible para enviar. Informo al Administrador");
					almacen.enviarNotificacionAdministrador(pedido.getNumeroPedido(),pedido);
					//almacen.paqueteEmpaquetadoYEnviar(pedido,pedidoEmpaquetado);//Una vez que el pedido ya esta empaquetado y disponible para enviarse, se lo tenemos que decir al administrador.
				}
			}catch (Exception e) {
				
			}
			almacen.salir();//cuando acabe la jornada, vamos saliendo para que el encargado cierre el almacen
		}
	}
	
	
	//Metodos basicos 
	private static void println(String mensaje, long sleepMillis) {
		sleepRandom(sleepMillis);
		System.out.println(Thread.currentThread().getName() + ": " + mensaje);
	}

	public static void println(String mensaje) {
		System.out.println(Thread.currentThread().getName() + ": " + mensaje);
	}

	public static void sleepRandom(long millis) {
		sleep((long) (Math.random() * millis));
	}

	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}
	
	
	//-------------------------- Main ----------------------------------

		public static void createThreads(int numThreads, String name, Runnable r) {
			for (int i = 0; i < numThreads; i++) {
				Thread t = new Thread(r, name + "_" + i);
				hilos.add(t);
				t.start();
				
			}
		}

		public static void main(String[] args) {

			createThreads(NUM_CLIENTES, "Cliente", () -> cliente());
			
			createThreads(NUM_EMP_RECOGE_PEDIDOS, "EmpCogePedidos", () -> EmpleadoRecogePedidos());
			
			createThreads(NUM_EMP_EMPAQUETA_PEDIDOS, "EmpElaboraProductos" , () -> EmpleadoEmpaquetaPedidos());
			
			createThreads(NUM_EMP_ADMINISTRATIVOS, "EmpPreparaPedido", () -> EmpleadoAdministrativo());
			
			createThreads(NUM_EMP_LIMPIEZA, "EmpLimpieza", () -> empLimpieza());

			new Thread(() -> encargado(), "Encargado").start();
		}
}
