-- =====================================================================
--  VamoJunto — Banco de dados (modelo físico) · versão 2
--  UPX II · Análise e Desenvolvimento de Sistemas · Equipe Vision Hexa
--  SGBD alvo: MySQL 8.0+ (também testado em MariaDB 10.11)
--  9 tabelas · 10 relacionamentos · 39 CHECK · 11 triggers
--  11 functions · 21 procedures · 5 views · 2 usuários de banco
--
--  Versão 2: senhas de exemplo com hash SHA-256 real (senha: vamo123)
--  e objetos novos para o console Java (seção EXTENSÕES).
--
--  Como executar: MySQL Workbench > File > Open SQL Script > executar
--  tudo (raio)  ou  terminal: mysql -u root -p < vamojunto.sql
-- =====================================================================
 
DROP DATABASE IF EXISTS vamojunto;
CREATE DATABASE vamojunto
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
USE vamojunto;
 
-- ---------------------------------------------------------------------
-- 1. INSTITUICAO
-- ---------------------------------------------------------------------
CREATE TABLE Instituicao (
    cod_instituicao INT          NOT NULL AUTO_INCREMENT,
    nome            VARCHAR(150) NOT NULL,
    sigla           VARCHAR(20)  NULL,
    cidade          VARCHAR(100) NOT NULL,
    estado          CHAR(2)      NOT NULL,
    PRIMARY KEY (cod_instituicao),
    CONSTRAINT uq_instituicao_nome_cidade UNIQUE (nome, cidade),
    CONSTRAINT ck_instituicao_uf CHECK (estado IN ('AC','AL','AP','AM','BA','CE','DF','ES','GO','MA','MT','MS','MG','PA','PB','PR','PE','PI','RJ','RN','RS','RO','RR','SC','SP','SE','TO'))
) ENGINE=InnoDB;
 
-- ---------------------------------------------------------------------
-- 2. USUARIO   (Instituicao 0..1 —vincula— N Usuario)
-- ---------------------------------------------------------------------
CREATE TABLE Usuario (
    cod_usuario     INT          NOT NULL AUTO_INCREMENT,
    cod_instituicao INT          NULL,
    nome            VARCHAR(150) NOT NULL,
    cpf             CHAR(11)     NOT NULL,
    email           VARCHAR(255) NOT NULL,
    telefone        VARCHAR(11)  NOT NULL,
    senha_hash      VARCHAR(255) NOT NULL,
    dt_nascimento   DATE         NOT NULL,
    verificado      BOOLEAN      NOT NULL DEFAULT 0,
    dt_verificacao  DATETIME     NULL,
    dt_cadastro     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ativo           BOOLEAN      NOT NULL DEFAULT 1,
    PRIMARY KEY (cod_usuario),
    CONSTRAINT uq_usuario_cpf   UNIQUE (cpf),
    CONSTRAINT uq_usuario_email UNIQUE (email),
    CONSTRAINT fk_usuario_instituicao FOREIGN KEY (cod_instituicao)
        REFERENCES Instituicao (cod_instituicao)
        ON UPDATE RESTRICT ON DELETE SET NULL,
    CONSTRAINT ck_usuario_cpf      CHECK (cpf REGEXP '^[0-9]{11}$'),
    CONSTRAINT ck_usuario_email    CHECK (email LIKE '%_@_%._%' AND email NOT LIKE '% %'),
    CONSTRAINT ck_usuario_telefone CHECK (telefone REGEXP '^[0-9]{10,11}$'),
    CONSTRAINT ck_usuario_verificado CHECK (verificado IN (0,1)),
    CONSTRAINT ck_usuario_ativo    CHECK (ativo IN (0,1)),
    CONSTRAINT ck_usuario_dt_verif CHECK (
        (verificado = 0 AND dt_verificacao IS NULL) OR
        (verificado = 1 AND dt_verificacao IS NOT NULL))
) ENGINE=InnoDB;
 
-- ---------------------------------------------------------------------
-- 3. MOTORISTA   (Usuario 1 —é um— 0..1 Motorista · especialização)
-- ---------------------------------------------------------------------
CREATE TABLE Motorista (
    cod_motorista INT        NOT NULL,
    cnh           CHAR(11)   NOT NULL,
    categoria_cnh VARCHAR(2) NOT NULL,
    validade_cnh  DATE       NOT NULL,
    ativo         BOOLEAN    NOT NULL DEFAULT 1,
    PRIMARY KEY (cod_motorista),
    CONSTRAINT uq_motorista_cnh UNIQUE (cnh),
    CONSTRAINT fk_motorista_usuario FOREIGN KEY (cod_motorista)
        REFERENCES Usuario (cod_usuario)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT ck_motorista_cnh       CHECK (cnh REGEXP '^[0-9]{11}$'),
    CONSTRAINT ck_motorista_categoria CHECK (categoria_cnh IN ('B','C','D','E','AB','AC','AD','AE')),
    CONSTRAINT ck_motorista_ativo     CHECK (ativo IN (0,1))
) ENGINE=InnoDB;
 
-- ---------------------------------------------------------------------
-- 4. VEICULO
-- ---------------------------------------------------------------------
CREATE TABLE Veiculo (
    cod_veiculo INT         NOT NULL AUTO_INCREMENT,
    marca       VARCHAR(50) NOT NULL,
    modelo      VARCHAR(50) NOT NULL,
    cor         VARCHAR(30) NOT NULL,
    placa       CHAR(7)     NOT NULL,
    ano         SMALLINT    NOT NULL,
    renavam     CHAR(11)    NOT NULL,
    qtd_lugares TINYINT     NOT NULL,
    PRIMARY KEY (cod_veiculo),
    CONSTRAINT uq_veiculo_placa   UNIQUE (placa),
    CONSTRAINT uq_veiculo_renavam UNIQUE (renavam),
    -- aceita placa antiga (ABC1234) e Mercosul (ABC1D23)
    CONSTRAINT ck_veiculo_placa   CHECK (placa REGEXP '^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$'),
    CONSTRAINT ck_veiculo_renavam CHECK (renavam REGEXP '^[0-9]{11}$'),
    CONSTRAINT ck_veiculo_ano     CHECK (ano BETWEEN 1950 AND 2100),
    CONSTRAINT ck_veiculo_lugares CHECK (qtd_lugares BETWEEN 2 AND 9)
) ENGINE=InnoDB;
 
-- ---------------------------------------------------------------------
-- 5. COMBUSTIVEL
-- ---------------------------------------------------------------------
CREATE TABLE Combustivel (
    cod_combustivel INT          NOT NULL AUTO_INCREMENT,
    tipo            VARCHAR(20)  NOT NULL,
    preco_litro     DECIMAL(6,3) NOT NULL,
    dt_atualizacao  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                 ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (cod_combustivel),
    CONSTRAINT uq_combustivel_tipo UNIQUE (tipo),
    CONSTRAINT ck_combustivel_tipo  CHECK (tipo IN ('GASOLINA','ETANOL','DIESEL','GNV')),
    CONSTRAINT ck_combustivel_preco CHECK (preco_litro > 0)
) ENGINE=InnoDB;
 
-- ---------------------------------------------------------------------
-- 6. VIAGEM  (Motorista 1 —realiza— N · Veiculo 1 —usa— N ·
--             Combustivel 1 —abastece— N)
-- ---------------------------------------------------------------------
CREATE TABLE Viagem (
    cod_viagem            INT           NOT NULL AUTO_INCREMENT,
    cod_motorista         INT           NOT NULL,
    cod_veiculo           INT           NOT NULL,
    cod_combustivel       INT           NOT NULL,
    dt_inicio             DATETIME      NOT NULL,
    dt_fim                DATETIME      NULL,
    status                VARCHAR(15)   NOT NULL DEFAULT 'AGENDADA',
    vagas                 TINYINT       NOT NULL,
    distancia_km          DECIMAL(7,2)  NOT NULL,
    consumo_aplicado_km_l DECIMAL(5,2)  NOT NULL,
    preco_litro_aplicado  DECIMAL(6,3)  NULL,      -- preenchido pela trigger
    custo_total           DECIMAL(10,2) NULL,      -- calculado pela trigger
    qtd_participantes     TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (cod_viagem),
    CONSTRAINT fk_viagem_motorista FOREIGN KEY (cod_motorista)
        REFERENCES Motorista (cod_motorista)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_viagem_veiculo FOREIGN KEY (cod_veiculo)
        REFERENCES Veiculo (cod_veiculo)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_viagem_combustivel FOREIGN KEY (cod_combustivel)
        REFERENCES Combustivel (cod_combustivel)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT ck_viagem_status    CHECK (status IN ('AGENDADA','EM_ANDAMENTO','CONCLUIDA','CANCELADA')),
    CONSTRAINT ck_viagem_datas     CHECK (dt_fim IS NULL OR dt_fim > dt_inicio),
    CONSTRAINT ck_viagem_concluida CHECK (status <> 'CONCLUIDA' OR dt_fim IS NOT NULL),
    CONSTRAINT ck_viagem_vagas     CHECK (vagas >= 1),
    CONSTRAINT ck_viagem_distancia CHECK (distancia_km > 0),
    CONSTRAINT ck_viagem_consumo   CHECK (consumo_aplicado_km_l > 0),
    CONSTRAINT ck_viagem_preco     CHECK (preco_litro_aplicado IS NULL OR preco_litro_aplicado > 0),
    CONSTRAINT ck_viagem_custo     CHECK (custo_total IS NULL OR custo_total >= 0),
    CONSTRAINT ck_viagem_particip  CHECK (qtd_participantes BETWEEN 0 AND vagas)
) ENGINE=InnoDB;
 
