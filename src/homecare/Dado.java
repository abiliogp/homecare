package homecare;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

public class Dado implements Serializable{  

	private String dado;
	private String tipo;
	private String time;
	
	public Dado (String tipo, String dado){
		this.dado = dado;
		this.tipo = tipo;
		this.time = (new Timestamp(new Date().getTime())).toString();
	}
	
	public Dado (String tipo, String dado, String time){
		this.tipo = tipo;
		this.dado = dado;		
		this.time = time;
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
