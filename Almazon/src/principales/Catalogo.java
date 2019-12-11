package principales;

/**
 * 
 * @author Belen
 *
 *	Catálogo de productos que se procesan en el almacén.
 *
 */

public enum Catalogo {

	Camiseta(true, 9.99),
	Pantalon(true,14.99),
	Abrigo(true, 39.99),
	Sudadera(true, 19.99),
	Zapatilla(true, 29.99);
	
	private volatile boolean enStock;
	private volatile double precio;
	
	private Catalogo(boolean enStock, double precio) {
		this.enStock=enStock;
		this.precio=precio;
	}
	
	public boolean hayStock() {
		return enStock;
	}
	
	public double getPrecio() {
		return this.precio;
	}
	
}