-- ---------------------------------------------------------------------
-- 7. PONTO  (Viagem 1 —possui— N Ponto)  rota = ORIGEM, PARADA..., DESTINO
-- ---------------------------------------------------------------------
CREATE TABLE Ponto (
    cod_ponto   INT          NOT NULL AUTO_INCREMENT,
    cod_viagem  INT          NOT NULL,
    ordem       TINYINT      NOT NULL,
    tipo        VARCHAR(10)  NOT NULL,
    logradouro  VARCHAR(150) NOT NULL,
    numero      VARCHAR(10)  NULL,
    bairro      VARCHAR(100) NULL,
    cep         CHAR(8)      NULL,
    cidade      VARCHAR(100) NOT NULL,
    estado      CHAR(2)      NOT NULL,
    latitude    DECIMAL(9,6) NULL,
    longitude   DECIMAL(9,6) NULL,
    PRIMARY KEY (cod_ponto),
    CONSTRAINT uq_ponto_viagem_ordem UNIQUE (cod_viagem, ordem),
    -- chave alternativa usada pela FK composta de Solicitacao
    CONSTRAINT uq_ponto_ponto_viagem UNIQUE (cod_ponto, cod_viagem),
    CONSTRAINT fk_ponto_viagem FOREIGN KEY (cod_viagem)
        REFERENCES Viagem (cod_viagem)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT ck_ponto_tipo   CHECK (tipo IN ('ORIGEM','PARADA','DESTINO')),
    CONSTRAINT ck_ponto_ordem  CHECK ((tipo = 'ORIGEM' AND ordem = 1) OR (tipo <> 'ORIGEM' AND ordem > 1)),
    CONSTRAINT ck_ponto_cep    CHECK (cep IS NULL OR cep REGEXP '^[0-9]{8}$'),
    CONSTRAINT ck_ponto_uf     CHECK (estado IN ('AC','AL','AP','AM','BA','CE','DF','ES','GO','MA','MT','MS','MG','PA','PB','PR','PE','PI','RJ','RN','RS','RO','RR','SC','SP','SE','TO')),
    CONSTRAINT ck_ponto_lat    CHECK (latitude  IS NULL OR latitude  BETWEEN -90  AND 90),
    CONSTRAINT ck_ponto_lng    CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180)
) ENGINE=InnoDB;
 
-- ---------------------------------------------------------------------
-- 8. SOLICITACAO  (Usuario 1 —solicita— N · Viagem 1 —participa— N ·
--                  Ponto 1 —embarca em— N)
-- ---------------------------------------------------------------------
CREATE TABLE Solicitacao (
    cod_solicitacao     INT           NOT NULL AUTO_INCREMENT,
    cod_viagem          INT           NOT NULL,
    cod_passageiro      INT           NOT NULL,
    cod_ponto           INT           NOT NULL,
    dt_solicitacao      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    dt_resposta         DATETIME      NULL,
    status              VARCHAR(10)   NOT NULL DEFAULT 'PENDENTE',
    distancia_trecho_km DECIMAL(7,2)  NOT NULL,
    valor_rateio        DECIMAL(10,2) NULL,
    PRIMARY KEY (cod_solicitacao),
    CONSTRAINT uq_solicitacao_viagem_passageiro UNIQUE (cod_viagem, cod_passageiro),
    CONSTRAINT fk_solicitacao_viagem FOREIGN KEY (cod_viagem)
        REFERENCES Viagem (cod_viagem)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_solicitacao_passageiro FOREIGN KEY (cod_passageiro)
        REFERENCES Usuario (cod_usuario)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    -- FK composta: o ponto de embarque precisa ser DA MESMA viagem
    CONSTRAINT fk_solicitacao_ponto FOREIGN KEY (cod_ponto, cod_viagem)
        REFERENCES Ponto (cod_ponto, cod_viagem)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT ck_solicitacao_status   CHECK (status IN ('PENDENTE','ACEITA','RECUSADA','CANCELADA')),
    CONSTRAINT ck_solicitacao_resposta CHECK (
        (status = 'PENDENTE'  AND dt_resposta IS NULL) OR
        (status <> 'PENDENTE' AND dt_resposta IS NOT NULL)),
    CONSTRAINT ck_solicitacao_datas    CHECK (dt_resposta IS NULL OR dt_resposta >= dt_solicitacao),
    CONSTRAINT ck_solicitacao_trecho   CHECK (distancia_trecho_km > 0),
    CONSTRAINT ck_solicitacao_rateio   CHECK (valor_rateio IS NULL OR valor_rateio >= 0),
    CONSTRAINT ck_solicitacao_rateio_aceita CHECK (status = 'ACEITA' OR valor_rateio IS NULL)
) ENGINE=InnoDB;
 
-- ---------------------------------------------------------------------
-- 9. AVALIACAO  (Solicitacao 1 —recebe— N Avaliacao)
--    autor = quem avalia. Cada lado avalia uma única vez por solicitação.
-- ---------------------------------------------------------------------
CREATE TABLE Avaliacao (
    cod_avaliacao   INT          NOT NULL AUTO_INCREMENT,
    cod_solicitacao INT          NOT NULL,
    autor           VARCHAR(10)  NOT NULL,
    nota            TINYINT      NOT NULL,
    comentario      VARCHAR(500) NULL,
    dt_avaliacao    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (cod_avaliacao),
    CONSTRAINT uq_avaliacao_solicitacao_autor UNIQUE (cod_solicitacao, autor),
    CONSTRAINT fk_avaliacao_solicitacao FOREIGN KEY (cod_solicitacao)
        REFERENCES Solicitacao (cod_solicitacao)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT ck_avaliacao_autor CHECK (autor IN ('PASSAGEIRO','MOTORISTA')),
    CONSTRAINT ck_avaliacao_nota  CHECK (nota BETWEEN 1 AND 5)
) ENGINE=InnoDB;
 
-- Índices de apoio às consultas mais comuns
CREATE INDEX ix_viagem_status_inicio ON Viagem (status, dt_inicio);
CREATE INDEX ix_solicitacao_status   ON Solicitacao (cod_viagem, status);
 
-- =====================================================================
-- FUNCTIONS (Aula 11) — cálculos reaproveitados pelas triggers, views e
-- procedures de consulta. Podem ser chamadas direto: SELECT fn_xxx(...);
-- =====================================================================
DELIMITER $$
 
-- Custo de combustível da viagem: distância ÷ consumo × preço do litro
CREATE FUNCTION fn_calcular_custo(p_distancia DECIMAL(7,2), p_consumo DECIMAL(5,2),
                                  p_preco DECIMAL(6,3))
RETURNS DECIMAL(10,2)
DETERMINISTIC NO SQL
BEGIN
    IF p_distancia IS NULL OR p_consumo IS NULL OR p_consumo <= 0 OR p_preco IS NULL THEN
        RETURN NULL;
    END IF;
    RETURN ROUND(p_distancia / p_consumo * p_preco, 2);
END$$
 
-- Parte de um passageiro: custo do trecho dividido por todos no carro
CREATE FUNCTION fn_calcular_rateio(p_custo DECIMAL(10,2), p_trecho DECIMAL(7,2),
                                   p_distancia DECIMAL(7,2), p_participantes INT)
RETURNS DECIMAL(10,2)
DETERMINISTIC NO SQL
BEGIN
    IF p_custo IS NULL OR p_distancia IS NULL OR p_distancia <= 0 THEN
        RETURN NULL;
    END IF;
    RETURN ROUND(p_custo * (p_trecho / p_distancia) / (p_participantes + 1), 2);
END$$
 
-- Idade em anos completos na data de hoje
CREATE FUNCTION fn_idade(p_nascimento DATE)
RETURNS INT
NOT DETERMINISTIC NO SQL
BEGIN
    RETURN TIMESTAMPDIFF(YEAR, p_nascimento, CURDATE());
END$$
 
-- Esconde o CPF para exibição: 123.***.***-01
CREATE FUNCTION fn_mascarar_cpf(p_cpf CHAR(11))
RETURNS VARCHAR(14)
DETERMINISTIC NO SQL
BEGIN
    IF p_cpf IS NULL THEN
        RETURN NULL;
    END IF;
    RETURN CONCAT(LEFT(p_cpf, 3), '.***.***-', RIGHT(p_cpf, 2));
END$$
 
-- Valor em reais no padrão brasileiro: R$ 1.234,50
CREATE FUNCTION fn_formatar_moeda(p_valor DECIMAL(12,2))
RETURNS VARCHAR(25)
DETERMINISTIC NO SQL
BEGIN
    IF p_valor IS NULL THEN
        RETURN NULL;
    END IF;
    -- FORMAT gera 1,234.50 (padrão americano); trocamos vírgula e ponto.
    -- Não depende do locale pt_BR do servidor (que não agrupa milhares).
    RETURN CONCAT('R$ ', REPLACE(REPLACE(REPLACE(FORMAT(p_valor, 2), ',', '#'), '.', ','), '#', '.'));
END$$
 
-- Vagas que ainda podem ser ocupadas (0 se a viagem não está agendada)
CREATE FUNCTION fn_vagas_restantes(p_viagem INT)
RETURNS INT
READS SQL DATA
BEGIN
    DECLARE v_vagas  INT;
    DECLARE v_partic INT;
    DECLARE v_status VARCHAR(15);
    SET v_vagas  = (SELECT vagas             FROM Viagem WHERE cod_viagem = p_viagem);
    SET v_partic = (SELECT qtd_participantes FROM Viagem WHERE cod_viagem = p_viagem);
    SET v_status = (SELECT status            FROM Viagem WHERE cod_viagem = p_viagem);
    IF v_vagas IS NULL THEN
        RETURN NULL;
    END IF;
    IF v_status <> 'AGENDADA' THEN
        RETURN 0;
    END IF;
    RETURN v_vagas - v_partic;
END$$
 
-- Nota média que os passageiros deram ao motorista
CREATE FUNCTION fn_media_avaliacao_motorista(p_motorista INT)
RETURNS DECIMAL(3,2)
READS SQL DATA
BEGIN
    RETURN (SELECT ROUND(AVG(a.nota), 2)
              FROM Avaliacao a
              JOIN Solicitacao s ON s.cod_solicitacao = a.cod_solicitacao
              JOIN Viagem v      ON v.cod_viagem      = s.cod_viagem
             WHERE v.cod_motorista = p_motorista
               AND a.autor = 'PASSAGEIRO');
END$$
 
