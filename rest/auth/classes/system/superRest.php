<?php

class superRest {
	
	protected $method;
	protected $vars;
	protected $retorno;
	protected $config;
	protected $debug; 

	public function __construct() {
		
		global $config;
		$this->config = $config;
		$this->debug = $this->config['debug'];
		$this->explodeURL();
		
		if(!$this->method) {
			$this->goToDocs();
		}
		
		if (method_exists($this,$this->method)) {
			if (!$this->checkMethod($this->method)) {
				$this->retorno["status"] = false;
				$this->retorno["error"] = "103";
				$this->retorno["message"] = "You are trying to access a private method.";				
			} else {				
				$this->{$this->method}();
			}
		} else {
			$this->retorno["status"] = false;
			$this->retorno["error"] = "101";
			$this->retorno["message"] = "You are trying to access a inexistent method.";
		}
		$this->view($this->retorno);
	}
	
	private function explodeURL() {
		
		$urlPrefix = str_replace('index.php','',$_SERVER['SCRIPT_NAME']);
		if ($urlPrefix != '/') {
			$url = str_replace($urlPrefix,'',$_SERVER['REQUEST_URI']);
		} else {
			$url = substr($_SERVER['REQUEST_URI'],1);
		}
		$temp = explode('/',$url);

		//MMOYANO modificado para test
		//$this->method = $temp[0];
		$this->method = str_replace('index.php?','',$temp[0]);
		
		for($i=1;$i<count($temp); $i+=2) {
			if (trim($temp[$i]) != '') {
			$this->vars[$temp[$i]] = $temp[$i+1];
			}
		}
	}

	private function exitJSON($array){
		return json_encode($array);
	}

	private function view($array) {
		echo $this->exitJSON($array);
	}

	private function goToDocs(){
		header('location: '.$this->config['urls']['docs']);
		die();
	}
	
	private function checkMethod($method) {
		if(in_array($method,$this->permitidos)){
			return true;
		} else {
			return false;
		}
	}

	protected function isDecimalNumber($n) {
		return is_numeric($n);
	}

	protected function checkDateTime($data) {
		if (@date('YmdHis', @strtotime($data)) == $data) {
			return true;
		} else {
			return false;
		}
	}
}
?>