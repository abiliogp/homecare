package homecare;

import java.util.ArrayList;
import java.util.Random;

public class HomeCare {

	private Paciente paciente;
	
	private ArrayList<Sensor> sensores = new ArrayList<Sensor>();
	private ArrayList<Dado> dadosLidos = new ArrayList<Dado>();
	
	public HomeCare (){
		paciente = new Paciente(" Johnny Cash", "male", "26/02/1932", "111.222.333-00","71");
	}
	
	public String getCpf(){
		return paciente.getCpf();
	}
	
	public ArrayList<Dado> getDados(){
		if(!dadosLidos.isEmpty()){
			dadosLidos.clear();
		}
		lerSensores();
		return dadosLidos;
	}
	
	private void lerSensores(){
		Random rad = new Random();
		for(int i = 0; i < 20; i++){
			dadosLidos.add(new Dado(35.5 + rad.nextDouble() * 2,"temp"));
			dadosLidos.add(new Dado(13.45 +  rad.nextDouble() * 2,"press"));
			dadosLidos.add(new Dado(5.1 + rad.nextDouble() * 3,"presd"));
			dadosLidos.add(new Dado(66.75 + rad.nextDouble() * 10,"card"));
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
