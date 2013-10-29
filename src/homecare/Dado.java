package homecare;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

public class Dado implements Serializable{  

	private String valor;
	private String tipo;
	private String time;
	
	public Dado (String valor, String tipo){
		this.valor = valor;
		this.tipo = tipo;
		this.time = (new Timestamp(new Date().getTime())).toString();
	}
	
	public Dado (String valor, String tipo, String time){
		this.valor = valor;
		this.tipo = tipo;
		this.time = time;
	}
	
	public String getValor() {
		return valor;
	}
	
	public String getTipo() {
		return tipo;
	}
	
	public String getTime() {
		return time;
	}
	
	public void setDados(String dado) {
		this.valor = dado;
	}
	public String getUnidadeMedida() {
		return tipo;
	}
	public void setUnidadeMedida(String unidadeMedida) {
		this.tipo = unidadeMedida;
	}
	
	
}
