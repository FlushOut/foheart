<?php
/**
 * rest.php
 * Contiene todas las URL y regas de neogocio para FOHeart FlushOut Solutions
 * 
 * @author John Borrego <jebmarca@gmail.com>
 * @author Manuel Moyano <mnlmoyano@gmail.com>
 */
class rest extends superRest {

   /**
    * @var Array de metodos que pueden ser llamados por el servicio
    */		
	protected $permitidos = array('login','logout','loginApp','logoutApp','getApps');
	

	/**
	 * Login de la aplicación, primero verifica la contraseña
	 * y retorna las aplicaciones asignadas al usuario
	 * 
	 * 
	 * @return void
	 */		
	public function login() {		

		if (!$this->vars['email'] or !$this->vars['password']) {
			$this->retorno["status"] = false;
			$this->retorno["error"] = "102";
			$this->retorno["message"] = "Please send email, password for login.";			
		} else {
			$user = new user();
			$userId = $user->getIdByEmail($this->vars['email']);
			$result = $user->login($userId[0]['_id'],$this->vars['password']);
			if ($result) {
				$apps = new user_apps();
				$appsData = $apps->getAppsByUser($userId[0]['_id']);
				if (count($appsData) == 0) {
					$this->retorno['status'] = true;
					$this->retorno['appscount'] = 0;
					$this->retorno["message"] = "The user have not applications";	
				} else {
					$resultApps = $apps->login($userId[0]['_id']);
					if ($resultApps) {
						$this->retorno['status'] = true;
						$this->retorno['userId'] = $userId[0]['_id'];
						$this->retorno['appscount'] = count($appsData);
						$this->retorno['appcodes'] = $appsData;
					} else {
						$this->retorno['status'] = false;
						$this->retorno["message"] = "Cannot login for apps";	
					}
				}
			} else {
				$this->retorno['status'] = false;
				$this->retorno["message"] = "Cannot login for user";
			}
		}
	}

	public function logout() {

		if (!$this->vars['email']) {
			$this->retorno["status"] = false;
			$this->retorno["error"] = "102";
			$this->retorno["message"] = "Please send email";
		} else {
			$user = new user();
			$userId = $user->getIdByEmail($this->vars['email']);
			$apps = new user_apps();
			$result = $apps->logout($userId[0]['_id']);
			if($result) {
				$this->retorno['status'] = true;				
			} else {
				$this->retorno['status'] = false;
				$this->retorno["message"] = "Cannot logout for user";				
			}		
		} 
	}

	public function loginApp() {		

		if (!$this->vars['email'] or !$this->vars['appcode']) {
			$this->retorno["status"] = false;
			$this->retorno["error"] = "102";
			$this->retorno["message"] = "Please send email and appcode for login.";			
		} else {
			$user = new user();
			$userId = $user->getIdByEmail($this->vars['email']);
			$apps = new user_apps();
			$resultApp = $apps->login($userId[0]['_id'],$this->vars['appcode']);
			if ($resultApp) {
				$this->retorno['status'] = true;
			} else {
				$this->retorno['status'] = false;
				$this->retorno["message"] = "Cannot login for app";	
			}
		}
	}

	public function logoutApp() {		

		if (!$this->vars['email'] or !$this->vars['appcode']) {
			$this->retorno["status"] = false;
			$this->retorno["error"] = "102";
			$this->retorno["message"] = "Please send email and appcode";
		} else {
			$user = new user();
			$userId = $user->getIdByEmail($this->vars['email']);
			$apps = new user_apps();
			$result = $apps->logout($userId[0]['_id'],$this->vars['appcode']);
			if($result) {
				$this->retorno['status'] = true;				
			} else {
				$this->retorno['status'] = false;	
				$this->retorno["message"] = "Cannot logout for app";			
			}		
		} 
	}
}
?>