<?php
class user_apps extends superModel {
  
    protected $table = 'user_applications';

    public function login($fk_user,$fk_application = "") {
    	if($fk_application == "")
        	$this->genericQuery("update user_applications SET logged = 1 where fk_user = ".$fk_user);
    	else
    		$this->genericQuery("update user_applications SET logged = 1 where fk_user = ".$fk_user." and fk_application = ".$fk_application);
        return true;
    }

    public function logout($fk_user,$fk_application = "") {
        if($fk_application == "")
        	$this->genericQuery("update user_applications SET logged = 0 where fk_user = ".$fk_user);
    	else
    		$this->genericQuery("update user_applications SET logged = 0 where fk_user = ".$fk_user." and fk_application = ".$fk_application);
        return true;
    }

    public function getAppsByUser($fk_user) {
    	$query = $this->genericQuery("select ua.fk_application as _id, a.code, ua.start_app from user_applications ua inner join applications a on ua.fk_application = a._id where fk_user = ".$fk_user);
    	$objReturn = array();
    	foreach ($query as $value) {
            $objReturn[] = $value;
        }
        return $objReturn;
    }
}