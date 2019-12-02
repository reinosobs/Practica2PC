package Principales;

import java.util.ArrayList;
import java.util.List;

public class Paquete {
private List<Catalogo> productos = new ArrayList<Catalogo>();
	
	public void addProducto(Catalogo producto) {
		productos.add(producto);		
	}

	@Override
	public String toString() {
		return "Comida [productos=" + productos + "]";
	}	
}
