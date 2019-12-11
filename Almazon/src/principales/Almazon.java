package principales;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Semaphore;


public class Almazon {
	
	private int numPlayas;
	private int num_clientes;
	private int num_emp_coge_pedidos;
	private int num_emp_empaqueta_pedidos;
	private int num_emp_administrador;
	//Sirve para controlar al abrir y cerrar el almacen. Trabaja junto con el mutex de contador de gente.
	private int contador_total_gente=0;

	private final int NUM_PEDIDOS_MAXIMOS=2;
	
	//Cola bloqueante de pedidos. Máximo 1 por cada empleado administrador.
	BlockingQueue<Pedido> pedidos;
	
	//Cola bloqueante de pedidos para los empleados recoge pedidos. Maximo uno por cada coge_pedidos para que no se bloqueen al hacer el put. 
	//Esto vale para que se bloqueen los que elaboran pedidos si aun no tienen trabajo.
	BlockingQueue<Pedido> pedidos_a_elaborar;
	
	//Cola bloqueante para las playas.
	BlockingQueue<Playa> playas;
	
	//cola de paquetes pendientes de empaquetar.
	BlockingQueue<Pedido> pedidos_para_empaquetar;

	//Hash para que administrador le envie el importe de la compra al cliente.
	ConcurrentHashMap<Integer, Exchanger<Double>> hash_espera_importe =new ConcurrentHashMap<Integer, Exchanger<Double>>();
	
	//ConcurrentHashMap<Pedido, Exchanger<Paquete>> hash_pedido_listo= new ConcurrentHashMap<Pedido, Exchanger<Paquete>>();
	//Hash para los pedidos con errores, que se debe revisar con prioridad por el recogePedidos.
	ConcurrentHashMap<Pedido, Paquete> hash_pedidos_atencion= new ConcurrentHashMap<Pedido, Paquete>();
	
	//Hash para que el empaquetador envíe aviso al administrador de que el paquete ya esta listo.
	ConcurrentHashMap<Integer, Exchanger<Pedido>> hash_pedidos_para_enviar= new ConcurrentHashMap<Integer,Exchanger<Pedido>>();
	
	//Hash para que el administrador envíe aviso al cliente de que el paquete ya esta listo y enviado.
	ConcurrentHashMap<Integer, Exchanger<Pedido>> hash_pedidos_para_cliente= new ConcurrentHashMap<Integer,Exchanger<Pedido>>();
	
	//Cerrojos y booleanos para el control de cambio de turno.
	Semaphore cerrojo_fin_turno1,cerrojo_fin_turno2;
	boolean fin_turno1, fin_jornada;
	
	//El encargado espera a que todos se hayan ido.
	Semaphore encargado= new Semaphore(0);
	
	//contador de la apertura y cierre del almacén.
	CountDownLatch apertura;
	
