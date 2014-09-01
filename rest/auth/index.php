<?php
require('./config.php');
 
if (!$config['debug']) {
	error_reporting(0);
} else {
	error_reporting(E_ALL);
	echo "<pre>";
}
require('./autoload.php');
if (!$config['debug']) {
	header('Content-Type: application/json');
}
if ($config['debug']) {
	print_r($_SERVER);
}
$rest = new rest();
?>
