package Principales;

public enum Catalogo {

	Camiseta(true, 9.99),
	Pantalon(true,  14.99),
	Abrigo(false, 39.99),
	Sudadera(true,  19.99),
	Zapatillas(false,  29.99);
	
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
		return precio;
	}
	
}
