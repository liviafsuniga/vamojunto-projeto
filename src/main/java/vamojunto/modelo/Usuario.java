package vamojunto.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import vamojunto.util.Formatador;
import vamojunto.util.Seguranca;
import vamojunto.util.Validador;

/**
 * Usuario da plataforma. Toda conta e passageira; se tiver CNH vira Motorista
 * (especializacao - veja a classe Motorista, que herda desta).
 *
 * POO: encapsulamento, construtores sobrecarregados, associacao (agregacao)
 * com Instituicao, metodos de negocio autenticar() e solicitarCarona().
 */
public class Usuario {

    /** Regra de negocio: somente maiores de 18 anos (igual a trg_usuario_bi). */
    public static final int IDADE_MINIMA = 18;

    private int codigo;
    private Instituicao instituicao;      // pode ser null (vinculo opcional)
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private String senhaHash;
    private LocalDate dataNascimento;
    private boolean verificado;
    private LocalDateTime dataVerificacao;
    private LocalDateTime dataCadastro;
    private boolean ativo = true;

    /** Construtor vazio: usado pelo DAO ao montar o objeto a partir do banco. */
    public Usuario() {
    }

    /** Construtor para um novo cadastro (sem instituicao). */
    public Usuario(String nome, String cpf, String email, String telefone, LocalDate dataNascimento) {
        setNome(nome);
        setCpf(cpf);
        setEmail(email);
        setTelefone(telefone);
        setDataNascimento(dataNascimento);
    }

    /** Sobrecarga: novo cadastro ja vinculado a uma instituicao. */
    public Usuario(String nome, String cpf, String email, String telefone, LocalDate dataNascimento,
                   Instituicao instituicao) {
        this(nome, cpf, email, telefone, dataNascimento);
        this.instituicao = instituicao;
    }

    /** Construtor de copia: usado pela subclasse Motorista para "promover" um usuario. */
    protected Usuario(Usuario outro) {
        this.codigo = outro.codigo;
        this.instituicao = outro.instituicao;
        this.nome = outro.nome;
        this.cpf = outro.cpf;
        this.email = outro.email;
        this.telefone = outro.telefone;
        this.senhaHash = outro.senhaHash;
        this.dataNascimento = outro.dataNascimento;
        this.verificado = outro.verificado;
        this.dataVerificacao = outro.dataVerificacao;
        this.dataCadastro = outro.dataCadastro;
        this.ativo = outro.ativo;
    }

    // ------------------------------------------------------------------
    // Metodos de negocio
    // ------------------------------------------------------------------

    /** Guarda apenas o hash da senha (requisito nao funcional de seguranca). */
    public void definirSenha(String senha) {
        Validador.exigir(Seguranca.senhaForte(senha),
                "A senha deve ter pelo menos " + Seguranca.TAMANHO_MINIMO_SENHA + " caracteres.");
        this.senhaHash = Seguranca.gerarHash(senha);
    }

    /** Confere a senha digitada com o hash guardado. Usuario inativo nao entra. */
    public boolean autenticar(String senha) {
        return ativo && senhaHash != null && senha != null
                && senhaHash.equalsIgnoreCase(Seguranca.gerarHash(senha));
    }

    public int getIdade() {
        return dataNascimento == null ? 0 : Period.between(dataNascimento, LocalDate.now()).getYears();
    }

    /** RF02: so usuario verificado e ativo pode ser motorista (igual a trg_motorista_bi). */
    public boolean podeSerMotorista() {
        return verificado && ativo;
    }

    /** Polimorfismo: Motorista sobrescreve para devolver true. */
    public boolean isMotorista() {
        return false;
    }

    /** Polimorfismo: Motorista sobrescreve. */
    public String getPerfil() {
        return "PASSAGEIRO";
    }

    /**
     * RF07: cria um pedido de carona. As regras basicas sao conferidas aqui
     * (o banco confere de novo nas triggers).
     */
    public Solicitacao solicitarCarona(Viagem viagem, Ponto embarque, BigDecimal trechoKm) {
        Validador.exigir(ativo, "Usuario inativo nao pode solicitar carona.");
        Validador.exigir(viagem.getStatus() == StatusViagem.AGENDADA,
                "So e possivel pedir carona em viagem agendada.");
        Validador.exigir(viagem.getMotorista() == null || viagem.getMotorista().getCodigo() != codigo,
                "Voce e o motorista desta viagem.");
        Validador.exigir(embarque.getTipo() != TipoPonto.DESTINO, "Nao e possivel embarcar no DESTINO.");
        Validador.exigir(viagem.getVagasRestantes() > 0, "Esta viagem nao tem mais vagas.");
        Validador.exigir(trechoKm != null && trechoKm.signum() > 0
                        && trechoKm.compareTo(viagem.getDistanciaKm()) <= 0,
                "O trecho deve ser maior que zero e no maximo a distancia da viagem.");
        return new Solicitacao(viagem, this, embarque, trechoKm);
    }

    public String getCpfMascarado() {
        return Formatador.mascararCpf(cpf);
    }

    // ------------------------------------------------------------------
    // Getters e setters (encapsulamento com validacao)
    // ------------------------------------------------------------------

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public Instituicao getInstituicao() {
        return instituicao;
    }

    public void setInstituicao(Instituicao instituicao) {
        this.instituicao = instituicao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        Validador.exigir(!Validador.vazio(nome) && nome.trim().length() >= 3,
                "Informe o nome completo (minimo 3 letras).");
        this.nome = nome.trim();
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        String digitos = Validador.apenasDigitos(cpf);
        Validador.exigir(Validador.cpfValido(digitos), "CPF deve ter 11 digitos.");
        this.cpf = digitos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        Validador.exigir(Validador.emailValido(email == null ? null : email.trim()),
                "E-mail invalido. Exemplo: nome@dominio.com");
        this.email = email.trim().toLowerCase();
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        String digitos = Validador.apenasDigitos(telefone);
        Validador.exigir(Validador.telefoneValido(digitos), "Telefone deve ter DDD + numero (10 ou 11 digitos).");
        this.telefone = digitos;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    /** Usado pelo DAO (o valor ja vem como hash do banco). */
    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        Validador.exigir(dataNascimento != null, "Data de nascimento e obrigatoria.");
        Validador.exigir(Period.between(dataNascimento, LocalDate.now()).getYears() >= IDADE_MINIMA,
                "E preciso ter pelo menos " + IDADE_MINIMA + " anos para usar o VamoJunto.");
        this.dataNascimento = dataNascimento;
    }

    /** Usado pelo DAO: dado que ja esta no banco, sem revalidar a idade. */
    public void carregarDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public boolean isVerificado() {
        return verificado;
    }

    public void setVerificado(boolean verificado) {
        this.verificado = verificado;
    }

    public LocalDateTime getDataVerificacao() {
        return dataVerificacao;
    }

    public void setDataVerificacao(LocalDateTime dataVerificacao) {
        this.dataVerificacao = dataVerificacao;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public String toString() {
        return nome + " <" + email + "> - " + getPerfil();
    }
}
