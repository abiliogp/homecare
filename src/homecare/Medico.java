package homecare;

import java.util.ArrayList;

public class Medico {

	private String nome;
	private String crm;
	
	private ArrayList<Paciente> pacientes;
	
	/*
	 * get and set
	 */
	public String getNome() {
		return nome;
	}
	
	public String getCrm() {
		return crm;
	}
	
	public ArrayList<Paciente> getListaPacientes(){
		return this.pacientes;
	}

	
}
