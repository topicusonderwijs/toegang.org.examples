<?php

require_once 'vendor/autoload.php';
require_once 'toegangsso.php';

/**
 * Get the posted JWT
 */
$jws = $_POST['jws'];

$authenticated_user = ToegangSso::validate($jws);

if(isset($authenticated_user)){
    echo "A user has succesfully authenticated: ";
    foreach($authenticated_user as $prop){
        echo "<br>".$prop;
    }
}
else{
    echo "Error authenticating user, see log for more information.";
}
?>
