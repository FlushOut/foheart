<?php
class user extends superModel {
  
    protected $table = 'users';

    public function getIdByEmail($email) {
        return $this->select("_id",array("email='".addslashes($email)."'","status=1"));
    }

    public function login($id,$password) {
        return $this->genericQuery("select 1 from users where _id = ".$id." and password ='".$password."' and status = 1");
    }
}