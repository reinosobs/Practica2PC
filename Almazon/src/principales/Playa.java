package principales;

public class Playa {

	private boolean ocupada; //boolean limpiada.
	private int pedidos_hechos;
	
	public Playa(boolean ocupado) {
		this.ocupada=ocupado;
		this.pedidos_hechos=0;
	}
	
	public void OcuparPlaya() {
		ocupada=true;
		this.pedidos_hechos++;
	}
	
	public void DesocuparPlaya() {
		ocupada=false;
	}
	
	public boolean estaOcupada() {
		return this.ocupada;
	}

	public int getPedidosHechos() {
		return this.pedidos_hechos;
	}
	
	public void setPedidosHechos(int n) {
		this.pedidos_hechos=n;
	}
	
	public String toString() {
		return "Playa";
	}

	public void OcuparPlayaParaLimpiar(boolean lim) {
		//this.ocupada=true;
		
		if(lim) {
			this.pedidos_hechos=0;
		}else {
			this.ocupada=true;
		}
		
	}
	
	
}