-- Quanto o passageiro já pagou em caronas concluídas
CREATE FUNCTION fn_total_gasto_passageiro(p_usuario INT)
RETURNS DECIMAL(10,2)
READS SQL DATA
BEGIN
    RETURN (SELECT IFNULL(SUM(s.valor_rateio), 0)
              FROM Solicitacao s
              JOIN Viagem v ON v.cod_viagem = s.cod_viagem
             WHERE s.cod_passageiro = p_usuario
               AND s.status = 'ACEITA'
               AND v.status = 'CONCLUIDA');
END$$
 
DELIMITER ;
 
-- =====================================================================
-- TRIGGERS — regras que CHECK não consegue garantir sozinho
-- =====================================================================
DELIMITER $$
 
-- Usuário precisa ter 18 anos ou mais
CREATE TRIGGER trg_usuario_bi BEFORE INSERT ON Usuario
FOR EACH ROW
BEGIN
    IF fn_idade(NEW.dt_nascimento) < 18 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Usuario deve ter pelo menos 18 anos.';
    END IF;
END$$
 
CREATE TRIGGER trg_usuario_bu BEFORE UPDATE ON Usuario
FOR EACH ROW
BEGIN
    IF fn_idade(NEW.dt_nascimento) < 18 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Usuario deve ter pelo menos 18 anos.';
    END IF;
END$$
 
-- Só usuário verificado e ativo pode virar motorista
CREATE TRIGGER trg_motorista_bi BEFORE INSERT ON Motorista
FOR EACH ROW
BEGIN
    DECLARE v_verificado TINYINT;
    DECLARE v_ativo      TINYINT;
    SELECT verificado, ativo INTO v_verificado, v_ativo
      FROM Usuario WHERE cod_usuario = NEW.cod_motorista;
    IF v_verificado = 0 OR v_ativo = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Somente usuario verificado e ativo pode ser motorista.';
    END IF;
END$$
 
-- Viagem: motorista apto, vagas cabem no carro, preço e custo calculados
CREATE TRIGGER trg_viagem_bi BEFORE INSERT ON Viagem
FOR EACH ROW
BEGIN
    DECLARE v_ativo    TINYINT;
    DECLARE v_validade DATE;
    DECLARE v_lugares  TINYINT;
    DECLARE v_preco    DECIMAL(6,3);
 
    SELECT ativo, validade_cnh INTO v_ativo, v_validade
      FROM Motorista WHERE cod_motorista = NEW.cod_motorista;
    IF v_ativo = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Motorista inativo nao pode criar viagem.';
    END IF;
    IF v_validade < DATE(NEW.dt_inicio) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'CNH do motorista vencida na data da viagem.';
    END IF;
 
    SELECT qtd_lugares INTO v_lugares FROM Veiculo WHERE cod_veiculo = NEW.cod_veiculo;
    IF NEW.vagas > v_lugares - 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Vagas maiores que os lugares do veiculo (descontando o motorista).';
    END IF;
 
    SET NEW.status = 'AGENDADA';
    SET NEW.qtd_participantes = 0;
    IF NEW.preco_litro_aplicado IS NULL THEN
        SELECT preco_litro INTO v_preco
          FROM Combustivel WHERE cod_combustivel = NEW.cod_combustivel;
        SET NEW.preco_litro_aplicado = v_preco;
    END IF;
    SET NEW.custo_total = fn_calcular_custo(NEW.distancia_km, NEW.consumo_aplicado_km_l, NEW.preco_litro_aplicado);
END$$
 
CREATE TRIGGER trg_viagem_bu BEFORE UPDATE ON Viagem
FOR EACH ROW
BEGIN
    DECLARE v_lugares TINYINT;
    DECLARE v_preco   DECIMAL(6,3);
 
    IF OLD.status IN ('CONCLUIDA','CANCELADA') AND NEW.status <> OLD.status THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Viagem concluida ou cancelada nao pode mudar de status.';
    END IF;
    IF OLD.status = 'EM_ANDAMENTO' AND NEW.status = 'AGENDADA' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Viagem em andamento nao pode voltar para agendada.';
    END IF;
 
    SELECT qtd_lugares INTO v_lugares FROM Veiculo WHERE cod_veiculo = NEW.cod_veiculo;
    IF NEW.vagas > v_lugares - 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Vagas maiores que os lugares do veiculo (descontando o motorista).';
    END IF;
 
    IF NEW.cod_combustivel <> OLD.cod_combustivel AND NEW.preco_litro_aplicado <=> OLD.preco_litro_aplicado THEN
        SELECT preco_litro INTO v_preco
          FROM Combustivel WHERE cod_combustivel = NEW.cod_combustivel;
        SET NEW.preco_litro_aplicado = v_preco;
    END IF;
    SET NEW.custo_total = fn_calcular_custo(NEW.distancia_km, NEW.consumo_aplicado_km_l, NEW.preco_litro_aplicado);
END$$
 
-- Sempre que custo ou participantes mudam, o rateio é recalculado.
-- Regra: cada passageiro paga a fração do custo do SEU trecho,
-- dividida entre todos no carro (passageiros aceitos + motorista).
CREATE TRIGGER trg_viagem_au AFTER UPDATE ON Viagem
FOR EACH ROW
BEGIN
    IF NOT (NEW.custo_total <=> OLD.custo_total)
       OR NEW.qtd_participantes <> OLD.qtd_participantes
       OR NEW.distancia_km <> OLD.distancia_km THEN
        UPDATE Solicitacao
           SET valor_rateio = fn_calcular_rateio(NEW.custo_total, distancia_trecho_km,
                                                 NEW.distancia_km, NEW.qtd_participantes)
         WHERE cod_viagem = NEW.cod_viagem
           AND status = 'ACEITA';
    END IF;
END$$
 
-- Ponto: um único DESTINO, sempre o último; rota só muda com viagem agendada
CREATE TRIGGER trg_ponto_bi BEFORE INSERT ON Ponto
FOR EACH ROW
BEGIN
    DECLARE v_status        VARCHAR(15);
    DECLARE v_ordem_destino TINYINT;
    DECLARE v_maior_ordem   TINYINT;
 
    SELECT status INTO v_status FROM Viagem WHERE cod_viagem = NEW.cod_viagem;
    IF v_status <> 'AGENDADA' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'So e possivel alterar a rota de uma viagem agendada.';
    END IF;
 
    SELECT MAX(ordem) INTO v_ordem_destino
      FROM Ponto WHERE cod_viagem = NEW.cod_viagem AND tipo = 'DESTINO';
    SELECT MAX(ordem) INTO v_maior_ordem
      FROM Ponto WHERE cod_viagem = NEW.cod_viagem;
 
    IF NEW.tipo = 'DESTINO' AND v_ordem_destino IS NOT NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'A viagem ja possui um ponto de DESTINO.';
    END IF;
    IF NEW.tipo = 'DESTINO' AND v_maior_ordem IS NOT NULL AND NEW.ordem < v_maior_ordem THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'O DESTINO precisa ser o ultimo ponto da rota.';
    END IF;
    IF NEW.tipo <> 'DESTINO' AND v_ordem_destino IS NOT NULL AND NEW.ordem > v_ordem_destino THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Nao pode haver ponto depois do DESTINO.';
    END IF;
END$$
 
CREATE TRIGGER trg_ponto_bu BEFORE UPDATE ON Ponto
FOR EACH ROW
BEGIN
    DECLARE v_status        VARCHAR(15);
    DECLARE v_ordem_destino TINYINT;
    DECLARE v_maior_outros  TINYINT;
 
    SELECT status INTO v_status FROM Viagem WHERE cod_viagem = NEW.cod_viagem;
    IF v_status <> 'AGENDADA' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'So e possivel alterar a rota de uma viagem agendada.';
    END IF;
    IF NEW.tipo <> OLD.tipo OR NEW.cod_viagem <> OLD.cod_viagem THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tipo e viagem do ponto nao podem ser alterados; exclua e recrie o ponto.';
    END IF;
 
    -- a ordem pode mudar (para encaixar paradas), mas o DESTINO continua o último
    IF NEW.ordem <> OLD.ordem THEN
        SELECT MAX(ordem) INTO v_maior_outros
          FROM Ponto WHERE cod_viagem = NEW.cod_viagem AND cod_ponto <> NEW.cod_ponto;
        SELECT MAX(ordem) INTO v_ordem_destino
          FROM Ponto WHERE cod_viagem = NEW.cod_viagem AND tipo = 'DESTINO'
                       AND cod_ponto <> NEW.cod_ponto;
        IF NEW.tipo = 'DESTINO' AND NEW.ordem < v_maior_outros THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'O DESTINO precisa ser o ultimo ponto da rota.';
        END IF;
        IF NEW.tipo <> 'DESTINO' AND v_ordem_destino IS NOT NULL AND NEW.ordem > v_ordem_destino THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Nao pode haver ponto depois do DESTINO.';
        END IF;
    END IF;
END$$
 
-- Solicitação: regras de quem pode pedir carona e onde embarcar
CREATE TRIGGER trg_solicitacao_bi BEFORE INSERT ON Solicitacao
FOR EACH ROW
BEGIN
    DECLARE v_motorista INT;
    DECLARE v_status    VARCHAR(15);
    DECLARE v_distancia DECIMAL(7,2);
    DECLARE v_tipo      VARCHAR(10);
    DECLARE v_ativo     TINYINT;
 
    SELECT cod_motorista, status, distancia_km INTO v_motorista, v_status, v_distancia
      FROM Viagem WHERE cod_viagem = NEW.cod_viagem;
    SELECT tipo INTO v_tipo FROM Ponto WHERE cod_ponto = NEW.cod_ponto;
    SELECT ativo INTO v_ativo FROM Usuario WHERE cod_usuario = NEW.cod_passageiro;
 
    IF NEW.cod_passageiro = v_motorista THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'O motorista nao pode solicitar carona na propria viagem.';
    END IF;
    IF v_ativo = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Usuario inativo nao pode solicitar carona.';
    END IF;
    IF v_status <> 'AGENDADA' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'So e possivel solicitar carona em viagem agendada.';
    END IF;
    IF v_tipo = 'DESTINO' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Nao e possivel embarcar no ponto de DESTINO.';
    END IF;
    IF NEW.distancia_trecho_km > v_distancia THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Distancia do trecho maior que a distancia da viagem.';
    END IF;
 
    SET NEW.status       = 'PENDENTE';
    SET NEW.dt_resposta  = NULL;
    SET NEW.valor_rateio = NULL;
