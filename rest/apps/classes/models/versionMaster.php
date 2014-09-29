<?php
class versionMaster extends superModel {
  
    protected $table = 'version_masters';

    public function checkVersion($tablename,$version) {
        return $this->genericQuery("select 1 from version_masters where table_name = '".$tablename."' and version > ".$version);
    }

}