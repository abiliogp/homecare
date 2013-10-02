package homecare;

public class Paciente {
	
	private String nome;
	private String genero;
	private String dataNasc;
	
	private Medico medico;
	
	/*
	 * get and set
	 */
	public Medico getMedico(){
		return this.medico;
	}

	public String getNome() {
		return nome;
	}

	public String getGenero() {
		return genero;
	}

	public String getDataNasc() {
		return dataNasc;
	}

	
}