END$$
 
-- Solicitação: transições de status válidas e controle de vagas
CREATE TRIGGER trg_solicitacao_bu BEFORE UPDATE ON Solicitacao
FOR EACH ROW
BEGIN
    DECLARE v_vagas   TINYINT;
    DECLARE v_partic  TINYINT;
    DECLARE v_status  VARCHAR(15);
 
    IF NEW.cod_viagem <> OLD.cod_viagem OR NEW.cod_passageiro <> OLD.cod_passageiro THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Viagem e passageiro da solicitacao nao podem ser alterados.';
    END IF;
 
    IF NEW.status <> OLD.status THEN
        IF NOT ( (OLD.status = 'PENDENTE' AND NEW.status IN ('ACEITA','RECUSADA','CANCELADA'))
              OR (OLD.status = 'ACEITA'   AND NEW.status = 'CANCELADA') ) THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Transicao de status da solicitacao invalida.';
        END IF;
 
        SELECT vagas, qtd_participantes, status INTO v_vagas, v_partic, v_status
          FROM Viagem WHERE cod_viagem = NEW.cod_viagem;
        IF v_status <> 'AGENDADA' THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'So e possivel responder solicitacoes de viagem agendada.';
        END IF;
        IF NEW.status = 'ACEITA' AND v_partic >= v_vagas THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Viagem sem vagas disponiveis.';
        END IF;
 
        IF NEW.dt_resposta IS NULL THEN
            SET NEW.dt_resposta = CURRENT_TIMESTAMP;
        END IF;
        IF NEW.status <> 'ACEITA' THEN
            SET NEW.valor_rateio = NULL;
        END IF;
    END IF;
END$$
 
-- Avaliação: só depois da viagem concluída e com carona aceita
CREATE TRIGGER trg_avaliacao_bi BEFORE INSERT ON Avaliacao
FOR EACH ROW
BEGIN
    DECLARE v_status_sol    VARCHAR(10);
    DECLARE v_status_viagem VARCHAR(15);
    SELECT s.status, v.status INTO v_status_sol, v_status_viagem
      FROM Solicitacao s JOIN Viagem v ON v.cod_viagem = s.cod_viagem
     WHERE s.cod_solicitacao = NEW.cod_solicitacao;
    IF v_status_sol <> 'ACEITA' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'So e possivel avaliar uma carona aceita.';
    END IF;
    IF v_status_viagem <> 'CONCLUIDA' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'So e possivel avaliar apos a viagem ser concluida.';
    END IF;
END$$
 
-- =====================================================================
-- PROCEDURES DE OPERAÇÃO (Aulas 11 e 12) — com transação e bloqueio
-- de linha (FOR UPDATE) para evitar dois aceites na mesma vaga
-- =====================================================================
 
-- Recalcula qtd_participantes; a trigger trg_viagem_au refaz o rateio
CREATE PROCEDURE sp_atualizar_participantes(IN p_viagem INT)
BEGIN
    -- a contagem é feita antes do UPDATE: a trigger trg_viagem_au altera
    -- Solicitacao, então ela não pode estar sendo lida pelo mesmo comando
    DECLARE v_qtd INT;
    SELECT COUNT(*) INTO v_qtd FROM Solicitacao
     WHERE cod_viagem = p_viagem AND status = 'ACEITA';
    UPDATE Viagem SET qtd_participantes = v_qtd
     WHERE cod_viagem = p_viagem;
END$$
 
-- Motorista aceita ou recusa um pedido
CREATE PROCEDURE sp_responder_solicitacao(IN p_solicitacao INT, IN p_resposta VARCHAR(10))
BEGIN
    DECLARE v_viagem INT;
    DECLARE v_status VARCHAR(10);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
 
    IF p_resposta NOT IN ('ACEITA','RECUSADA') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Resposta deve ser ACEITA ou RECUSADA.';
    END IF;
 
    START TRANSACTION;
    SELECT cod_viagem, status INTO v_viagem, v_status FROM Solicitacao
     WHERE cod_solicitacao = p_solicitacao FOR UPDATE;
    IF v_viagem IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Solicitacao nao encontrada.';
    END IF;
    -- v2: responder de novo um pedido ja respondido nao faz sentido
    -- (antes, ACEITA -> ACEITA passava sem erro e mudava a dt_resposta)
    IF v_status <> 'PENDENTE' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Esta solicitacao ja foi respondida ou cancelada.';
    END IF;
    SELECT cod_viagem INTO v_viagem FROM Viagem WHERE cod_viagem = v_viagem FOR UPDATE;
 
    UPDATE Solicitacao SET status = p_resposta, dt_resposta = CURRENT_TIMESTAMP
     WHERE cod_solicitacao = p_solicitacao;
    CALL sp_atualizar_participantes(v_viagem);
    COMMIT;
END$$
 
-- Passageiro cancela o próprio pedido (pendente ou aceito)
CREATE PROCEDURE sp_cancelar_solicitacao(IN p_solicitacao INT)
BEGIN
    DECLARE v_viagem INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
 
    START TRANSACTION;
    SELECT cod_viagem INTO v_viagem FROM Solicitacao
     WHERE cod_solicitacao = p_solicitacao FOR UPDATE;
    IF v_viagem IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Solicitacao nao encontrada.';
    END IF;
    UPDATE Solicitacao SET status = 'CANCELADA', dt_resposta = CURRENT_TIMESTAMP
     WHERE cod_solicitacao = p_solicitacao;
    CALL sp_atualizar_participantes(v_viagem);
    COMMIT;
END$$
 
DELIMITER ;
 
-- =====================================================================
-- VIEWS (Aula 10) — consultas prontas e camada de segurança
-- =====================================================================
CREATE VIEW vw_viagens_disponiveis AS
SELECT v.cod_viagem,
       u.nome                          AS motorista,
       CONCAT(ve.marca, ' ', ve.modelo, ' - ', ve.placa) AS veiculo,
       o.cidade                        AS origem,
       d.cidade                        AS destino,
       v.dt_inicio,
       v.vagas - v.qtd_participantes   AS vagas_restantes,
       v.custo_total
  FROM Viagem v
  JOIN Usuario u  ON u.cod_usuario  = v.cod_motorista
  JOIN Veiculo ve ON ve.cod_veiculo = v.cod_veiculo
  LEFT JOIN Ponto o ON o.cod_viagem = v.cod_viagem AND o.tipo = 'ORIGEM'
  LEFT JOIN Ponto d ON d.cod_viagem = v.cod_viagem AND d.tipo = 'DESTINO'
 WHERE v.status = 'AGENDADA'
   AND v.vagas > v.qtd_participantes;
 
CREATE VIEW vw_reputacao_motorista AS
SELECT m.cod_motorista,
       u.nome,
       COUNT(a.cod_avaliacao)  AS qtd_avaliacoes,
       ROUND(AVG(a.nota), 2)   AS media_nota
  FROM Motorista m
  JOIN Usuario u          ON u.cod_usuario = m.cod_motorista
  LEFT JOIN Viagem v      ON v.cod_motorista = m.cod_motorista
  LEFT JOIN Solicitacao s ON s.cod_viagem = v.cod_viagem
  LEFT JOIN Avaliacao a   ON a.cod_solicitacao = s.cod_solicitacao
                         AND a.autor = 'PASSAGEIRO'
 GROUP BY m.cod_motorista, u.nome;
 
-- Dados públicos do usuário: sem senha e com CPF mascarado (segurança)
CREATE VIEW vw_usuario_publico AS
SELECT u.cod_usuario,
       u.nome,
       u.email,
       fn_mascarar_cpf(u.cpf)   AS cpf_mascarado,
       i.sigla                  AS instituicao,
       fn_idade(u.dt_nascimento) AS idade,
       CASE WHEN m.cod_motorista IS NULL THEN 'PASSAGEIRO' ELSE 'MOTORISTA' END AS perfil,
       u.verificado,
       u.ativo,
       u.dt_cadastro
  FROM Usuario u
  LEFT JOIN Instituicao i ON i.cod_instituicao = u.cod_instituicao
  LEFT JOIN Motorista m   ON m.cod_motorista   = u.cod_usuario;
 
-- Uma linha por viagem com tudo o que a tela de detalhes precisa
CREATE VIEW vw_detalhe_viagem AS
SELECT v.cod_viagem,
       v.status,
       v.dt_inicio,
       v.dt_fim,
       v.cod_motorista,
       u.nome                                   AS motorista,
       CONCAT(ve.marca, ' ', ve.modelo, ' ', ve.cor, ' - ', ve.placa) AS veiculo,
       c.tipo                                   AS combustivel,
       o.cidade                                 AS origem,
       d.cidade                                 AS destino,
       (SELECT COUNT(*) FROM Ponto p
         WHERE p.cod_viagem = v.cod_viagem AND p.tipo = 'PARADA') AS qtd_paradas,
       v.distancia_km,
       v.vagas,
       v.qtd_participantes,
       v.vagas - v.qtd_participantes            AS vagas_restantes,
       v.custo_total
  FROM Viagem v
  JOIN Usuario u      ON u.cod_usuario      = v.cod_motorista
  JOIN Veiculo ve     ON ve.cod_veiculo     = v.cod_veiculo
  JOIN Combustivel c  ON c.cod_combustivel  = v.cod_combustivel
  LEFT JOIN Ponto o   ON o.cod_viagem = v.cod_viagem AND o.tipo = 'ORIGEM'
  LEFT JOIN Ponto d   ON d.cod_viagem = v.cod_viagem AND d.tipo = 'DESTINO';
 
