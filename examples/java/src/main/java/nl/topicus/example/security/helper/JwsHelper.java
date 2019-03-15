package nl.topicus.example.security.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.topicus.example.exception.JwsConversionException;
import nl.topicus.example.model.JwsPayload;
import nl.topicus.example.security.JwsConstants;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.ErrorCodes;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class JwsHelper {

    private static JwsPayload JwsPayload = new JwsPayload();
    private static long expirationInMs;
    private static NumericDate expirationDate;

    private JwsHelper(){}

    /**
     * Stelt in waar een JWS token aan moet voldoen en ontsleutelt deze met de public key
     *
     * @param token moet een valide JWS token zijn
     * @param publisherName moet overeenkomen met de publisher dat in de JWS payload zit
     * @return de JWS payload in json formaat
     * @throws JwsConversionException als de publisher niet overeenkomt, het vervaldatum van de token is verlopen of als de token niet valide is
     */
    public static String validateJws(String token, String publisherName) throws JwsConversionException {
        try {
            /*
             *  Hier wordt de public key verwerkt. Hiermee wordt de token ontsleuteld
             */
            byte[] data = Base64.getDecoder().decode((JwsConstants.PUBLIC_KEY.getBytes()));
            X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
            KeyFactory fact = KeyFactory.getInstance("RSA");
            PublicKey key = fact.generatePublic(spec);
            /*
             * De eisen worden ingesteld naar waar de token moet voldoen, ook wordt ingesteld hoe de token ontsleuteld dient te worden
             */
            JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                    .setAllowedClockSkewInSeconds(30)
                    .setRequireSubject()
                    .setExpectedAudience(publisherName)
                    .setVerificationKey(key)
                    .setJwsAlgorithmConstraints(
                            new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST,
                                    AlgorithmIdentifiers.RSA_USING_SHA256))
                    .build();

            /*
             * Nu wordt de token getoetst met de door ons ingestelde eisen en manier van ontsleutelen
             */
            JwtClaims jwtClaims = jwtConsumer.processToClaims(token);

            /*
             * Het vervaldatum van de token wordt op correcte wijze, door deze methode, bewaart in 2 verschillende variabelen
             */
            initializeDate(jwtClaims.getExpirationTime().getValueInMillis());

            if (NumericDate.now().isOnOrAfter(expirationDate)) {
                throw new JwsConversionException("JWS token has expired");
            }

            return jwtClaims.toJson();

        } catch (InvalidJwtException e) {
            if (e.hasErrorCode(ErrorCodes.AUDIENCE_INVALID)) {
                throw new JwsConversionException("Invalid audience");
            }

        } catch (MalformedClaimException | NoSuchAlgorithmException | InvalidKeySpecException multiE) {
            multiE.printStackTrace();
        }

        throw new JwsConversionException("Invalid JWS token");
    }

    /**
     *  Haalt alle eigenschappen uit een json JWS payload en plaatst deze in een java object genaamd: 'JwsPayload'
     *
     * @param jwsJsonToken moet een json string bevatten van een ontsleutelde JWS payload
     * @return een java class met daarin de eigenschappen vanuit de json JWS payload
     * @throws IOException wanneer de json JWS payload niet goed kan worden gelezen
     */
    public static JwsPayload createJwsPayload(String jwsJsonToken) throws IOException {
        setJwsPayload(new ObjectMapper().readValue(jwsJsonToken, JwsPayload.class));
        return JwsPayload;
    }

    public static long getExpirationInMs() {
        return expirationInMs;
    }

    public static JwsPayload getJwsPayload() {
        return JwsPayload;
    }

    /**
     * Vult 2 fields in. De eerste als leesbare datum en de tweede het datum in milliseconden
     *
     * @param expirationInMs het vervaldatum van de JWS token in milliseconden
     */
    private static void initializeDate(long expirationInMs){
        JwsHelper.expirationInMs = expirationInMs;
        JwsHelper.expirationDate = NumericDate.fromMilliseconds(expirationInMs / 1000);
    }

    private static void setJwsPayload(JwsPayload jwsPayload) {
        JwsHelper.JwsPayload = jwsPayload;
    }
}
