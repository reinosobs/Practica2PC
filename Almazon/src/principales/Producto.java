  package principales;

/**
 * 
 * @author Belen
 *
 *	Listado de productos del Enum correspondiente al pedido.
 *
 */

public class Producto {

	private Catalogo productos;
	
	public Producto(Catalogo productos) {
		this.productos= productos;
	}
	public String toString() {
		return "("+productos+")";
	}
	
}
