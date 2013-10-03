package homecare;

public class Dado {

	private double dado;
	private String unidadeMedida;
	
	public Dado (double dado, String unidadeMedida){
		this.dado = dado;
		this.unidadeMedida = unidadeMedida;
	}
	
	public double getDados() {
		return dado;
	}
	public void setDados(double dado) {
		this.dado = dado;
	}
	public String getUnidadeMedida() {
		return unidadeMedida;
	}
	public void setUnidadeMedida(String unidadeMedida) {
		this.unidadeMedida = unidadeMedida;
	}
	
	
}
