package Principales;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Exchanger;

public class Almazon {
	
	//Hash para los exchange de esperas al importe de pedido
	ConcurrentHashMap<Integer, Exchanger<Double>> hash_espera_importe = new ConcurrentHashMap<Integer, Exchanger<Double>> ();
	
	//Hash para los pedidos listos. 
	ConcurrentHashMap<Pedido, Exchanger<Paquete>> hash_pedido_listo = new ConcurrentHashMap<Pedido, Exchanger<Paquete>> ();
			 
	//Hash para los pedidos erroneos.
	ConcurrentHashMap<Pedido, Paquete> hash_pedidos_atencion = new ConcurrentHashMap<Pedido, Paquete> ();
	
	//Hash para las notificaciones de pedidos
	ConcurrentHashMap<Integer,Pedido> hash_pedidos_notificaciones = new ConcurrentHashMap<Integer,Pedido> ();
	
	public Almazon(int numPlayas, int numClientes, int numEmpRecogePedidos, int numEmpEmpaquetaPedidos,
			int numEmpAdministrativos) {
		// TODO Auto-generated constructor stub
	}

	public void entradaEmpleados() {
		// TODO Auto-generated method stub
		
	}

	public boolean esFinJornada() {
		// TODO Auto-generated method stub
		return false;
	}

	public Pedido esperarPedido() {
		// coger los pedidos pendientes a recoger los productos del pedido. Sera de una lista de pedidos pendientes
		return null;
	}

	public void empaquetarPedido(Pedido pedido) {
		//Aqui debe de introducir el pedido que viene en una lista de pedidos disponibles en playas para empaquetar
		//donde el empaquetador pueda ir a esa playa a empaquetar
		
	}

	public Playa esperarPedidoCompletoPlaya() {
		//cojo un pedido que este listo en una playa para poder empaquetar de la lista, cogiendo el numero de pedido.
		return null;
	}

	
	public void pedidoErroneo(Pedido pedido, Paquete paquete) {//añadimos el paquete erroneo a la lista de pedidos para revisar por el recogepedidos
		hash_pedidos_atencion.put(pedido,paquete);
		
	}
	
	/* public void paqueteEmpaquetadoYEnviar(Pedido pedido, Paquete paquete) throws InterruptedException {//Empaquetado y enviamos el paquete y el aviso al administrador
		Exchanger<Paquete> canal = hash_pedido_listo.get(pedido);
		canal.exchange(paquete);
	}
	public Paquete esperarPedido(Pedido pedido) throws InterruptedException {//este metodo es para que el cliente ya reciba el paquete de su pedido
		
		Exchanger<Paquete> canal = new Exchanger<Paquete>();
		hash_pedido_listo.put(pedido, canal);
		//ticket_a_preparar.put(ticket);
		
		
		Paquete paquete = null;
		paquete = canal.exchange(null);
		
		return paquete;
		
	}creemos que no nos pide que tengamos que decir que el cliente ya ha recibido el paquete*/

	public void enviarNotificacionAdministrador(int NumPedido,Pedido pedido) {
		
		hash_pedidos_notificaciones.put(NumPedido, pedido);
		
	}

	public void enviarNotificacion(Pedido pedido) {
		
		Pedido notificacionPedido= hash_pedidos_notificaciones.get(pedido.getNumeroPedido());
		
		System.out.println(Thread.currentThread().getName() + ": "+"El pedido "+notificacionPedido.getNumeroPedido()+" ya ha sido empaquetado y enviado");
		
		//No sabemos si debemos de borrar la notificacion debido a que cada notificacion tendra su pedido
		
	}
	
	public void darImporte(double importe, Pedido pedido) throws InterruptedException {
		Exchanger<Double> canal = hash_espera_importe.get(pedido.getNumeroPedido());
		canal.exchange(importe);
		
	}
}

	