-- Todas as caronas pedidas, com passageiro, motorista e ponto de embarque
CREATE VIEW vw_caronas AS
SELECT s.cod_solicitacao,
       s.cod_viagem,
       up.nome                 AS passageiro,
       um.nome                 AS motorista,
       v.dt_inicio,
       p.cidade                AS cidade_embarque,
       CONCAT(p.logradouro, ', ', IFNULL(p.numero, 's/n')) AS ponto_embarque,
       s.distancia_trecho_km,
       s.status,
       s.valor_rateio
  FROM Solicitacao s
  JOIN Usuario up ON up.cod_usuario = s.cod_passageiro
  JOIN Viagem v   ON v.cod_viagem   = s.cod_viagem
  JOIN Usuario um ON um.cod_usuario = v.cod_motorista
  JOIN Ponto p    ON p.cod_ponto    = s.cod_ponto;
 
-- =====================================================================
-- PROCEDURES DE CONSULTA — chamadas pelo sistema Java (console)
-- Todas devolvem uma tabela de resultado: CALL sp_xxx(...);
-- Parâmetros de filtro aceitam NULL = "sem filtro".
-- =====================================================================
DELIMITER $$
 
-- ---------- Aula 7 — SELECT: filtros, ordenação e funções -----------
 
-- 1. Busca viagens agendadas com vaga (filtros opcionais por cidade e data)
CREATE PROCEDURE sp_buscar_viagens(IN p_origem VARCHAR(100), IN p_destino VARCHAR(100),
                                   IN p_data DATE)
READS SQL DATA
BEGIN
    SELECT cod_viagem,
           DATE_FORMAT(dt_inicio, '%d/%m/%Y %H:%i') AS saida,
           motorista,
           origem,
           destino,
           vagas_restantes,
           fn_formatar_moeda(custo_total)           AS custo_total
      FROM vw_detalhe_viagem
     WHERE status = 'AGENDADA'
       AND vagas_restantes > 0
       AND (p_origem  IS NULL OR p_origem  = '' OR origem  LIKE CONCAT('%', p_origem,  '%'))
       AND (p_destino IS NULL OR p_destino = '' OR destino LIKE CONCAT('%', p_destino, '%'))
       AND (p_data    IS NULL OR DATE(dt_inicio) = p_data)
     ORDER BY dt_inicio, vw_detalhe_viagem.custo_total;
END$$
 
-- 2. Busca usuários ativos por parte do nome ou e-mail (sem dados sensíveis)
CREATE PROCEDURE sp_buscar_usuarios(IN p_texto VARCHAR(150))
READS SQL DATA
BEGIN
    SELECT cod_usuario,
           nome,
           email,
           cpf_mascarado,
           IFNULL(instituicao, '-')        AS instituicao,
           idade,
           perfil,
           IF(verificado = 1, 'Sim', 'Nao') AS verificado
      FROM vw_usuario_publico
     WHERE ativo = 1
       AND (p_texto IS NULL OR p_texto = ''
            OR nome  LIKE CONCAT('%', p_texto, '%')
            OR email LIKE CONCAT('%', p_texto, '%'))
     ORDER BY nome;
END$$
 
-- ---------- Aula 8 — JOINs e subconsultas ---------------------------
 
-- 3. Ficha completa de uma viagem (JOIN de 5 tabelas via view + functions)
CREATE PROCEDURE sp_detalhes_viagem(IN p_viagem INT)
READS SQL DATA
BEGIN
    IF NOT EXISTS (SELECT 1 FROM Viagem WHERE cod_viagem = p_viagem) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Viagem nao encontrada.';
    END IF;
    SELECT d.cod_viagem,
           d.status,
           DATE_FORMAT(d.dt_inicio, '%d/%m/%Y %H:%i')          AS saida,
           IFNULL(DATE_FORMAT(d.dt_fim, '%d/%m/%Y %H:%i'), '-') AS chegada,
           d.motorista,
           IFNULL(fn_media_avaliacao_motorista(d.cod_motorista), 0) AS nota_motorista,
           d.veiculo,
           d.combustivel,
           d.origem,
           d.destino,
           d.qtd_paradas,
           d.distancia_km,
           d.vagas,
           d.qtd_participantes                 AS passageiros,
           fn_vagas_restantes(d.cod_viagem)    AS vagas_restantes,
           fn_formatar_moeda(d.custo_total)    AS custo_total
      FROM vw_detalhe_viagem d
     WHERE d.cod_viagem = p_viagem;
END$$
 
-- 4. Rota da viagem, com quantos passageiros embarcam em cada ponto
--    (subconsulta correlacionada)
CREATE PROCEDURE sp_rota_viagem(IN p_viagem INT)
READS SQL DATA
BEGIN
    IF NOT EXISTS (SELECT 1 FROM Viagem WHERE cod_viagem = p_viagem) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Viagem nao encontrada.';
    END IF;
    SELECT p.ordem,
           p.tipo,
           CONCAT(p.logradouro, ', ', IFNULL(p.numero, 's/n'),
                  IFNULL(CONCAT(' - ', p.bairro), '')) AS endereco,
           p.cidade,
           p.estado,
           (SELECT COUNT(*) FROM Solicitacao s
             WHERE s.cod_ponto = p.cod_ponto AND s.status = 'ACEITA') AS embarques
      FROM Ponto p
     WHERE p.cod_viagem = p_viagem
     ORDER BY p.ordem;
END$$
 
-- 5. Passageiros que pediram carona numa viagem (INNER e LEFT JOIN)
CREATE PROCEDURE sp_passageiros_viagem(IN p_viagem INT)
READS SQL DATA
BEGIN
    IF NOT EXISTS (SELECT 1 FROM Viagem WHERE cod_viagem = p_viagem) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Viagem nao encontrada.';
    END IF;
    SELECT s.cod_solicitacao,
           u.nome                              AS passageiro,
           IFNULL(i.sigla, '-')                AS instituicao,
           CONCAT(pt.ordem, ' - ', pt.logradouro) AS ponto_embarque,
           s.distancia_trecho_km               AS trecho_km,
           s.status,
           fn_formatar_moeda(s.valor_rateio)   AS rateio
      FROM Solicitacao s
      JOIN Usuario u          ON u.cod_usuario      = s.cod_passageiro
      JOIN Ponto pt           ON pt.cod_ponto       = s.cod_ponto
      LEFT JOIN Instituicao i ON i.cod_instituicao  = u.cod_instituicao
     WHERE s.cod_viagem = p_viagem
     ORDER BY FIELD(s.status, 'ACEITA', 'PENDENTE', 'RECUSADA', 'CANCELADA'), u.nome;
END$$
 
-- 6. Histórico do usuário como motorista E como passageiro (UNION ALL)
CREATE PROCEDURE sp_historico_usuario(IN p_usuario INT)
READS SQL DATA
BEGIN
    IF NOT EXISTS (SELECT 1 FROM Usuario WHERE cod_usuario = p_usuario) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Usuario nao encontrado.';
    END IF;
    SELECT h.papel,
           h.cod_viagem,
           DATE_FORMAT(h.dt_inicio, '%d/%m/%Y %H:%i') AS saida,
           h.origem,
           h.destino,
           h.status_viagem,
           h.situacao,
           fn_formatar_moeda(h.valor)                AS valor
      FROM (
            SELECT 'MOTORISTA'        AS papel,
                   d.cod_viagem, d.dt_inicio, d.origem, d.destino,
                   d.status           AS status_viagem,
                   CONCAT(d.qtd_participantes, ' passageiro(s)') AS situacao,
                   ROUND(d.custo_total / (d.qtd_participantes + 1), 2) AS valor
              FROM vw_detalhe_viagem d
             WHERE d.cod_motorista = p_usuario
            UNION ALL
            SELECT 'PASSAGEIRO',
                   d.cod_viagem, d.dt_inicio, d.origem, d.destino,
                   d.status,
                   s.status,
                   s.valor_rateio
              FROM Solicitacao s
              JOIN vw_detalhe_viagem d ON d.cod_viagem = s.cod_viagem
             WHERE s.cod_passageiro = p_usuario
           ) h
     ORDER BY h.dt_inicio DESC;
END$$
 
-- 7. Usuários ativos que ainda não pegaram nenhuma carona
--    (LEFT JOIN ... IS NULL + subconsulta NOT EXISTS)
CREATE PROCEDURE sp_usuarios_sem_carona()
READS SQL DATA
BEGIN
    SELECT u.cod_usuario,
           u.nome,
           u.email,
           IFNULL(i.sigla, '-')                      AS instituicao,
           DATE_FORMAT(u.dt_cadastro, '%d/%m/%Y')    AS cadastro
      FROM Usuario u
      LEFT JOIN Instituicao i ON i.cod_instituicao = u.cod_instituicao
      LEFT JOIN Solicitacao s ON s.cod_passageiro  = u.cod_usuario
                             AND s.status = 'ACEITA'
     WHERE u.ativo = 1
       AND s.cod_solicitacao IS NULL
       AND NOT EXISTS (SELECT 1 FROM Motorista m WHERE m.cod_motorista = u.cod_usuario)
     ORDER BY u.nome;
END$$
 
-- 8. Viagens com custo acima da média geral (subconsulta escalar)
CREATE PROCEDURE sp_viagens_acima_media()
READS SQL DATA
BEGIN
    SELECT d.cod_viagem,
           d.motorista,
           d.origem,
           d.destino,
           d.status,
           fn_formatar_moeda(d.custo_total) AS custo_total,
           fn_formatar_moeda((SELECT AVG(custo_total) FROM Viagem
                               WHERE status <> 'CANCELADA')) AS media_geral
      FROM vw_detalhe_viagem d
     WHERE d.status <> 'CANCELADA'
       AND d.custo_total > (SELECT AVG(custo_total) FROM Viagem WHERE status <> 'CANCELADA')
     ORDER BY d.custo_total DESC;
END$$
 
-- ---------- Aula 9 — Funções de agregação, GROUP BY e HAVING --------
 
