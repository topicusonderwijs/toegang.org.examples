<?php

use \Firebase\JWT\JWT;

$config = include('config.php');

// Recommended to use a database instead of a file cache, as this is incompatible with multi tenant systems.
require_once 'simplecache.php';

/**
 * Toegang.org Single Sign On class
 *
 * PHP version 5
 *
 * @category  Authentication
 * @package   Authentication_Toegang_org
 * @license   http://opensource.org/licenses/BSD-3-Clause 3-clause BSD
 *
 * @author    Topicus team Toegang.org <topicus@toegang.org>
 * @author    Paul de Wit <p.dewit@toegang.org>
 * @since     01.08.2018
 * @version   1.0
 *
 * @link     https://github.com/topicusonderwijs/toegang.org.examples
 */

class ToegangSso
{
    public static function GetPublicKey()
    {
        global $config;
        return $config['jwt_public_key'];
    }

    private function Redirect($url, $statusCode = 303)
    {
        header('Location: ' . $url, true, $statusCode);
        die();
    }

    /**
    * Validates jwt token and calls the callback api to let Toegang.org know this was a successful login.
    * Returns an array of decoded user properties, for account login or creation.
    */
    public static function Validate($jws)
    {
        global $config;

        $c = new Cache();

        if (empty($jws)) {
            // 'JWS should not be empty, redirect to generic error page and enable user to call for help or try a tlink
            error_log('ERROR: TOEGANG: JWS is missing from ' . $_SERVER['REMOTE_ADDR']);
            self::Redirect ($config['ui_base_uri'] . '/error');
        } else {
            /*
            * The supported algorithm is RS256. JWT:decode does the validation on the number of segments, algorithm, key,
            * signature, etcetera. We have to check the relevant claims in the payload, like 'exp' and 'aud'.
            */
            $decoded = JWT::decode($jws, self::GetPublicKey(), array('RS256'));
            $decoded_array = (array)$decoded;
            $currentTime = time() * 1000;
            $exp = $decoded_array['exp'];
            if (!empty($exp) && $exp < $currentTime) {
                // JWS has expired, redirect to original Tlink to build a new one
                sleep(2);
                error_log('WARNING: TOEGANG: JWS was expired for ' . $decoded_array['ref']);
                self::Redirect($config['ui_base_uri']."/".$decoded_array['tlink'] );
            }

            //check if token is for the correct publisher
            if (isset($config['uitgever_naam'])) {
                if (!$decoded_array['aud'] == $config['uitgever_naam']) {
                    // 'publisherID doesn match. Probably configuration fault.
                    error_log('ERROR: TOEGANG: PublisherID mismatch ' . $decoded_array['aud'] . ' code: ' . $decoded_array['ref']);
                    self::Redirect ($config['ui_base_uri'] . '/code');
                }
            }
            else
            {
                // publisherID not passed when calling validate().
                error_log('WARNING: TOEGANG: PublisherID not used.');
            }
            // Check if rnd value is the same as last time (for security sake)
            $rnd = $decoded_array['rnd'];
            if ($c->isCached($rnd)) {
                // values are the same, this link is re-used, so redirect to the original tlink
                sleep(5);
                error_log('WARNING: TOEGANG: rnd was reused from ' . $_SERVER['REMOTE_ADDR'] . ' code: ' . $decoded_array['ref']);
                self::Redirect($config['ui_base_uri']."/".$decoded_array['tlink'] );
            }
            else{
                $c->store($rnd,$rnd,1000);
            }

            // Perform Callback
            $payload = json_encode(array("jws"=>$jws, "payload"=>$decoded_array));
            self::doCallback($payload, $config['sso_callback_uri']);
        }
        return ($decoded_array);
    }

    public static function doCallback($payload,$endpoint)
    {
        $headers = array('Content-Type: application/json', 'Content-Length: ' . strlen($payload));
        $options = [
            CURLOPT_URL => 'https://'.$endpoint,
            CURLOPT_HTTPHEADER => $headers,
            CURLOPT_POST => true,
            CURLOPT_POSTFIELDS => $payload,
            // If you are experiencing redirect issues, enable the following parameters:
           // CURLOPT_FOLLOWLOCATION => true,
            CURLOPT_CUSTOMREQUEST => "POST"
        ];
        $curl = curl_init();
        curl_setopt_array($curl, $options);
        $result = curl_exec($curl);
        $httpcode = curl_getinfo($curl, CURLINFO_HTTP_CODE);
        if ($httpcode <> '204')
        {
            error_log('ERROR: Callback failed. Return status: ' . $httpcode);
        }
        curl_close($curl);
    }
}
?>