	Semaphore mutex_contador_gente_dentro= new Semaphore(1);
	
	
	//CONSTRUCTOR.
	public Almazon(int numPlayas, int numClientes, int numEmpRecogePedidos, int numEmpEmpaquetaPedidos, int numEmpAdministrativos) {
		
		this.numPlayas= numPlayas;
		playas  = new ArrayBlockingQueue<Playa>(this.numPlayas);
		for(int i=0;i<this.numPlayas;i++) {
			try {
				playas.put(new Playa(false));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		this.num_clientes= numClientes;
		this.num_emp_administrador= numEmpAdministrativos;
		this.num_emp_coge_pedidos=numEmpRecogePedidos;
		this.num_emp_empaqueta_pedidos=numEmpEmpaquetaPedidos;
		
		pedidos  = new ArrayBlockingQueue<Pedido> (num_emp_coge_pedidos);
		
		pedidos_a_elaborar = new ArrayBlockingQueue<Pedido> (num_emp_empaqueta_pedidos);
		
		pedidos_para_empaquetar= new ArrayBlockingQueue<Pedido>(num_emp_empaqueta_pedidos);
		
		apertura = new CountDownLatch(1);
		
		fin_turno1= false;
		fin_jornada= false;
		
		cerrojo_fin_turno1= new Semaphore (1);
		cerrojo_fin_turno2=new Semaphore (1);
		
	}
		
	//EmpleadoRecogePedidos
	public void empaquetarPedido(Pedido pedido) throws InterruptedException {
		pedidos_para_empaquetar.put(pedido);
	}
	
	//EmpleadoEmpaquetaPedidos
	public Pedido esperarPedidoParaEmpaquetar() throws InterruptedException {
		Pedido pedido=pedidos_para_empaquetar.take();
		System.out.println("Pedido para empaquetar");
		return pedido;
		
	}
	
	//EMPAQUETA PEDIDOS.
	public void pedidoErroneo(Pedido pedido, Paquete paquete) {
		hash_pedidos_atencion.put(pedido, paquete);
	}
		 
	 //ADMINISTRADOR.
	 public Pedido esperarPedido() throws InterruptedException{
		 
		 Pedido pedido=pedidos.take();
		 return pedido;
	 }

	 //RECOGE PEDIDOS.
	 public Pedido esperarPedidoParaRecoger() throws InterruptedException {
		 Pedido pedido = pedidos_a_elaborar.take();
		 return pedido;
	 }
	 
	 //EMPAQUETA PEDIDOS.
	 public Playa cogerPlaya() throws InterruptedException {
		 
		 Playa playa = playas.take();
		 playa.OcuparPlaya();

		 return playa;
	}
	 
	 //EMPAQUETA PEDIDOS.
	public void soltarPlaya(Playa playa) throws InterruptedException {
		
		playa.DesocuparPlaya();
		playas.put(playa);

	}
	 
	 //ADMINISTRADOR.
	public void tramitarPedido(Pedido pedido) throws InterruptedException {
		pedidos_a_elaborar.put(pedido);
	}
		
	//EMPAQUETA PEDIDOS.
	public void enviarNotificacionAdministrador(int NumPedido, Pedido pedido) throws InterruptedException {
		System.out.println("                                           Enviando notificacion al administrador");
		hash_pedidos_para_enviar.put(pedido.getNumeroPedido(), new Exchanger<Pedido>());

	}
	
	//ADMINISTRATIVOS.
	public void enviarNotificacion(Pedido pedido) throws InterruptedException {
		
		hash_pedidos_para_enviar.get(pedido.getNumeroPedido());
		System.out.println("                                      El pedido "+ pedido.getNumeroPedido()+" ya ha sido empaquetado y enviado");
		hash_pedidos_para_cliente.put(pedido.getNumeroPedido(), new Exchanger<Pedido>());
	}
	
	//CLIENTE.
	public Pedido esperarNotificacionAdministrador(Pedido pedido) throws InterruptedException {

		hash_pedidos_para_cliente.get(pedido.getNumeroPedido());
		System.out.println(                 "Pedido "+ pedido.getNumeroPedido()+" le ha sido enviado correctamente");
		return pedido;
		
	}
	
	//ADMINISTRADOR.
	public void darImporte(double importe, Pedido pedido) throws InterruptedException{
		Exchanger<Double> canal= hash_espera_importe.get(pedido.getNumeroPedido());
		canal.exchange(importe);
	}
	
	//CLIENTE.
	public double esperarImporte(principales.Pedido pedido) throws InterruptedException {
		Exchanger<Double> canal = hash_espera_importe.get(pedido.getNumeroPedido());
		
		Double importe=0.0;
		importe = canal.exchange(null);
		
		return importe;
	}
	
	//CLIENTE.
	public void hacerPedido(Pedido pedido) throws InterruptedException {
		
		hash_espera_importe.put(pedido.getNumeroPedido(), new Exchanger<Double>());
				
		//Mandamos el pedido para que nos atiendan
		pedidos.put(pedido);
		
	}

	//LIMPIADOR
	public void LimpiarPlaya() throws InterruptedException {
		
		//SE NOS ADELANTAN ANTES DE LIMPIAR (+2 PEDIDOS).

		boolean limpieza=false;
		
		for(Playa p: playas) {
			p.OcuparPlayaParaLimpiar(limpieza);
			if(p.getPedidosHechos()==NUM_PEDIDOS_MAXIMOS){
				limpieza=true;
				p.OcuparPlayaParaLimpiar(limpieza);
				Thread.sleep(500);
				soltarPlaya(p);
			}else {
				soltarPlaya(p);
			}
		}
	}

	//ENCARGADO.
	public void abrir() throws InterruptedException {
		
		System.out.println("Encargado abre el almacen");
		cerrojo_fin_turno1.acquire();
		fin_turno1 = false;
		cerrojo_fin_turno1.release();
		
		apertura.countDown();
	}

	//ENCARGADO.
	public void cambioTurno() throws InterruptedException {
		cerrojo_fin_turno1.acquire();
		fin_turno1 = true;
		cerrojo_fin_turno1.release();
		
		cerrojo_fin_turno2.acquire();
		fin_jornada = false;
		cerrojo_fin_turno2.release();		
	}

	//ENCARGADO.
	public void echarCiere() throws InterruptedException {
		apertura = new CountDownLatch(1);
		
		cerrojo_fin_turno2.acquire();
		fin_jornada = true;
		cerrojo_fin_turno2.release();
		
	}
	
	//ENCARGADO.
	public void cerrarAlmacen() throws InterruptedException {
		encargado.acquire();
			
		playas  = new ArrayBlockingQueue<Playa>(this.numPlayas);
		for(int i=0;i<this.numPlayas;i++) {
			playas.put(new Playa(false));
		}
				
		pedidos  = new ArrayBlockingQueue<Pedido> (num_emp_coge_pedidos);
		
		pedidos_a_elaborar = new ArrayBlockingQueue<Pedido> (num_emp_empaqueta_pedidos);
		
		pedidos_para_empaquetar= new ArrayBlockingQueue<Pedido>(num_emp_empaqueta_pedidos);
				
	}

	//ENCARGADO.
	public void supervisionAlmacen() {
		List<Integer> numPedidosCola= new ArrayList<Integer>();
		List<Integer> numPlayasLibres= new ArrayList<Integer>();
		
		for(int i=0;i<10;i++) {
			int pedidos_pendientes= getpedidosEnCola();
			numPedidosCola.add(pedidos_pendientes);
			int playas_libres= getPlayasLibre();
			numPlayasLibres.add(playas_libres);
			
			System.out.println(Thread.currentThread().getName()+": Supervisión: Pendientes en cola=" + pedidos_pendientes
					+ ", Playas Libres=" + playas_libres);
		}
	}

	//TODOS MENOS EL ENCARGADO.
	public void salir() {
		try {
			mutex_contador_gente_dentro.acquire();
			if(contador_total_gente>0) {
				contador_total_gente--;
				System.out.println(Thread.currentThread().getName()+": Saliendo del almacen. El número de personas dentro es: "+contador_total_gente);
			}else {							//en caso de que no haya gente dentro del almacen, se avisa al encargado para que pueda cerrar.
				encargado.release();
			}
			mutex_contador_gente_dentro.release();
		}catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}
	
	//TODOS MENOS EL CLIENTE Y EL ENCARGADO.
	public void entrarTrabajar() {
		
		System.out.println(Thread.currentThread().getName()+" : Esperando a que el encargado permita la entrada.");
		try {
			apertura.await();
			mutex_contador_gente_dentro.acquire();
			contador_total_gente++;
			mutex_contador_gente_dentro.release();
		}catch(InterruptedException ie) {
			ie.printStackTrace();
		}
		
	}
	
	//TODOS MENOS CLIENTE Y ENCARGADO.
	public boolean finDeJornada() throws InterruptedException {
		boolean auxiliarJornada;
		cerrojo_fin_turno2.acquire();
		auxiliarJornada= fin_jornada;
		cerrojo_fin_turno2.release();
		
		return auxiliarJornada;
		
	}
					/* GETS*/

	public int getpedidosEnCola() {
		return pedidos.size();
	}
	
	public int getPlayasLibre() {
		return playas.size();
	}



	
}