-- 9. Ranking dos motoristas pela nota dos passageiros
CREATE PROCEDURE sp_ranking_motoristas(IN p_min_avaliacoes INT)
READS SQL DATA
BEGIN
    SELECT u.nome                         AS motorista,
           COUNT(DISTINCT v.cod_viagem)   AS viagens_concluidas,
           COUNT(a.cod_avaliacao)         AS avaliacoes,
           ROUND(AVG(a.nota), 2)          AS media,
           MIN(a.nota)                    AS menor_nota,
           MAX(a.nota)                    AS maior_nota
      FROM Motorista m
      JOIN Usuario u          ON u.cod_usuario   = m.cod_motorista
      JOIN Viagem v           ON v.cod_motorista = m.cod_motorista
                             AND v.status = 'CONCLUIDA'
      LEFT JOIN Solicitacao s ON s.cod_viagem      = v.cod_viagem
      LEFT JOIN Avaliacao a   ON a.cod_solicitacao = s.cod_solicitacao
                             AND a.autor = 'PASSAGEIRO'
     GROUP BY m.cod_motorista, u.nome
    HAVING COUNT(a.cod_avaliacao) >= IFNULL(p_min_avaliacoes, 0)
     ORDER BY media DESC, avaliacoes DESC, u.nome;
END$$
 
-- 10. Relatório mês a mês de um ano
CREATE PROCEDURE sp_relatorio_mensal(IN p_ano INT)
READS SQL DATA
BEGIN
    SELECT DATE_FORMAT(dt_inicio, '%Y-%m')                              AS mes,
           COUNT(*)                                                     AS viagens,
           SUM(status = 'CONCLUIDA')                                    AS concluidas,
           SUM(status = 'CANCELADA')                                    AS canceladas,
           SUM(CASE WHEN status <> 'CANCELADA' THEN distancia_km ELSE 0 END) AS km_rodados,
           fn_formatar_moeda(SUM(CASE WHEN status = 'CONCLUIDA'
                                      THEN custo_total ELSE 0 END))     AS custo_dividido,
           ROUND(AVG(CASE WHEN status = 'CONCLUIDA'
                          THEN qtd_participantes END), 1)               AS media_passageiros
      FROM Viagem
     WHERE YEAR(dt_inicio) = p_ano
     GROUP BY DATE_FORMAT(dt_inicio, '%Y-%m')
     ORDER BY mes;
END$$
 
-- 11. Resumo por instituição de ensino
CREATE PROCEDURE sp_resumo_por_instituicao()
READS SQL DATA
BEGIN
    SELECT IFNULL(i.sigla, '(sem instituicao)')                           AS instituicao,
           COUNT(DISTINCT u.cod_usuario)                                   AS usuarios,
           COUNT(DISTINCT m.cod_motorista)                                 AS motoristas,
           COUNT(DISTINCT CASE WHEN s.status = 'ACEITA'
                               THEN s.cod_solicitacao END)                 AS caronas_aceitas,
           fn_formatar_moeda(IFNULL(SUM(CASE WHEN s.status = 'ACEITA'
                                             THEN s.valor_rateio END), 0)) AS valor_caronas
      FROM Usuario u
      LEFT JOIN Instituicao i ON i.cod_instituicao = u.cod_instituicao
      LEFT JOIN Motorista m   ON m.cod_motorista   = u.cod_usuario
      LEFT JOIN Solicitacao s ON s.cod_passageiro  = u.cod_usuario
     WHERE u.ativo = 1
     GROUP BY i.cod_instituicao, i.sigla
     ORDER BY usuarios DESC, instituicao;
END$$
 
-- 12. Passageiros que mais usaram o app (HAVING com parâmetro)
CREATE PROCEDURE sp_resumo_passageiros(IN p_min_caronas INT)
READS SQL DATA
BEGIN
    SELECT u.nome                                           AS passageiro,
           COUNT(*)                                         AS caronas_concluidas,
           fn_formatar_moeda(fn_total_gasto_passageiro(u.cod_usuario)) AS total_pago,
           ROUND(SUM(s.distancia_trecho_km), 2)             AS km_percorridos
      FROM Solicitacao s
      JOIN Usuario u ON u.cod_usuario = s.cod_passageiro
      JOIN Viagem v  ON v.cod_viagem  = s.cod_viagem
     WHERE s.status = 'ACEITA'
       AND v.status = 'CONCLUIDA'
     GROUP BY u.cod_usuario, u.nome
    HAVING COUNT(*) >= IFNULL(p_min_caronas, 1)
     ORDER BY fn_total_gasto_passageiro(u.cod_usuario) DESC, u.nome;
END$$
 
-- 13. Números gerais da plataforma (painel)
CREATE PROCEDURE sp_estatisticas_gerais()
READS SQL DATA
BEGIN
    SELECT (SELECT COUNT(*) FROM Usuario   WHERE ativo = 1)               AS usuarios_ativos,
           (SELECT COUNT(*) FROM Motorista WHERE ativo = 1)               AS motoristas_ativos,
           (SELECT COUNT(*) FROM Viagem    WHERE status = 'AGENDADA')     AS viagens_agendadas,
           (SELECT COUNT(*) FROM Viagem    WHERE status = 'CONCLUIDA')    AS viagens_concluidas,
           (SELECT COUNT(*) FROM Solicitacao WHERE status = 'ACEITA')     AS caronas_aceitas,
           fn_formatar_moeda((SELECT IFNULL(SUM(custo_total), 0) FROM Viagem
                               WHERE status = 'CONCLUIDA'))               AS custo_dividido,
           (SELECT ROUND(AVG(nota), 2) FROM Avaliacao)                     AS media_avaliacoes;
END$$
 
DELIMITER ;
 
-- =====================================================================
-- EXTENSÕES PARA O CONSOLE JAVA (versão 2)
-- Objetos novos usados pelas telas de passageiro, motorista e avaliação:
--   3 functions : fn_distancia_km, fn_trecho_estimado,
--                 fn_media_avaliacao_passageiro
--   5 procedures: sp_match_rotas (RF06), sp_avaliacoes_pendentes (RF11),
--                 sp_iniciar_viagem, sp_concluir_viagem,
--                 sp_cancelar_viagem (RF10, com transação)
-- =====================================================================
DELIMITER $$

-- Distância em linha reta entre duas coordenadas (fórmula de Haversine)
CREATE FUNCTION fn_distancia_km(p_lat1 DECIMAL(9,6), p_lng1 DECIMAL(9,6),
                                p_lat2 DECIMAL(9,6), p_lng2 DECIMAL(9,6))
RETURNS DECIMAL(9,3)
DETERMINISTIC NO SQL
BEGIN
    IF p_lat1 IS NULL OR p_lng1 IS NULL OR p_lat2 IS NULL OR p_lng2 IS NULL THEN
        RETURN NULL;
    END IF;
    RETURN ROUND(6371 * 2 * ASIN(SQRT(
               POWER(SIN(RADIANS(p_lat2 - p_lat1) / 2), 2) +
               COS(RADIANS(p_lat1)) * COS(RADIANS(p_lat2)) *
               POWER(SIN(RADIANS(p_lng2 - p_lng1) / 2), 2))), 3);
END$$

-- Quantos km o passageiro percorre embarcando neste ponto até o DESTINO.
-- Com coordenadas: proporção da rota medida por Haversine.
-- Sem coordenadas: proporção pelo número de trechos que faltam.
CREATE FUNCTION fn_trecho_estimado(p_ponto INT)
RETURNS DECIMAL(7,2)
READS SQL DATA
BEGIN
    DECLARE v_viagem        INT;
    DECLARE v_ordem         INT;
    DECLARE v_tipo          VARCHAR(10);
    DECLARE v_distancia     DECIMAL(7,2);
    DECLARE v_total         DECIMAL(12,3);
    DECLARE v_restante      DECIMAL(12,3);
    DECLARE v_sem_coord     INT;
    DECLARE v_segmentos     INT;
    DECLARE v_seg_restantes INT;

    SELECT p.cod_viagem, p.ordem, p.tipo, v.distancia_km
      INTO v_viagem, v_ordem, v_tipo, v_distancia
      FROM Ponto p JOIN Viagem v ON v.cod_viagem = p.cod_viagem
     WHERE p.cod_ponto = p_ponto;

    IF v_viagem IS NULL OR v_tipo = 'DESTINO' THEN
        RETURN NULL;
    END IF;
    IF v_tipo = 'ORIGEM' THEN
        RETURN v_distancia;
    END IF;

    -- cada ponto "a" é ligado ao próximo ponto "b" da rota (subconsulta)
    SELECT SUM(fn_distancia_km(a.latitude, a.longitude, b.latitude, b.longitude)),
           SUM(CASE WHEN a.ordem >= v_ordem
                    THEN fn_distancia_km(a.latitude, a.longitude, b.latitude, b.longitude) END),
           SUM(a.latitude IS NULL OR a.longitude IS NULL OR b.latitude IS NULL OR b.longitude IS NULL),
           COUNT(*),
           SUM(a.ordem >= v_ordem)
      INTO v_total, v_restante, v_sem_coord, v_segmentos, v_seg_restantes
      FROM Ponto a
      JOIN Ponto b ON b.cod_viagem = a.cod_viagem
                  AND b.ordem = (SELECT MIN(c.ordem) FROM Ponto c
                                  WHERE c.cod_viagem = a.cod_viagem AND c.ordem > a.ordem)
     WHERE a.cod_viagem = v_viagem;

    IF v_segmentos IS NULL OR v_segmentos = 0 THEN
        RETURN v_distancia;
    END IF;
    IF v_sem_coord = 0 AND v_total > 0 THEN
        RETURN GREATEST(LEAST(ROUND(v_distancia * v_restante / v_total, 2), v_distancia), 0.01);
    END IF;
    RETURN GREATEST(ROUND(v_distancia * v_seg_restantes / v_segmentos, 2), 0.01);
END$$

-- Nota média que os motoristas deram ao passageiro
CREATE FUNCTION fn_media_avaliacao_passageiro(p_usuario INT)
RETURNS DECIMAL(3,2)
READS SQL DATA
BEGIN
    RETURN (SELECT ROUND(AVG(a.nota), 2)
              FROM Avaliacao a
              JOIN Solicitacao s ON s.cod_solicitacao = a.cod_solicitacao
             WHERE s.cod_passageiro = p_usuario
               AND a.autor = 'MOTORISTA');
END$$

-- 14. RF06 — Match: viagens com trajeto compatível com o do passageiro.
--     Procura um ponto de embarque (origem ou parada) parecido com a
--     origem do passageiro e um DESTINO parecido com o destino dele.
--     similaridade: logradouro = 3, bairro = 2, cidade = 1 (em cada ponta)
CREATE PROCEDURE sp_match_rotas(IN p_origem VARCHAR(150), IN p_destino VARCHAR(150),
                                IN p_data DATE, IN p_passageiro INT)
