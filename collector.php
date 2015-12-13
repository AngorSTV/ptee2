<?php
$SQL_server = '';
$user = '';
$password = '';
$database = '';

$mysqli = new mysqli ( $SQL_server, $user, $password, $database );

if (! $mysqli) {
	file_put_contents ( 'sqli_log.txt', date ( "y.m.d H:i:s" ) . " Connect DB error (" . mysqli_connect_errno () . ") " . mysqli_connect_error () . "\n", 8 );
	die ( 'Connect Error (' . mysqli_connect_errno () . ') ' . mysqli_connect_error () );
}

$stmt = $mysqli->prepare ( 'INSERT INTO `u870394185_usage`.`collector` (`date`, `ip`, `host`, `version`) VALUES (?, ?, ?, ?)' );

$stmt->bind_param ( 'sssi', $date, $ip, $host, $version );

$version = 0 + $_POST ['version'];
$host = $_POST ['host'];
$date = date ( 'y.m.d H:i:s' );
$ip = $_SERVER ['REMOTE_ADDR'];

//$stmt->bind_param ( 'sssi', $date, $ip, $host, $version );

$stmt->execute ();
echo "IP=".$ip."<br>";
echo "Rows inserted: ".$stmt->affected_rows;
$stmt->close ();

?>