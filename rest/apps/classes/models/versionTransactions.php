<?php
class versionTransactions extends superModel {

    public function checkVersion($tablename,$request,$response) {
        return $this->genericQuery("select 1 from version_transactions where table_name = '".$tablename."' and fk_request = '".$request."' and fk_response = '".$response."'");
    }

}