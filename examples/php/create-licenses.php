<?php

require_once 'licenseservice.php';

$productEan = urlencode('1111111111111');
$referenceId = urlencode(uniqid().uniqid());
$uitgeverId = urlencode('Uitgever X');
$aantalLicenties = "12";

$licenses = LicenseApi::createLicenses($productEan, $referenceId, $uitgeverId, $aantalLicenties);

if(isset($licenses['codes'])){
    foreach ($licenses['codes'] as $license){
        echo "You have created a license with code : ".$license."<br/>";
    }
}
else{
    echo "An error occured while fetching licenses, see console for more details.";
}

?>
