<?php
/**
 * rest.php
 * contém todas as URLs e regras de negócio do REST FlushOut Solutions
 * 
 * @author John Borrego <jebmarca@gmail.com>
 * @author Manuel Moyano <mnlmoyano@gmail.com>
 */
class rest extends superRest {

   /**
    * @var array os métodos que podem ser chamados por URL
    */		
	protected $permitidos = array('login','logout','applicence','getapp','location','location_imei','location_track','location_user','submit_apps', 'reset_apps','get_apps', 'send_data', 'get_data','post_data','is_logged','add_hash','get_hash','is_hash_used','send_pessoa','list_pessoas','v2_send_data','check_version');
	

	// Teste para o lucas

	public function send_pessoa()
	{
		if (!$this->vars['_id']
			 or !$this->vars['nome']
			 or !$this->vars['email']
			 or !$this->vars['cep']) {
				 
			// se os dados não foram passados, informe o erro
			$this->retorno["status"] = false;
			$this->retorno["error"] = "106";
			$this->retorno["message"] = "Please send all the 4 parameters to execute this function.";
		} 
		else 
		{
			$superModel = new superModel();

			$sql = "replace into t_pessoas (_id, nome,email, cep) VALUES (".$this->vars['_id'].", '".$this->vars['nome']."', '".$this->vars['email']."', '".$this->vars['cep']."')";

			//echo $sql;
			$res = $superModel->genericQuery($sql);

			if ($res===false)
			{
				$this->retorno['status'] = false;
				$this->retorno['_id'] = $this->vars['_id'];
			}
			else
			{
				$this->retorno['status'] = true;
				$this->retorno['_id'] = $this->vars['_id'];
			}
		}
	}

	public function list_pessoas()
	{
		$superModel = new superModel();
		$sql = "select * from t_pessoas";
		$res = $superModel->genericQuery($sql);

		$this->retorno['status'] = true;
		$this->retorno['results'] = $res;
	}



