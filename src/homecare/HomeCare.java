package homecare;

import java.util.ArrayList;

public abstract class HomeCare {

	protected Paciente paciente;
	
	protected ArrayList<Sensor> sensores = new ArrayList<Sensor>();
	protected ArrayList<Dado> dadosLidos = new ArrayList<Dado>();
	
	public HomeCare (Paciente paciente){
		
	}
	
	public ArrayList<Dado> getDados(){
		lerSensores();
		return dadosLidos;
	}
	
	protected void lerSensores(){
		for(int i = 0; i < sensores.size(); i++){
			dadosLidos.add(sensores.get(i).getDado());	
		}
	}
	
	/*
	 * gerencia os sensores add
	 * ler um arquivo de configuração dos sensores
	 * a serem utilizados
	 */
	private void addSensores(){
		Sensor temp = new Temperatura();
		sensores.add(temp);
	}
	
	

}
