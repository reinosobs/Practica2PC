package Principales;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Pedido {
	
	private List<Catalogo> productosPedido = new ArrayList<Catalogo>();
	public static int num=1;
	private int numPedido;
	
	public Pedido (){
		numPedido = num;
		num++;
	
	}

	public static Pedido crearPedidoRandom() {//Metodo para crear un pedido de manera random con distintos productos
		
		
		Random random = new Random();
		
		int numProductos = random.nextInt(10)+1;
		
		Pedido pedido = new Pedido();
		
		for(int i=0; i<numProductos; i++){
			int randomNumProduct = random.nextInt(Catalogo.values().length);
			Catalogo randomProduct = Catalogo.values()[randomNumProduct];
			pedido.productosPedido.add(randomProduct);
		}
		
		return pedido;
	}

	public double calcularImporte() {//Calculamos el precio total del pedido
		double importe = 0;
		for(Catalogo productoPedido : productosPedido){
			importe += productoPedido.getPrecio();
		}
		return importe;
	}

	public List<Catalogo> getProductosPedido() {//devolvemos el pedido
		return productosPedido;
	}

	public int getNumeroPedido() {
		return numPedido;		
	}
	
}