	public function send_data() {
		// valida se foram enviados todos os dados obrigatórios para a função
		if (!$this->vars['coduser']
			 or !$this->vars['password']
			 or !$this->vars['codapp']
			 or !$this->vars['rowid']
			 or !$this->vars['tablename']
			 or !$this->vars['record']
			 or !$this->vars['datetime']) {
				 
			// se os dados não foram passados, informe o erro
			$this->retorno["status"] = false;
			$this->retorno["error"] = "106";
			$this->retorno["message"] = "Please send all the 6 parameters to execute this function.";
		} else {
			
			// ok, os dados foram enviados via get... posso tentar logar
			$logado = $this->doLogin($this->vars['coduser'],$this->vars['password'],$this->vars['imei']);
			if(!$logado) {
				
				// se não conseguiu logar, pare por aqui
				$this->retorno['status'] = false;
				$this->retorno["error"] = "116";
				$this->retorno["message"] = "Invalid user";
			
			} else {

				// ok, é um usuário logado!
		
				// vamos verificar se os parâmetros estão de acordo
				$erro = false;
				
				if (!$erro) 
				{
					$superModel = new superModel();

					// desserializa o json
					$rawJson = urldecode(utf8_decode($this->vars['record']));
					$jsonRecord = json_decode($rawJson, true);
					if (!$this->vars['thekey']) $thekey = "_id"; 
					else
					{
						unset($jsonRecord['_id']);
						$thekey = $this->vars['thekey'];
					}
					
					//unset($jsonRecord['_id']);
					unset($jsonRecord['_sync']);

					// pega o nome da table
					$table = $this->vars['tablename'];
					$sql_init = "insert ignore into ".$table. " set ";
					$sql_end = "";

					// monta a string sql
					foreach ($jsonRecord as $key => $value) 
					{
						if ($thekey == $key)
						{
							$response = $superModel->genericQuery("select * from ".$table." where fk_user=".$this->vars['coduser']." and ".$thekey."='".$value."'");

							if (count($response)>0)
							{
								$sql_init = "UPDATE ".$table. " set ";
								$sql_end = " WHERE ".$thekey."=".$value;
							}
						}
						$sql_insert.=$key."='".$value."', ";
					}

					$user = $this->vars['coduser'];
					$sql_insert .= "fk_user='$user', ";

					$dt = $this->vars['datetime'];
					$date = addslashes(substr($dt,0,4)."-".substr($dt,4,2)."-".substr($dt,6,2)." ".substr($dt,8,2).":".substr($dt,10,2).":".substr($dt,12,2));

					$sql_insert .= "date_time='$date'";

//echo $sql_init.$sql_insert.$sql_end;					
					
					$res = $superModel->genericQuery($sql_init.$sql_insert.$sql_end);
					// se deu certo, retorna true com o id
					// senão, retorna false com o id
					if ($res===false)
					{
						//printf("Erro: %s\n", $superModel->db->error);
						$this->retorno['status'] = false;
						$this->retorno['rowid'] = $this->vars['rowid'];
						$this->retorno['table'] = $this->vars['tablename'];

						$logerror = "insert into log_error set fk_application=".$this->vars['codapp'].", fk_user=".$this->vars['coduser'].", message='".addslashes($superModel->db->error)."', data_string='".addslashes($rawJson)."'";
						$superModel->genericQuery($logerror);
					}
					else
					{
						$this->retorno['status'] = true;
						$this->retorno['rowid'] = $this->vars['rowid'];
						$this->retorno['table'] = $this->vars['tablename'];

					
						// Inclui nas tabelas do integrador
						$sql_app = "SELECT * FROM application WHERE lk ='".$this->vars['codapp']."'";
						$res_app = $superModel->genericQuery($sql_app);

						$_fk_application = $res_app[0]['_id'];
						$_fk_user = $this->vars['coduser'];
						$_table_name = $this->vars['tablename'];
						$_app_version = $this->vars['appversion'];
						$_capture_version = $this->vars['baseversion'];
						$_app_version = "1";
						$_capture_version = "2.52";
						$_datetime = $date;
						$_record = $rawJson;
						$_record_index = $this->vars['rowid'];

						$res = $superModel->genericQuery("insert ignore into inserts set fk_application='$_fk_application', 
							fk_user='$_fk_user', 
							table_name='$_table_name', 
							app_version='$_app_version', 
							capture_version='$_capture_version', 
							datetime='$_datetime', 
							record='$_record', 
							record_index='$_record_index'");
					}
					
				} else {
					// deu erro de validação, apenas sete status complementar como false
					$this->retorno['status'] = false;
					$this->retorno['rowid'] = $this->vars['rowid'];
					$this->retorno['table'] = $this->vars['tablename'];
				}
			}
		}
	
	}


	public function v2_send_data() {
		// valida se foram enviados todos os dados obrigatórios para a função
		{
			$sql_insert = "";
			//echo "-->1 \n";
			// ok, os dados foram enviados via get... posso tentar logar
			$logado = $this->doLogin($this->vars['coduser'],$this->vars['password']);
			if(!$logado) {
				//echo "---->1.0 \n";
				// se não conseguiu logar, pare por aqui
				$this->retorno['status'] = false;
				$this->retorno["error"] = "116";
				$this->retorno["message"] = "Invalid user";
			
			} else {
				//echo "-->1.1 \n";
				// ok, é um usuário logado!
		
				// vamos verificar se os parâmetros estão de acordo
				$erro = false;
				
				if (!$erro) 
				{
					$superModel = new superModel();

					$superModel->changedBd($this->vars['user'], $this->vars['pass'], $this->vars['appName']);
					// desserializa o json

					$rawJson = urldecode(utf8_decode($this->vars['record']));
					//echo $rawJson."\n";
					$jsonRecord = json_decode($rawJson, true);
					if (!$this->vars['thekey']) $thekey = "_id"; 
					else
					{
						unset($jsonRecord['_id']);
						$thekey = $this->vars['thekey'];
					}
					//echo "-->1.2 \n";
					//unset($jsonRecord['_id']);
					unset($jsonRecord['_sync']);
					//echo $jsonRecord." \n";
					// pega o nome da table
					$table = $this->vars['tablename'];
					$sql_init = "insert ignore into ".$table. " set ";
					$sql_end = "";
					
					echo "--> ".$jsonRecord." \n";
					// monta a string sql
					foreach ($jsonRecord as $key => $value) 
					{
						if ($thekey == $key)
						{
							$response = $superModel->genericQuery("select * from ".$table." where fk_user=".$this->vars['coduser']." and ".$thekey."='".$value."'");

							if (count($response)>0)
							{
								$sql_init = "UPDATE ".$table. " set ";
								$sql_end = " WHERE ".$thekey."=".$value;
							}
						}
						//echo "-->1.3 ".$key."='".$value."', "."\n";
						$sql_insert.=$key."='".$value."', ";
					}
					//echo "-->1.4 \n";
					$user = $this->vars['coduser'];
					$sql_insert .= "mobile_date='$user', ";

					$dt = $this->vars['datetime'];
					$date = addslashes(substr($dt,0,4)."-".substr($dt,4,2)."-".substr($dt,6,2)." ".substr($dt,8,2).":".substr($dt,10,2).":".substr($dt,12,2));

					$sql_insert .= "sync_date='$date'";

//echo $sql_init.$sql_insert.$sql_end;					
					echo "-->1.5 ".$sql_init.$sql_insert.$sql_end."\n";

					$res = $superModel->genericQuery($sql_init.$sql_insert.$sql_end);
					// se deu certo, retorna true com o id
					// senão, retorna false com o id
					if ($res===false)
					{
						//echo "-->1.5.0 \n";
						//printf("Erro: %s\n", $superModel->db->error);
						$this->retorno['status'] = false;
						$this->retorno['rowid'] = $this->vars['rowid'];
						$this->retorno['table'] = $this->vars['tablename'];

						$logerror = "insert into log_error set fk_application=".$this->vars['codapp'].", fk_user=".$this->vars['coduser'].", message='".addslashes($superModel->db->error)."', data_string='".addslashes($rawJson)."'";
						$superModel->genericQuery($logerror);
					}
					else
					{
						//echo "-->1.5.1 \n";
						$this->retorno['status'] = true;
						$this->retorno['rowid'] = $this->vars['rowid'];
						$this->retorno['table'] = $this->vars['tablename'];
						$this->retorno['json'] = $jsonRecord;
						$this->retorno['rawJson'] = $rawJson;
						


						//echo "-->1.5.2 \n";
						// Inclui nas tabelas do integrador
						$sql_app = "SELECT * FROM application WHERE lk ='".$this->vars['codapp']."'";
						$res_app = $superModel->genericQuery($sql_app);

						$_fk_application = $res_app[0]['_id'];
						$_fk_user = $this->vars['coduser'];
						$_table_name = $this->vars['tablename'];
						$_app_version ="1.1";// $this->vars['appversion'];
						$_capture_version = "1.1";// $this->vars['baseversion'];
						$_app_version = "1";
						$_capture_version = "2.52";
						$_datetime = $date;
						$_record = $rawJson;
						$_record_index = $this->vars['rowid'];
						//echo "-->1.5.3 \n";
						$res = $superModel->genericQuery("insert ignore into inserts set fk_application='$_fk_application', 
							fk_user='$_fk_user', 
							table_name='$_table_name', 
							app_version='$_app_version', 
							capture_version='$_capture_version', 
							datetime='$_datetime', 
							record='$_record', 
							record_index='$_record_index'");
						//echo "-->1.5.4 \n";
					}
					
				} else {
					// deu erro de validação, apenas sete status complementar como false
					$this->retorno['status'] = false;
					$this->retorno['rowid'] = $this->vars['rowid'];
					$this->retorno['table'] = $this->vars['tablename'];
				}
			}
		}
	
	}

	public function get_data() {
		// valida se foram enviados todos os dados obrigatórios para a função
		if (!$this->vars['coduser']
			 or !$this->vars['password']
			 or !$this->vars['codapp']
			 or !$this->vars['tablename']
			 or !$this->vars['captureversion']
			 or !$this->vars['appversion']
			 or !$this->vars['requesttype']
			 or !$this->vars['requestparam']) {
				 
			// se os dados não foram passados, informe o erro
			$this->retorno["status"] = false;
			$this->retorno["error"] = "106";
			$this->retorno["message"] = "Please send all the 6 parameters to execute this function.";
		} else {
			// ok, os dados foram enviados via get... posso tentar logar
			$logado = $this->doLogin($this->vars['coduser'],$this->vars['password']);
			if(!$logado) {
				// se não conseguiu logar, pare por aqui
				$this->retorno['status'] = false;
				$this->retorno["error"] = "116";
				$this->retorno["message"] = "Invalid user";
			
			} else {
				// ok, é um usuário logado!
		
				// vamos verificar se os parâmetros estão de acordo
				$erro = false;
				
				if (!$erro) 
				{
					//$request_param_san  = str_replace($request_param_san, "]", "");
					
					$superModel = new superModel();
					$superModel->changedBd($this->vars['user'], $this->vars['pass'], $this->vars['appName']);

					if (strtolower($this->vars["requesttype"])=="s")
					{
						$arrTables = explode(";;", $this->vars["tablename"]);
						$arrUserExclusive = explode(";;", $this->vars["userexclusive"]);
						$arrRequestParam = explode(";;", urldecode($this->vars["requestparam"]));
//print_r($arrRequestParam);

						$counter = 0;
						foreach ($arrTables as $tablename) 
						{
							$sql = "SELECT * FROM ".$tablename." WHERE 1=1 ";

							if ($arrUserExclusive[$counter] && $arrUserExclusive[$counter]=="true")
								$sql .="AND fk_user='".$this->vars["coduser"]."' ";

							if ($arrRequestParam[$counter]!="null")
							{
								$request_param = $arrRequestParam[$counter];
								$request_param_san  = str_replace(array("[","]"), array("",""), $request_param);

								$sql .="AND ". $request_param_san;
							}
//echo "  ".$sql."   ";
						$res = $superModel->genericQuery($sql);

						$this->retorno['results'][$counter][$tablename] = $res;
						$this->retorno['count'] = count($res);
							$counter++;
						}

					}
					elseif (strtolower($this->vars["requesttype"])=="u")
					{
						$update_set = urldecode($this->vars["updateset"]);
						$update_set_san  = str_replace(array("[","]"), array("",""), $update_set);

						$sql = "UPDATE ".$this->vars["tablename"]." SET ".$update_set_san." WHERE 1=1 ";
						
						if ($this->vars["userexclusive"] && $this->vars["userexclusive"]=="true")
							$sql .="AND fk_user='".$this->vars["coduser"]."' ";

						if ($this->vars["requestparam"]!="null")
							$sql .="AND ". $request_param_san;
//echo $sql;
						$res = $superModel->genericQuery($sql);

						$this->retorno['results'][0][$this->vars["tablename"]] = $res;
					}
					

					$this->retorno['status'] = true;

					// Insere os dados no integrador

						$sql_app = "SELECT * FROM application WHERE lk ='".$this->vars['codapp']."'";
						$res_app = $superModel->genericQuery($sql_app);

						$_fk_application = $res_app[0]['_id'];
						$_fk_user = $this->vars['coduser'];
						$_request_type = $this->vars['requesttype'];
						$_request_name = $this->vars['tablename'];
						$_request_param = $request_param;
						$_datetime = date("Y-m-d G:i:s");

						if ($_request_param=="null" && $this->vars["userexclusive"]=="true")
						{
							if ($this->vars['requesttype']=="p")
								$_request_param = $_fk_user;
							else
								$_request_param = "[fk_user]=".$_fk_user;
						}
						elseif ($_request_param!="null" && $this->vars["userexclusive"]=="true")
						{
							if ($this->vars['requesttype']=="p")
								$_request_param .= ";".$_fk_user;
							else
								$_request_param .= " AND [fk_user]=".$_fk_user;
						}
						
						$res = $superModel->genericQuery("insert ignore into request set 
							fk_application='$_fk_application', 
							fk_user='$_fk_user', 
							request_type='$_request_type', 
							request_name='$_request_name', 
							request_param='$_request_param', 
							datetime='$_datetime'");
/*
						echo "insert into request set 
							fk_application='$_fk_application', 
							fk_user='$_fk_user', 
							request_type='$_request_type', 
							request_param='$_request_param', 
							datetime='$_datetime'";*/

				} else {
					// deu erro de validação, apenas sete status complementar como false
					$this->retorno['status'] = false;
				}
			}
		}
	
	}

	public function post_data() 
	{
		{
			// ok, os dados foram enviados via get... posso tentar logar
			$logado = $this->doLogin($this->vars['__coduser'],$this->vars['__password'],"0");
			if(!$logado) {
				
				// se não conseguiu logar, pare por aqui
				$this->retorno['status'] = false;
				$this->retorno["error"] = "116";
				$this->retorno["message"] = "Invalid user";
			
			} 
			else 
			{
			
				// ok, é um usuário logado!
		
				// vamos verificar se os parâmetros estão de acordo
				$erro = false;
				
				if (!$erro) 
				{
					$sql = "INSERT IGNORE INTO ".$this->vars['__tablename']." SET ";

					$values = array();

					if ($this->vars['__user_exclusive'] == "true") $values['fk_user'] = $this->vars['__coduser'];

					foreach ($this->vars as $key => $value) 
					{
						if (substr($key, 0,2)!="__") 
						{
							$values[$key] = "'".$value."'";
						}
					}

					$superModel = new superModel();
					$res_app = $superModel->insertInto($this->vars['__tablename'], $values);
					
					if ($res_app) 
					{
						$this->retorno['status'] = true;
						$this->retorno['rowid'] = $res_app;
					}
				} 
				else 
				{
					// deu erro de validação, apenas sete status complementar como false
					$this->retorno['status'] = false;
				}
			}
		}
	
	}

	// is_logged/coduser/123/codapp/2134
	// return {"status":true}
	public function is_logged()
	{
		if (!$this->vars['coduser']
			 or !$this->vars['codapp']) 
		{
				 
			// se os dados não foram passados, informe o erro
			$this->retorno["status"] = false;
			$this->retorno["error"] = "106";
			$this->retorno["message"] = "Please send all the 2 parameters to execute this function.";
		} else 
		{
			$superModel = new superModel();

			$sql_idusr = "SELECT * FROM user WHERE cod_ac ='".$this->vars['coduser']."'";
			$res_idusr = $superModel->genericQuery($sql_idusr);

			$sql_login = "SELECT * FROM login WHERE fk_user=".$res_idusr[0]['_id']." AND fk_application=".$this->vars['codapp']." ORDER BY date_login DESC limit 1";
			$sql_logout = "SELECT * FROM logout WHERE fk_user=".$res_idusr[0]['_id']." AND fk_application=".$this->vars['codapp']." ORDER BY date_logout DESC limit 1";
					
			
			$res_in = $superModel->genericQuery($sql_login);
			$res_out = $superModel->genericQuery($sql_logout);


			if (count($res_in)==1)
			{
				if (count($res_out)==1)
				{
					$data_login = $res_in[0]['date_login'];
					$data_logout = $res_out[0]['date_logout'];
					
					if ($data_login>$data_logout)
						$this->retorno['status'] = true;
					else
						$this->retorno['status'] = false;
				}
				else
				{
					$this->retorno['status'] = true;
				}
			}
			else
			{
				$this->retorno['status'] = false;
			}
		}
	}

	// add_hash/coduser/123/lk/2134/hash/HHASD
	function add_hash()
	{
		if (!$this->vars['coduser']
			 or !$this->vars['codapp']
			 or !$this->vars['hash']) 
		{
				 
			// se os dados não foram passados, informe o erro
			$this->retorno["status"] = false;
			$this->retorno["error"] = "106";
			$this->retorno["message"] = "Please send all the 3 parameters to execute this function.";
		} else 
		{
			$logout_hash = new logout_hash();
			$logout_hash->revokeAll($this->vars['coduser'],$this->vars['codapp']);
			$id = $logout_hash->registrar($this->vars['coduser'],$this->vars['codapp'],$this->vars['hash']);

			if($id) {
				
				$this->retorno['status'] = true;				
			} else {
				// não incluiu
				$this->retorno['status'] = false;				
			}
		}
	}

	// get_hash/coduser/123/codapp/2134/hash/HHASD
	function get_hash()
	{
		if (!$this->vars['coduser']
			 or !$this->vars['codapp']
			 or !$this->vars['hash']) 
		{
				 
			// se os dados não foram passados, informe o erro
			$this->retorno["status"] = false;
			$this->retorno["error"] = "106";
			$this->retorno["message"] = "Please send all the 3 parameters to execute this function.";
		} else 
		{
			$logout_hash = new logout_hash();
			$id = $logout_hash->checkHash($this->vars['coduser'],$this->vars['codapp'],$this->vars['hash']);
			if($id) {
				
				$this->retorno['status'] = true;				
			} else {
				// não incluiu
				$this->retorno['status'] = false;				
			}
		}
	}

	// is_hash_used/coduser/123/codapp/2134/hash/HHASD
	function is_hash_used()
	{
		if (!$this->vars['coduser']
			 or !$this->vars['codapp']
			 or !$this->vars['hash']) 
		{
				 
			// se os dados não foram passados, informe o erro
			$this->retorno["status"] = false;
			$this->retorno["error"] = "106";
			$this->retorno["message"] = "Please send all the 3 parameters to execute this function.";
		} else 
		{
			$logout_hash = new logout_hash();
			$id = $logout_hash->checkUsed($this->vars['coduser'],$this->vars['codapp'],$this->vars['hash']);
			if($id) {
				
				$this->retorno['status'] = true;				
			} else {
				// não incluiu
				$this->retorno['status'] = false;				
			}
		}
	}


	/**
	 * Autenticação do usuário da aplicação.
	 * Informa quantidadade de apps do usuário
	 * Se for apenas um app, informa o código do app
	 * 
	 * @return void
	 */		
	public function login() {		
		// valida se foram enviados todos os dados para login
		if (!$this->vars['coduser'] or !$this->vars['password'] or !$this->vars['imei']) {
			// se os dados não foram passados, informe o erro
			$this->retorno["status"] = false;
			$this->retorno["error"] = "102";
			$this->retorno["message"] = "Please send coduser and password for login.";			
		} else {
			
			// ok, os dados foram enviados via get... posso tentar logar
			$logado = $this->doLogin($this->vars['coduser'],$this->vars['password'],$this->vars['imei'],$this->vars['latitude'],$this->vars['longitude']);

			// valida se usuário existe ou NOT
			if($logado) {

				// SE existe
					
				//verifica quantas apps o user tem
				$apps = new user_application();
				$appsData = $apps->getApplications($this->vars['coduser']);
				
				if(count($appsData)==0) {
					
					// SE usuario não tem apps
					$this->retorno['status'] = true;
					$this->retorno['applications'] = 0;
			
				} else if(count($appsData)==1) {
			
					// SE tem uma app só
					$this->retorno['status'] = true;
					$this->retorno['applications'] = 1;
					$this->retorno['appcode'] = $appsData[0]['lk'];
			
				} else {	
			
					// SE tem mais de uma app
					$this->retorno['status'] = true;
					$this->retorno['applications'] = count($appsData);
					
				}
			} else {
				// SE não existe
				$this->retorno['status'] = false;
				$this->retorno['applications'] = 0;
			}
		}
	}




	public function logout() 
	{
		// valida se foram enviados todos os dados obrigatórios para a função
		if (!$this->vars['coduser']
			or !$this->vars['codapp']
			or !$this->vars['imei']
			or !$this->vars['latitude']
			or !$this->vars['longitude']) {
				 
			// se os dados não foram passados, informe o erro
			$this->retorno["status"] = false;
			$this->retorno["error"] = "105";
			$this->retorno["message"] = "Please send all the 5 parameters to execute this function.";
		} 
		else 
		{
			// vamos verificar se os parâmetros estão de acordo
			$erro = false;
				

			if (!$erro) {
					
				// vamos incluir a localização
				// monta a classe de location e inclui
				$logout = new logout();
				$id = $logout->deslogarAcesso($this->vars['coduser'],$this->vars['latitude'],$this->vars['longitude'],$this->vars['codapp'],$this->vars['imei']);

				// verifica se incluiu corretamente
				if($id) {
					// incluiu
					$this->retorno['status'] = true;				
				} else {
					// não incluiu
					$this->retorno['status'] = false;				
				}
					
			} 
			else {
				// deu erro de validação, apenas sete status complementar como false
				$this->retorno['status'] = false;				
			}
		}
	
	}





	
	/**
	 * Informa um número de licença para um app
	 * 
	 * @return void
	 */		
	public function applicence() {
		// valida se foram enviados todos os dados obrigatórios para a função
		if (!$this->vars['coduser'] or !$this->vars['password'] or !$this->vars['imei'] or !$this->vars['licencekey']) {
			// se os dados não foram passados, informe o erro
			$this->retorno["status"] = false;
			$this->retorno["error"] = "104";
			$this->retorno["message"] = "Please send coduser, password and licencekey parameters.";			
		} else {
			
			// ok, os dados foram enviados via get... posso tentar logar
			$logado = $this->doLogin($this->vars['coduser'],$this->vars['password'],$this->vars['imei']);
			if(!$logado) {
				
				// se não conseguiu logar, pare por aqui
				$this->retorno['status'] = false;
			
			} else {
			
				// ok, é um usuário logado!
				
				// verifique se ele tem acessi a esta aplicação
				$userapp = new user_application();
				$status = $userapp->checkLicenceKey($this->vars['coduser'],$this->vars['licencekey']);
				
				$appl = new application();
				$appid = $appl->getAppID($this->vars['licencekey']);

				$doLogado = $this->doLogin($this->vars['coduser'],$this->vars['password'],$this->vars['imei'],$this->vars['latitude'],$this->vars['longitude'],$appid,true);

				// retorna status na tela
				$this->retorno['status'] = (bool)$status;
			
			}
		}
	}
	
	/**
	 * Informa um path para download de um app
	 * 
	 * @return void
	 */			
	public function getapp() {
		// valida se foram enviados todos os dados obrigatórios para a função
		if (!$this->vars['coduser'] or !$this->vars['password'] or !$this->vars['imei'] or !$this->vars['codapp']) {
			// se os dados não foram passados, informe o erro
			$this->retorno["status"] = false;
			$this->retorno["error"] = "105";
			$this->retorno["message"] = "Please send coduser, password and codapp parameters.";			
		} else {
			
			// ok, os dados foram enviados via get... posso tentar logar
			$logado = $this->doLogin($this->vars['coduser'],$this->vars['password'],$this->vars['imei']);
			if(!$logado) {
				
				// se não conseguiu logar, pare por aqui
				$this->retorno['status'] = false;
				$this->retorno['applications'] = 0;
			
			} else {
			
				// ok, é um usuário logado!
				
				// Pego a aplicação
					$userapp = new user_application();
					$path = $userapp->getPath($this->vars['coduser'],$this->vars['codapp']);
				
					if(!$path) {
						// SE app não foi encontrado
						$this->retorno['status'] = false;
					} else {
						// SE app foi encontrado
								
						// guarde na tabela de logs
						$log = new download_log();
						$log->logarDownload($this->vars['coduser'],$this->vars['codapp']);						
						
						// retorne na tela
						$this->retorno['status'] = true;
						$this->retorno['path'] = $path;
					}
			}
		}
	
	}
	
	/**
	 * Registro de localização do aparelho enquanto a aplicação estiver aberta baseado no imei.
	 * 
	 * @return void
	 */			
	public function location_imei() 
	{
		// valida se foram enviados todos os dados obrigatórios para a função
		if (!$this->vars['id']
			or !$this->vars['imei']
			or !$this->vars['lat']
			or !$this->vars['lon']
			or !$this->vars['speed']
			or !$this->vars['phonenumber']
			or !$this->vars['bearing']
			or !$this->vars['accuracy']
			or !$this->vars['batterylevel']
			or !$this->vars['gsmstrength']
			or !$this->vars['carrier']
			or !$this->vars['datetime']) {
				 
			// se os dados não foram passados, informe o erro
			$this->retorno["status"] = false;
			$this->retorno["error"] = "106";
			$this->retorno["message"] = "Please send all the 6 parameters to execute this function.";
		} 
		else 
		{
			// vamos verificar se os parâmetros estão de acordo
			$erro = false;
				
			if(!(float)$this->vars['lat'] > 0 or !$this->isDecimalNumber($this->vars['lat'])) {
				$this->retorno["error"] = "107";
				$this->retorno["message"] = "lat need to be a valid float.";							
				$erro = true;	
			}

			if(!(float)$this->vars['lon'] > 0 or !$this->isDecimalNumber($this->vars['lon'])) {
				$this->retorno["error"] = "108";
				$this->retorno["message"] = "lon need to be a valid float.";								
				$erro = true;	
			}
				
			if(!$this->checkDateTime($this->vars['datetime'])) {
				$this->retorno["error"] = "110";
				$this->retorno["message"] = "datetime is not in a valid format.";								
				$erro = true;	
			}
				
			if (!$erro) {
					
				// vamos incluir a localização
				// monta a classe de location e inclui
				$location_imei = new location_imei();
				$id = $location_imei->registrar($this->vars['imei'],
												$this->vars['lat'],
												$this->vars['lon'],
												$this->vars['speed'],
												$this->vars['datetime'],
												$this->vars['phonenumber'],
												$this->vars['bearing'],
												$this->vars['accuracy'],
												$this->vars['batterylevel'],
												$this->vars['gsmstrength'],
												$this->vars['carrier']);
					

				// verifica se incluiu corretamente
				if($id) {
					// incluiu
					$this->retorno['id'] = $this->vars['id'];	
					$this->retorno['status'] = true;				
				} else {
					// não incluiu
					$this->retorno['status'] = false;				
				}
					
			} 
			else {
				// deu erro de validação, apenas sete status complementar como false
				$this->retorno['status'] = false;				
			}
		}
	
	}

	/**
	 * Registro de localização do aparelho enquanto a aplicação estiver aberta baseado no imei.
	 * 
	 * @return void
	 */			
	public function location_track() 
	{
		// valida se foram enviados todos os dados obrigatórios para a função
		if (!$this->vars['imei']
			or !$this->vars['datestart']
			or !$this->vars['dateend']
			or !$this->vars['mode'] // 'full' or 'simple' 
			) {
				 
			// se os dados não foram passados, informe o erro
			$this->retorno["status"] = false;
			$this->retorno["error"] = "106";
			$this->retorno["message"] = "Please send all the 4 parameters to execute this function.";
		} 
		else 
		{
			// vamos verificar se os parâmetros estão de acordo
			$erro = false;

			if(!$this->checkDateTime($this->vars['datestart'])) {
				$this->retorno["error"] = "110";
				$this->retorno["message"] = "datestart is not in a valid format.";								
				$erro = true;	
			}
				
			if(!$this->checkDateTime($this->vars['dateend'])) {
				$this->retorno["error"] = "110";
				$this->retorno["message"] = "dateend is not in a valid format.";								
				$erro = true;	
			}
				
			if (!$erro) {
					
				// vamos incluir a localização
				// monta a classe de location e inclui
				$location_imei = new location_imei();

				if ($this->vars['mode']=="full")
					$result = $location_imei->getTrack($this->vars['imei'], $this->vars['datestart'], $this->vars['dateend']);
				else if ($this->vars['mode']=="simple")
					$result = $location_imei->getTrackSimple($this->vars['imei'], $this->vars['datestart'], $this->vars['dateend']);
				else if ($this->vars['mode']=="waypoints")
				{
					$rs = $location_imei->getTrackSimple($this->vars['imei'], $this->vars['datestart'], $this->vars['dateend']);

					$result = array();

					foreach ($rs as $item)
					{
						$objArr['location'] = $item['latitude'].','.$item['longitude'];
						$objArr['stopover']=false;

						$result[] = $objArr;
					}
					
				}

				// verifica se incluiu corretamente

				if ($this->vars['mode']=="full" || $this->vars['mode']=="simple")
				{
					if($result) {
						// incluiu
						$this->retorno['coords'] = $result;	
						$this->retorno['status'] = true;				
					} else {
						// não incluiu
						$this->retorno['status'] = false;				
					}
				}
				else
				{
					$this->retorno['coords'] = $result;	
				}
					
			} 
			else {
				// deu erro de validação, apenas sete status complementar como false
				$this->retorno['status'] = false;				
			}
		}
	
	}


	public function location_user() 
	{
		// valida se foram enviados todos os dados obrigatórios para a função
		if (!$this->vars['imei']) {
				 
			// se os dados não foram passados, informe o erro
			$this->retorno["status"] = false;
			$this->retorno["error"] = "106";
			$this->retorno["message"] = "IMEI missing.";
		} 
		else 
		{
			// vamos verificar se os parâmetros estão de acordo
			$erro = false;

			if (!$erro) {
					
				// vamos incluir a localização
				// monta a classe de location e inclui
				$location_imei = new location_imei();
				$result = $location_imei->getUserPosition($this->vars['imei']);

				if($result) {
					$this->retorno['coords'] = $result;	
					$this->retorno['status'] = true;
				}
				else
				{
					$this->retorno['status'] = false;
				}					
			} 
			else {
				// deu erro de validação, apenas sete status complementar como false
				$this->retorno['status'] = false;				
			}
		}
	
	}


	/**
	 * Registro de localização do aparelho enquanto a aplicação estiver aberta.
	 * 
	 * @return void
	 */			
	public function location() {
		// valida se foram enviados todos os dados obrigatórios para a função
		if (!$this->vars['coduser']
			 or !$this->vars['password']
			 or !$this->vars['imei']
			 or !$this->vars['lat']
			 or !$this->vars['lon']
			 or !$this->vars['speed']
			 or !$this->vars['datetime']
			 or !$this->vars['speed']
			or !$this->vars['phonenumber']
			or !$this->vars['bearing']
			or !$this->vars['accuracy']
			or !$this->vars['batterylevel']
			or !$this->vars['gsmstrength']
			or !$this->vars['carrier']
			or !$this->vars['codapp']) {
				 
			// se os dados não foram passados, informe o erro
			$this->retorno["status"] = false;
			$this->retorno["error"] = "106";
			$this->retorno["message"] = "Please send all the 6 parameters to execute this function.";			
		} else {
			
			// ok, os dados foram enviados via get... posso tentar logar
			$logado = $this->doLogin($this->vars['coduser'],$this->vars['password'],$this->vars['imei']);
			if(!$logado) {
				
				// se não conseguiu logar, pare por aqui
				$this->retorno['status'] = false;
				$this->retorno['applications'] = 0;
			
			} else {
			
				// ok, é um usuário logado!
		
				// vamos verificar se os parâmetros estão de acordo
				$erro = false;
				/*
				if(!(float)$this->vars['lat'] > 0 or !$this->isDecimalNumber($this->vars['lat'])) {
					$this->retorno["error"] = "107";
					$this->retorno["message"] = "lat need to be a valid float.";							
					$erro = true;	
				}

				if(!(float)$this->vars['lon'] > 0 or !$this->isDecimalNumber($this->vars['lon'])) {
					$this->retorno["error"] = "108";
					$this->retorno["message"] = "lon need to be a valid float.";								
					$erro = true;	
				}
	*/
				if(!$this->checkDateTime($this->vars['datetime'])) {
					$this->retorno["error"] = "110";
					$this->retorno["message"] = "datetime is not in a valid format.";								
					$erro = true;	
				}
				
				$this->vars['lat'] = floatval(number_format($this->vars['lat'], 6, '.', ''));
				$this->vars['lon'] = floatval(number_format($this->vars['lon'], 6, '.', ''));

				if(!(float)$this->vars['lat'] != 0 or !$this->isDecimalNumber($this->vars['lat'])) {
				$this->retorno["error"] = "107";
				$this->retorno["message"] = "latitude need to be a valid float.";							
				$erro = true;	
				}

				if(!(float)$this->vars['lon'] != 0 or !$this->isDecimalNumber($this->vars['lon'])) {
					$this->retorno["error"] = "108";
					$this->retorno["message"] = "longitude need to be a valid float.";								
					$erro = true;	
				}

				if (!$erro) {
					
					// vamos incluir a localização
					// monta a classe de location e inclui
					$location = new location();
					
					$id = $location->registrar(	$this->vars['coduser'],
												$this->vars['codapp'],
												$this->vars['lat'],
												$this->vars['lon'],
												$this->vars['speed'],
												$this->vars['datetime'],
												$this->vars['phonenumber'],
												$this->vars['bearing'],
												$this->vars['accuracy'],
												$this->vars['batterylevel'],
												$this->vars['gsmstrength'],
												$this->vars['carrier']
												);
					
					// verifica se incluiu corretamente
					if($id) {
						// incluiu
						$this->retorno['status'] = true;				
						$this->retorno['id'] = $this->vars['id'];	
					} else {
						// não incluiu
						$this->retorno['status'] = false;				
					}
					
				} else {
					// deu erro de validação, apenas sete status complementar como false
					$this->retorno['status'] = false;				
				}
			}
		}
	
	}


	/**
	 * Envio de aplicativos instalados no celular
	 * 
	 * @return void
	 */			
	public function submit_apps() 
	{
		// valida se foram enviados todos os dados obrigatórios para a função
		if (!$this->vars['imei']
			or !$this->vars['package']
			or !$this->vars['name']
			) {
				 
			// se os dados não foram passados, informe o erro
			$this->retorno["status"] = false;
			$this->retorno["error"] = "103";
			$this->retorno["message"] = "Please send all the 3 parameters to execute this function.";
		} 
		else 
		{
			// vamos verificar se os parâmetros estão de acordo
			$erro = false;
				
			if (!$erro) {
					
				// vamos incluir a localização
				// monta a classe de location e inclui
				$locker_apps = new locker_apps();
				$id = $locker_apps->registrar($this->vars['imei'],
												$this->vars['package'],
												$this->vars['name']);
					
				// verifica se incluiu corretamente
				if($id) {
					// incluiu
					$this->retorno['status'] = true;				
				} else {
					// não incluiu
					$this->retorno['status'] = false;				
				}
					
			} 
			else {
				// deu erro de validação, apenas sete status complementar como false
				$this->retorno['status'] = false;				
			}
		}
	
	}

	/**
	 * Resesta os apps instalados no celular
	 * 
	 * @return void
	 */			
	public function reset_apps() 
	{
		// valida se foram enviados todos os dados obrigatórios para a função
		if (!$this->vars['imei']) {
				 
			// se os dados não foram passados, informe o erro
			$this->retorno["status"] = false;
			$this->retorno["error"] = "119";
			$this->retorno["message"] = "Please send the IMEI parameter to execute this function.";
		} 
		else 
		{
			// vamos verificar se os parâmetros estão de acordo
			$erro = false;
				
			if (!$erro) 
			{
				$locker_apps = new locker_apps();
				$id = $locker_apps->resetApps($this->vars['imei']);					
				
				$this->retorno['status'] = true;
			} 
			else {
				// deu erro de validação, apenas sete status complementar como false
				$this->retorno['status'] = false;				
			}
		}
	
	}


	/**
	 * Registro de localização do aparelho enquanto a aplicação estiver aberta baseado no imei.
	 * 
	 * @return void
	 */			
	public function get_apps() 
	{
		// valida se foram enviados todos os dados obrigatórios para a função
		if (!$this->vars['imei']) {
				 
			// se os dados não foram passados, informe o erro
			$this->retorno["status"] = false;
			$this->retorno["error"] = "119";
			$this->retorno["message"] = "Please send the IMEI parameter to execute this function.";
		} 
		else 
		{
			// vamos verificar se os parâmetros estão de acordo
			$erro = false;

			if (!$erro) {
					
				$locker_apps = new locker_apps();
				$this->retorno['apps'] = $locker_apps->getAllowedApps($this->vars['imei']);
			} 
			else {
				// deu erro de validação, apenas sete status complementar como false
				$this->retorno['status'] = false;				
			}
		}
	
	}



	/**
	 * Função genérica para o encapsulamento de login
	 * 
	 * @return bool
	 */			
	private function doLogin($puser,$pwd,$imei="",$lat=0,$long=0,$app=null,$log=false) {

		$user = new user();
		//$userId = $user->getIdByEmail($puser);
		return $user->login($puser,$pwd);

/*		// carrega a model de usuários
		$user = new user();
		
		// pega no BD o password do usuário
		$dataSet = $user->getSenha($puser);
		
		// se não encontrou o usuário, retorne não-logado
		if(!count($dataSet)) {
			return false;
		}
		
		// testa se password enviado é igual ao do BD
		if($dataSet[0]['password']==md5($pwd)) {
			
			if ($dataSet[0]['imei']=="0" || $dataSet[0]['imei']==$imei)
			{
				// verifica se a companhia está ativa
				$company = new company();
				$status = $company->checkCompanyStatus($dataSet[0]['fk_company']);
				
				if ($status) {
					
					// grava log de login
					if ($log)
					{
						$login = new login();
						$login->logarAcesso($this->vars['coduser'],$lat,$long,$app);	
					}
					
					return true;
				} else {
					return false;
				}
			}
			else
			{
				return false;
			}

			
		} else {
			return false;
		}*/
	}


	public function check_version(){

		if (!$this->vars['coduser']
			 or !$this->vars['password']
			 or !$this->vars['tablename']
			 or !$this->vars['version']
			 or !$this->vars['user']
			 or !$this->vars['pass']
			 or !$this->vars['appName']) {
			// se os dados não foram passados, informe o erro
			$this->retorno["status"] = false;
			$this->retorno["error"] = "106";
			$this->retorno["message"] = "Please send all the 5 parameters to execute this function.";
		} else {
			$logado = $this->doLogin($this->vars['coduser'],$this->vars['password']);
			if(!$logado) {
				// se não conseguiu logar, pare por aqui
				$this->retorno['status'] = false;
				$this->retorno["error"] = "116";
				$this->retorno["message"] = "Invalid user";
			
			} else {

				$versionMaster = new versionMaster();

				$superModel = new superModel();
				$superModel->changedBd($this->vars['user'], $this->vars['pass'], $this->vars['appName']);

				$newVersion = $superModel->genericQuery("select version from version_masters where table_name = '".$this->vars['tablename']."' and version > ".$this->vars['version']);
				
				if (!$newVersion) {
					$this->retorno['status'] = false;
				} else {
					$this->retorno['status'] = true;
					$this->retorno['version'] = $newVersion[0]['version'];
				}
			}
		}
	}


}
?>