READS SQL DATA
BEGIN
    SELECT v.cod_viagem,
           DATE_FORMAT(v.dt_inicio, '%d/%m/%Y %H:%i')                    AS saida,
           u.nome                                                        AS motorista,
           IFNULL(fn_media_avaliacao_motorista(v.cod_motorista), 0)      AS nota_motorista,
           e.cod_ponto                                                   AS cod_ponto_embarque,
           e.tipo                                                        AS tipo_embarque,
           CONCAT(e.logradouro, ', ', IFNULL(e.numero, 's/n'),
                  IFNULL(CONCAT(' - ', e.bairro), ''), ' (', e.cidade, ')') AS embarque,
           CONCAT(d.logradouro, IFNULL(CONCAT(' - ', d.bairro), ''),
                  ' (', d.cidade, ')')                                   AS destino,
           fn_trecho_estimado(e.cod_ponto)                               AS trecho_km,
           fn_calcular_rateio(v.custo_total, fn_trecho_estimado(e.cod_ponto),
                              v.distancia_km, v.qtd_participantes + 1)   AS rateio_estimado,
           v.vagas - v.qtd_participantes                                 AS vagas_restantes,
           ( CASE WHEN p_origem IS NULL OR p_origem = '' THEN 0
                  WHEN e.logradouro LIKE CONCAT('%', p_origem, '%') THEN 3
                  WHEN e.bairro     LIKE CONCAT('%', p_origem, '%') THEN 2
                  ELSE 1 END
           + CASE WHEN p_destino IS NULL OR p_destino = '' THEN 0
                  WHEN d.logradouro LIKE CONCAT('%', p_destino, '%') THEN 3
                  WHEN d.bairro     LIKE CONCAT('%', p_destino, '%') THEN 2
                  ELSE 1 END )                                           AS similaridade
      FROM Viagem v
      JOIN Usuario u ON u.cod_usuario = v.cod_motorista
      JOIN Ponto e   ON e.cod_viagem  = v.cod_viagem AND e.tipo <> 'DESTINO'
      JOIN Ponto d   ON d.cod_viagem  = v.cod_viagem AND d.tipo =  'DESTINO'
     WHERE v.status = 'AGENDADA'
       AND v.vagas > v.qtd_participantes
       AND DATE(v.dt_inicio) >= CURDATE()
       AND (p_data IS NULL OR DATE(v.dt_inicio) = p_data)
       AND (p_passageiro IS NULL OR (
                v.cod_motorista <> p_passageiro
            AND NOT EXISTS (SELECT 1 FROM Solicitacao s
                             WHERE s.cod_viagem = v.cod_viagem
                               AND s.cod_passageiro = p_passageiro)))
       AND (p_origem IS NULL OR p_origem = ''
            OR e.cidade     LIKE CONCAT('%', p_origem, '%')
            OR e.bairro     LIKE CONCAT('%', p_origem, '%')
            OR e.logradouro LIKE CONCAT('%', p_origem, '%'))
       AND (p_destino IS NULL OR p_destino = ''
            OR d.cidade     LIKE CONCAT('%', p_destino, '%')
            OR d.bairro     LIKE CONCAT('%', p_destino, '%')
            OR d.logradouro LIKE CONCAT('%', p_destino, '%'))
     ORDER BY similaridade DESC, v.dt_inicio, e.ordem;
END$$

-- 15. RF11 — Caronas que o usuário ainda precisa avaliar (como passageiro
--     avalia o motorista; como motorista avalia cada passageiro)
CREATE PROCEDURE sp_avaliacoes_pendentes(IN p_usuario INT)
READS SQL DATA
BEGIN
    SELECT t.cod_solicitacao, t.meu_papel, t.avaliar, t.cod_viagem,
           DATE_FORMAT(t.dt_inicio, '%d/%m/%Y %H:%i') AS data_viagem
      FROM (
            SELECT s.cod_solicitacao, 'PASSAGEIRO' AS meu_papel, um.nome AS avaliar,
                   s.cod_viagem, v.dt_inicio
              FROM Solicitacao s
              JOIN Viagem v   ON v.cod_viagem  = s.cod_viagem
              JOIN Usuario um ON um.cod_usuario = v.cod_motorista
             WHERE s.cod_passageiro = p_usuario
               AND s.status = 'ACEITA' AND v.status = 'CONCLUIDA'
               AND NOT EXISTS (SELECT 1 FROM Avaliacao a
                                WHERE a.cod_solicitacao = s.cod_solicitacao
                                  AND a.autor = 'PASSAGEIRO')
            UNION ALL
            SELECT s.cod_solicitacao, 'MOTORISTA', up.nome, s.cod_viagem, v.dt_inicio
              FROM Solicitacao s
              JOIN Viagem v   ON v.cod_viagem  = s.cod_viagem
              JOIN Usuario up ON up.cod_usuario = s.cod_passageiro
             WHERE v.cod_motorista = p_usuario
               AND s.status = 'ACEITA' AND v.status = 'CONCLUIDA'
               AND NOT EXISTS (SELECT 1 FROM Avaliacao a
                                WHERE a.cod_solicitacao = s.cod_solicitacao
                                  AND a.autor = 'MOTORISTA')
           ) t
     ORDER BY t.dt_inicio DESC, t.cod_solicitacao;
END$$

-- Operação: motorista inicia a viagem. Pedidos ainda sem resposta são
-- recusados automaticamente e a saída real é registrada.
CREATE PROCEDURE sp_iniciar_viagem(IN p_viagem INT, IN p_motorista INT)
BEGIN
    DECLARE v_motorista INT;
    DECLARE v_status    VARCHAR(15);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    SELECT cod_motorista, status INTO v_motorista, v_status
      FROM Viagem WHERE cod_viagem = p_viagem FOR UPDATE;
    IF v_motorista IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Viagem nao encontrada.';
    END IF;
    IF p_motorista IS NOT NULL AND v_motorista <> p_motorista THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Esta viagem pertence a outro motorista.';
    END IF;
    IF v_status <> 'AGENDADA' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Somente viagem agendada pode ser iniciada.';
    END IF;

    UPDATE Solicitacao SET status = 'RECUSADA', dt_resposta = CURRENT_TIMESTAMP
     WHERE cod_viagem = p_viagem AND status = 'PENDENTE';
    UPDATE Viagem SET status = 'EM_ANDAMENTO', dt_inicio = LEAST(dt_inicio, NOW())
     WHERE cod_viagem = p_viagem;
    COMMIT;
END$$

-- Operação: motorista conclui a viagem (libera as avaliações)
CREATE PROCEDURE sp_concluir_viagem(IN p_viagem INT, IN p_motorista INT)
BEGIN
    DECLARE v_motorista INT;
    DECLARE v_status    VARCHAR(15);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    SELECT cod_motorista, status INTO v_motorista, v_status
      FROM Viagem WHERE cod_viagem = p_viagem FOR UPDATE;
    IF v_motorista IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Viagem nao encontrada.';
    END IF;
    IF p_motorista IS NOT NULL AND v_motorista <> p_motorista THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Esta viagem pertence a outro motorista.';
    END IF;
    IF v_status <> 'EM_ANDAMENTO' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Somente viagem em andamento pode ser concluida.';
    END IF;

    UPDATE Viagem SET status = 'CONCLUIDA',
                      dt_fim = GREATEST(NOW(), dt_inicio + INTERVAL 1 MINUTE)
     WHERE cod_viagem = p_viagem;
    COMMIT;
END$$

-- Operação: motorista cancela uma viagem agendada. Todos os pedidos
-- pendentes ou aceitos são cancelados junto (tudo ou nada).
CREATE PROCEDURE sp_cancelar_viagem(IN p_viagem INT, IN p_motorista INT)
BEGIN
    DECLARE v_motorista INT;
    DECLARE v_status    VARCHAR(15);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    SELECT cod_motorista, status INTO v_motorista, v_status
      FROM Viagem WHERE cod_viagem = p_viagem FOR UPDATE;
    IF v_motorista IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Viagem nao encontrada.';
    END IF;
    IF p_motorista IS NOT NULL AND v_motorista <> p_motorista THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Esta viagem pertence a outro motorista.';
    END IF;
    IF v_status <> 'AGENDADA' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Somente viagem agendada pode ser cancelada.';
    END IF;

    UPDATE Solicitacao SET status = 'CANCELADA', dt_resposta = CURRENT_TIMESTAMP
     WHERE cod_viagem = p_viagem AND status IN ('PENDENTE', 'ACEITA');
    CALL sp_atualizar_participantes(p_viagem);
    UPDATE Viagem SET status = 'CANCELADA' WHERE cod_viagem = p_viagem;
    COMMIT;
END$$

DELIMITER ;
 
-- =====================================================================
-- DADOS DE EXEMPLO
-- =====================================================================
INSERT INTO Instituicao (nome, sigla, cidade, estado) VALUES
 ('Universidade de Sorocaba',              'UNISO',  'Sorocaba', 'SP'),
 ('Faculdade de Tecnologia de Sorocaba',   'FATEC',  'Sorocaba', 'SP'),
 ('Universidade Federal de São Carlos',    'UFSCar', 'Sorocaba', 'SP');
 
-- Todos os usuários de exemplo têm a senha "vamo123" (guardada só como hash SHA-256)
INSERT INTO Usuario (cod_instituicao, nome, cpf, email, telefone, senha_hash,
                     dt_nascimento, verificado, dt_verificacao) VALUES
 (1,    'Ana Souza',      '11122233344', 'ana.souza@uniso.br',    '15991110001', SHA2('vamo123', 256),    '2003-04-12', 1, '2026-08-01 10:00:00'),
 (2,    'Bruno Lima',     '22233344455', 'bruno.lima@gmail.com',  '15991110002', SHA2('vamo123', 256),  '2001-09-30', 1, '2026-08-02 11:00:00'),
 (1,    'Carla Mendes',   '33344455566', 'carla@uniso.br',        '15991110003', SHA2('vamo123', 256),  '2004-01-20', 1, '2026-08-05 09:30:00'),
 (3,    'Diego Alves',    '44455566677', 'diego.alves@ufscar.br', '15991110004', SHA2('vamo123', 256),  '2002-07-07', 0, NULL),
 (NULL, 'Elisa Rocha',    '55566677788', 'elisa.rocha@outlook.com','1532221005', SHA2('vamo123', 256),  '1999-12-01', 1, '2026-08-10 14:00:00'),
 (2,    'Felipe Duarte',  '66677788899', 'felipe@fatec.sp.gov.br','15991110006', SHA2('vamo123', 256), '2000-03-15', 1, '2026-08-11 08:00:00');
 
