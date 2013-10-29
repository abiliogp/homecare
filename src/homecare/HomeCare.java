package homecare;

import java.util.ArrayList;

public class HomeCare {

	private Paciente paciente;
	
	private ArrayList<Sensor> sensores = new ArrayList<Sensor>();
	private ArrayList<Dado> dadosLidos = new ArrayList<Dado>();
	
	public HomeCare (){
		paciente = new Paciente("Joao Pedro", "masc", "10/01/1991", "254.122.321-88");
	}
	
	public String getCpf(){
		return paciente.getCpf();
	}
	
	public ArrayList<Dado> getDados(){
		lerSensores();
		return dadosLidos;
	}
	
	private void lerSensores(){
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
