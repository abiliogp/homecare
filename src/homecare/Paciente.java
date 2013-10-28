package homecare;

public class Paciente {
	
	private String nome;
	private String genero;
	private String dataNasc;
	private String cpf;
	
	public Paciente(String nome, String genero, String dataNasc, String cpf){
		this.nome = nome;
		this.genero = genero;
		this.dataNasc = dataNasc;
		this.cpf = cpf;
	}
	
	
	/*
	 * get and set
	 */
	public String getNome() {
		return nome;
	}

	public String getGenero() {
		return genero;
	}

	public String getDataNasc() {
		return dataNasc;
	}

	public String getCpf(){
		return cpf;
	}
	
}
