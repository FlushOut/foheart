<?php
/**
 * v1servico_status.php, model para a tabela v1servicos_status
 * 
 * @author John Borrego <jebmarca@gmail.com>
 * @author Manuel Moyano <mnlmoyano@gmail.com>
 */
class superModel {

	protected $config;
	protected $debug; 
	protected $db;
	
	
	
	/**
	 * Construtor da classe
	 * Carrega a configuração e conecta no BD
	 * 
	 * @return void
	 */		
	public function __construct() {
		
		// pega as configurações definidas em config.php
		global $config;
		$this->config = $config;
		
		// verifica se está em debug
		$this->debug = $this->config['debug'];

		// conecta ao banco de dados
		$this->db = new MySQLi($this->config['bd']['host'],$this->config['bd']['user'],$this->config['bd']['password'],$this->config['bd']['base'],$this->config['bd']['port']);
	}
	
	public function changedBd($user, $pass, $base){

	    $this->config['bd']['user'] = $user;
	    $this->config['bd']['password'] = $pass;
	    $this->config['bd']['base'] = str_replace("fo","db",strtolower($base));

		// conecta ao banco de dados
		$this->db = new MySQLi($this->config['bd']['host'],$this->config['bd']['user'],$this->config['bd']['password'],$this->config['bd']['base'],$this->config['bd']['port']);
	}

	/**
	 * Método genérico para fazer SELECTs nas tabelas
	 * 
	 * @param string $columns colunas que entrarão no SELECT
	 * @param array $where argumentos do WHERE que serão concatenados com AND
	 * @return array
	 */		
	public function select($columns="*",$where="") {
		
		// monte básico da query
		$query = "SELECT ".$this->db->real_escape_string($columns)." FROM ".$this->table;
		
		// Se veio alguma condição, monte no where
		if(count($where)>0) {
			$i = 0;
			foreach($where as $item) {
				
				// concatenador
				$query.=(($i==0)?" WHERE ":" AND ");
				$i = 1;
				
				// condição
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
	 * Método genérico para fazer INSERTs nas tabelas
	 * 
	 * @param array $values colunas e valores que serão usados no INSERT
	 * @return int ID inserido
	 */			
	public function insert($values) {
	
		$colunasSQL = null;
		$valoresSQL = null;

		foreach($values as $key => $value) {
			
			$colunasSQL.=($colunasSQL?',':'');
			$colunasSQL.="`".$key."`";

			$valoresSQL.=($valoresSQL?',':'');
			$valoresSQL.=$value;
			
		}
		
		$query = "INSERT INTO ".$this->table. " (".$colunasSQL.") VALUES (".$valoresSQL.")";

		$this->db->query($query);
		return $this->db->insert_id;
	}

	public function insertTable($values,$EspecificTable) {
	
		$colunasSQL = null;
		$valoresSQL = null;

		foreach($values as $key => $value) {
			
			$colunasSQL.=($colunasSQL?',':'');
			$colunasSQL.="`".$key."`";

			$valoresSQL.=($valoresSQL?',':'');
			$valoresSQL.=$value;
			
		}
		

		$query = "INSERT INTO ".$EspecificTable. " (".$colunasSQL.") VALUES (".$valoresSQL.")";
		$this->db->query($query);
		return $this->db->insert_id;
	}
	
	/**
	 * Método genérico para executar qualquer query (preferencialmente SELECTs)
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