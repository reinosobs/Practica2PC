package Principales;

public class Producto {
	
	private Catalogo productos;

	public Producto(Catalogo productos) {
		this.productos=productos;
	}
	
	public String toString() {
		return "( "+productos+" )";
	}
}
