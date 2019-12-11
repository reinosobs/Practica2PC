package principales;

import java.util.ArrayList;
import java.util.List;

public class Paquete {

	private List<Producto> productos= new ArrayList<Producto>();	
	
	public void addProducto(Producto producto) {
		productos.add(producto);
	}
	
	public String toString() {
		return "Comida [productos=" +productos+"]";
	}
	
}
