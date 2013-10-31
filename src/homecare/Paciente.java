package homecare;

public class Paciente {
	
	private String nome;
	private String genero;
	private String dataNasc;
	private String cpf;
	private String idade;
	
	public Paciente(String nome, String genero, String dataNasc,  String cpf, String idade){
		this.nome = nome;
		this.genero = genero;
		this.dataNasc = dataNasc;
		this.cpf = cpf;
		this.idade = idade;
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

	public String getIdade() {
		return idade;
	}
	
	public String getDataNasc() {
		return dataNasc;
	}

	public String getCpf(){
		return cpf;
	}
	
}
