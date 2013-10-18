package homecare;

import java.io.Serializable;

public class Dado implements Serializable{  

	private String dado;
	private String tipo;
	
	public Dado (String tipo, String dado){
		this.dado = dado;
		this.tipo = tipo;
	}
	
	public String getDados() {
		return dado;
	}
	public void setDados(String dado) {
		this.dado = dado;
	}
	public String getUnidadeMedida() {
		return tipo;
	}
	public void setUnidadeMedida(String unidadeMedida) {
		this.tipo = unidadeMedida;
	}
	
	
}