INSERT INTO Motorista (cod_motorista, cnh, categoria_cnh, validade_cnh) VALUES
 (1, '01234567890', 'B',  '2030-05-10'),
 (5, '09876543210', 'AB', '2029-11-20');
 
INSERT INTO Veiculo (marca, modelo, cor, placa, ano, renavam, qtd_lugares) VALUES
 ('Chevrolet', 'Onix',  'Prata',  'FTR4A21', 2021, '12345678901', 5),
 ('Fiat',      'Mobi',  'Branco', 'DKS1234', 2018, '10987654321', 4);
 
INSERT INTO Combustivel (tipo, preco_litro) VALUES
 ('GASOLINA', 6.190),
 ('ETANOL',   4.290),
 ('DIESEL',   6.050);
 
-- Viagem 1: Ana leva colegas do Centro até a UNISO (preço e custo automáticos)
INSERT INTO Viagem (cod_motorista, cod_veiculo, cod_combustivel, dt_inicio,
                    vagas, distancia_km, consumo_aplicado_km_l)
VALUES (1, 1, 1, '2026-10-13 07:00:00', 3, 18.00, 12.00);
 
-- Viagem 2: Elisa vai de Sorocaba a Votorantim
INSERT INTO Viagem (cod_motorista, cod_veiculo, cod_combustivel, dt_inicio,
                    vagas, distancia_km, consumo_aplicado_km_l)
VALUES (5, 2, 2, '2026-11-04 18:30:00', 2, 12.00, 9.50);
 
INSERT INTO Ponto (cod_viagem, ordem, tipo, logradouro, numero, bairro, cep, cidade, estado, latitude, longitude) VALUES
 (1, 1, 'ORIGEM',  'Rua XV de Novembro',      '100',  'Centro',          '18010080', 'Sorocaba',   'SP', -23.501520, -47.458170),
 (1, 2, 'PARADA',  'Av. General Carneiro',    '850',  'Lageado',         '18030000', 'Sorocaba',   'SP', -23.483900, -47.444600),
 (1, 3, 'DESTINO', 'Rod. Raposo Tavares, km 92.5', NULL, 'Jardim Novo Eldorado', '18023000', 'Sorocaba', 'SP', -23.524600, -47.501900),
 (2, 1, 'ORIGEM',  'Av. Dom Aguirre',         '2200', 'Santa Rosália',   '18090002', 'Sorocaba',   'SP', -23.493300, -47.444200),
 (2, 2, 'DESTINO', 'Av. 31 de Março',         '500',  'Centro',          '18110005', 'Votorantim', 'SP', -23.546700, -47.437800);
 
-- Pedidos de carona
INSERT INTO Solicitacao (cod_viagem, cod_passageiro, cod_ponto, distancia_trecho_km) VALUES
 (1, 2, 1, 18.00),   -- Bruno embarca na origem (viagem inteira)
 (1, 3, 2, 12.00),   -- Carla embarca na parada
 (1, 6, 2, 12.00),   -- Felipe embarca na parada
 (2, 3, 4, 12.00);   -- Carla também pede na viagem da Elisa
 
CALL sp_responder_solicitacao(1, 'ACEITA');
CALL sp_responder_solicitacao(2, 'ACEITA');
CALL sp_responder_solicitacao(3, 'RECUSADA');
CALL sp_responder_solicitacao(4, 'ACEITA');
 
-- Viagem 1 acontece e termina
UPDATE Viagem SET status = 'EM_ANDAMENTO' WHERE cod_viagem = 1;
UPDATE Viagem SET status = 'CONCLUIDA', dt_fim = '2026-10-13 07:40:00' WHERE cod_viagem = 1;
 
INSERT INTO Avaliacao (cod_solicitacao, autor, nota, comentario) VALUES
 (1, 'PASSAGEIRO', 5, 'Pontual e dirigiu com cuidado.'),
 (1, 'MOTORISTA',  5, 'Passageiro educado.'),
 (2, 'PASSAGEIRO', 4, 'Boa carona, música alta.');
 
-- Viagem 3: Elisa leva Bruno e Felipe de Votorantim até a faculdade (concluída)
INSERT INTO Viagem (cod_motorista, cod_veiculo, cod_combustivel, dt_inicio,
                    vagas, distancia_km, consumo_aplicado_km_l)
VALUES (5, 2, 2, '2026-10-06 07:15:00', 2, 25.00, 10.00);
 
INSERT INTO Ponto (cod_viagem, ordem, tipo, logradouro, numero, bairro, cep, cidade, estado, latitude, longitude) VALUES
 (3, 1, 'ORIGEM',  'Av. Gisele Constantino',            '1500', 'Parque Bela Vista', '18110650', 'Votorantim', 'SP', -23.538900, -47.441300),
 (3, 2, 'PARADA',  'Av. Antônio Carlos Comitre',        '1200', 'Parque Campolim',   '18047620', 'Sorocaba',   'SP', -23.526200, -47.465800),
 (3, 3, 'DESTINO', 'Rod. Senador José Ermírio de Moraes','1425', 'Alto da Boa Vista', '18085784', 'Sorocaba',   'SP', -23.466700, -47.426300);
 
INSERT INTO Solicitacao (cod_viagem, cod_passageiro, cod_ponto, distancia_trecho_km) VALUES
 (3, 2, 6, 25.00),   -- Bruno embarca na origem
 (3, 6, 7, 15.00);   -- Felipe embarca na parada
 
CALL sp_responder_solicitacao(5, 'ACEITA');
CALL sp_responder_solicitacao(6, 'ACEITA');
 
UPDATE Viagem SET status = 'EM_ANDAMENTO' WHERE cod_viagem = 3;
UPDATE Viagem SET status = 'CONCLUIDA', dt_fim = '2026-10-06 07:55:00' WHERE cod_viagem = 3;
 
INSERT INTO Avaliacao (cod_solicitacao, autor, nota, comentario) VALUES
 (5, 'PASSAGEIRO', 5, 'Carro limpo e pontual.'),
 (6, 'PASSAGEIRO', 5, 'Ótima conversa no caminho.'),
 (6, 'MOTORISTA',  5, 'Estava no ponto na hora combinada.');
 
-- Viagem 4: Ana vai para Itu em dezembro (agendada, ainda sem pedidos)
INSERT INTO Viagem (cod_motorista, cod_veiculo, cod_combustivel, dt_inicio,
                    vagas, distancia_km, consumo_aplicado_km_l)
VALUES (1, 1, 1, '2026-12-10 07:00:00', 4, 40.00, 12.50);
 
INSERT INTO Ponto (cod_viagem, ordem, tipo, logradouro, numero, bairro, cep, cidade, estado) VALUES
 (4, 1, 'ORIGEM',  'Rua XV de Novembro',       '100', 'Centro', '18010080', 'Sorocaba', 'SP'),
 (4, 2, 'DESTINO', 'Praça Padre Miguel',       '50',  'Centro', '13300010', 'Itu',      'SP');
 
-- Viagem 5: Ana cancelou uma viagem de outubro
INSERT INTO Viagem (cod_motorista, cod_veiculo, cod_combustivel, dt_inicio,
                    vagas, distancia_km, consumo_aplicado_km_l)
VALUES (1, 1, 1, '2026-10-20 18:00:00', 2, 10.00, 12.00);
 
INSERT INTO Ponto (cod_viagem, ordem, tipo, logradouro, numero, bairro, cep, cidade, estado) VALUES
 (5, 1, 'ORIGEM',  'Av. General Carneiro',     '850', 'Lageado', '18030000', 'Sorocaba', 'SP'),
 (5, 2, 'DESTINO', 'Rua XV de Novembro',       '100', 'Centro',  '18010080', 'Sorocaba', 'SP');
 
UPDATE Viagem SET status = 'CANCELADA' WHERE cod_viagem = 5;
 
-- =====================================================================
-- SEGURANÇA (Aula 12) — usuários com o mínimo de privilégio necessário
-- Troque as senhas antes de colocar em produção.
-- =====================================================================
 
-- Usuário que o sistema Java usa: lê, cadastra, atualiza e chama as
-- procedures/functions. NÃO pode apagar dados nem alterar a estrutura.
DROP USER IF EXISTS 'vamojunto_app'@'localhost';
CREATE USER 'vamojunto_app'@'localhost' IDENTIFIED BY 'VamoJunto#2026';
GRANT SELECT, INSERT, UPDATE, EXECUTE ON vamojunto.* TO 'vamojunto_app'@'localhost';
 
-- Usuário só de leitura para relatórios: enxerga apenas as views
-- (sem senha_hash e com CPF mascarado), nunca as tabelas.
DROP USER IF EXISTS 'vamojunto_leitura'@'localhost';
CREATE USER 'vamojunto_leitura'@'localhost' IDENTIFIED BY 'Leitura#2026';
GRANT SELECT ON vamojunto.vw_usuario_publico     TO 'vamojunto_leitura'@'localhost';
GRANT SELECT ON vamojunto.vw_detalhe_viagem      TO 'vamojunto_leitura'@'localhost';
GRANT SELECT ON vamojunto.vw_caronas             TO 'vamojunto_leitura'@'localhost';
GRANT SELECT ON vamojunto.vw_viagens_disponiveis TO 'vamojunto_leitura'@'localhost';
GRANT SELECT ON vamojunto.vw_reputacao_motorista TO 'vamojunto_leitura'@'localhost';
 
FLUSH PRIVILEGES;
 