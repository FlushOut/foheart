<?php
/**
 * v1servico_status.php, model para a tabela v1servicos_status
 * 
 * @author John Borrego <jebmarca@gmail.com>
 * @author Manuel Moyano <mnlmoyano@gmail.com>
 */
class superModelDev {
	
   /**
    * @var array configura��o da aplica��o
    */		
	protected $config;
	
   /**
    * @var bool guarda a configura��o de debug
    */		
	protected $debug;
	
   /**
    * @var object inst�ncia do classe mysqli
    */		 
	protected $db;
	
	
	
	/**
	 * Construtor da classe
	 * Carrega a configura��o e conecta no BD
	 * 
	 * @return void
	 */		
	public function __construct() {
		
		// pega as configura��es definidas em config.php
		global $config;
		$this->config = $config;
		
		// verifica se est� em debug
		$this->debug = $this->config['debug'];

		// conecta ao banco de dados
		$this->db = new MySQLi($this->config['bd']['host'],$this->config['bd']['user'],$this->config['bd']['password'],$this->config['bd']['base']."_dev",$this->config['bd']['port']);
	}
	
	/**
	 * M�todo gen�rico para fazer SELECTs nas tabelas
	 * 
	 * @param string $columns colunas que entrar�o no SELECT
	 * @param array $where argumentos do WHERE que ser�o concatenados com AND
	 * @return array
	 */		
	public function select($columns="*",$where="") {
		
		// monte b�sico da query
		$query = "SELECT ".$this->db->real_escape_string($columns)." FROM ".$this->table;

		// Se veio alguma condi��o, monte no where
		if(count($where)>0) {
			$i = 0;
			foreach($where as $item) {
				
				// concatenador
				$query.=(($i==0)?" WHERE ":" AND ");
				$i = 1;
				
				// condi��o
				$query.=$item;
				
			}
		}		
		//print_r($query);
		$resultSet = $this->db->query($query);
		if(is_object($resultSet)) {
		//	$obj = $resultSet->fetch_all(MYSQLI_ASSOC);
			$obj = array();
			while ($row = $resultSet->fetch_assoc()) {
			  $obj[] = $row;
			}
			return $obj;
		} else {
			return null;
		}
	}

	/**
	 * M�todo gen�rico para fazer INSERTs nas tabelas
	 * 
	 * @param array $values colunas e valores que ser�o usados no INSERT
	 * @return int ID inserido
	 */			
	public function insert($values) {
	
		$colunasSQL = null;
		$valoresSQL = null;

		foreach($values as $key => $value) {
			
			$colunasSQL.=($colunasSQL?',':'');
			$colunasSQL.=$key;

			$valoresSQL.=($valoresSQL?',':'');
			$valoresSQL.=$value;
			
		}
		
		$query = "INSERT INTO ".$this->table. " (".$colunasSQL.") VALUES (".$valoresSQL.")";


//		print_r($query);


		$this->db->query($query);
		return $this->db->insert_id;
	}
	
	/**
	 * M�todo gen�rico para executar qualquer query (preferencialmente SELECTs)
	 * 
	 * @param string $query SQL query completa
	 * @return array
	 */		
	public function genericQuery($query) {
		
		$resultSet = $this->db->query($query);

		if(is_object($resultSet)) {
		//	$obj = $resultSet->fetch_all(MYSQLI_ASSOC);
			$obj = array();
			while ($row = $resultSet->fetch_assoc()) {
			  $obj[] = $row;
			}
			return $obj;
		} else {
			return null;
		}
	}
}