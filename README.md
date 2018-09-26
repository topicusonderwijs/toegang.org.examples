# Toegang.org voor uitgevers
In dit document staat beschreven welke inrichting er moet plaatsvinden bij de uitgever om te kunnen koppelen met
toegang.org.

## Registreer een toegangsurl per product
De toegangsurl is een endpoint waarop de gebruiker kan worden gevalideerd en worden doorgestuurd naar de content van het product. 
Indien een gebruiker op Toegang.org terecht komt middels een geldige Tlink/EAN dan logged deze in op toegang.org middels een account of kennisnet-sessie.
Toegang.org stuurt een gemachtigde gebruiker door naar de toegangsurl.


## Ontvang gebruikers via Toegang.org
Gebruikers worden via Toegang.org doorgestuurd naar de toegangsurl die is geregistreerd bij het product. 
Er wordt een [JSON Web Signature](https://tools.ietf.org/html/rfc7515) (JWS) meegestuurd. 
Deze JWS bevat informatie waarmee de gebruiker kan worden doorgestuurd naar het gewenste product.

De url ziet er als volgt uit:
 ```
 {toegangsurl}#{JWS}
 ```
De toegangsurl is de url die in Toegang.org is geregistreerd bij het product. De JWS wordt in de hash van de url gezet 
(dat wil zeggen dat het na de # staat).

De JWS is signed, dit betekent dat de inhoud kan worden geverifieerd. Daardoor kan worden bepaald of de inhoud afkomstig is van Toegang.org.
De JWS kan op twee manieren worden geverifieerd:
1. Middels een door toegang.org beschikbaar gesteld verificatie-endpoint;
2. Voer zelf de verificatie uit middels de public key.

De payload van de geverifieerde JWS bevat de volgende velden:

Veld    | Verplicht | Omschrijving
---     | :---:     | ---
org     | Nee       | ESN van de school van de gebruiker of organisatie waar die school onder valt
orgname | Nee       | Naam van de school van de gebruiker of organisatie waar die school onder valt
fn      | Nee       | Voornaam van de gebruiker
sub     | Ja        | Id van de account die uniek is voor de uitgever
exp     | Ja        | Timestamp van het tijdstip tot wanneer de JWS geldig is.
tlink   | Ja        | De TLink-code van de licentie.
ean     | Ja        | De EAN van het product.
aud     | Ja        | De naam van de organisatie van de uitgever
ref     | Ja        | De meldcode voor de helpdesk om de gebruiker te kunnen volgen door het proces.
lac     | Nee       | Linked accounts; historische identifiers van deze gebruiker door bijvoorbeeld een fusie of account merge
rol	    | Nee	    | Rol van de ingelogde gebruiker (eduPersonAffiliation)

### Verificatie-endpoint
Het verificatie-endpoint verifieert het JWS en stuurt de inhoud weer terug. Het verificatie endpoint is bereikbaar op `https://api.toegang.org/jwt/verify`.  
De testomgeving is bereikbaar op `https://api-test.toegang.org/jwt/verify`.
De JWS moet in de request body als json object worden opgestuurd:
```
{
   "jws": "{JWS}",
}
```
De JWS is de inhoud van het veld `jws`.  Een voorbeeldwaarde van de inhoud van dit veld is op `https://api-test.toegang.org/jwt/testjwt` beschikbaar gesteld.

De body van de response is een JSON-object waarvan het veld `payload` de informatie over de gebruiker bevat.

### Zelf de JWS verifiëren
Om het JWS zelf te kunnen verifiëren is de publieke sleutel nodig. 
Deze sleutel kan worden opgevraagd op het endpoint: `https://api.toegang.org/jwt/jwks`. 
Op de test omgeving is dat `https://api-test.toegang.org/jwt/jwks`.  
Het response-object is een JSON Web Key Set (JWKS), voor meer informatie zie de [specificatie](https://tools.ietf.org/html/rfc7517#page-25).

Er zijn een aantal voorbeeld applicaties gemaakt waarin is uitgewerkt hoe de JWS kan worden geverieerd. 
Er zijn voorbeeld applicaties in de volgende talen:
- PHP  
https://github.com/topicusonderwijs/toegang.org.examples/tree/master/examples/php;
- Node.js  
https://github.com/topicusonderwijs/toegang.org.examples/tree/master/examples/nodejs. 

## Terugkoppeling naar Toegang.org
De uitgever kan terugkoppelen aan Toegang.org dat de gebruiker goed is ontvangen bij de uitgever. 
Toegang.org gebruikt deze terugkoppeling om de koppeling met de uitgever te monitoren.  
Als er geen terugkoppelingen worden gedaan, dan gaat toegang.org er vanuit dat de koppeling met de uitgever niet werkt.  
De terugkoppeling is vooral van belang voor producten met een licentie die een bepaald aantal keer mag worden gebruikt (type = AANTAL),
omdat pas bij de terugkoppeling er van de de licentie wordt afgeschreven.

De terugkoppeling wordt gedaan op de callback API. Dit is een endpoint dat zich bevindt op `https://api.toegang.org/callback/`.  
Op de testomgeving is dat `https://api-test.toegang.org/callback/`.  
In de request body staat een object met twee velden:
 1. `jws` is de JSON Web Signature;
 2. `payload` wordt gevuld met de waarde van het `payload`-object van de geverifieerde JWS.

```http request
POST /callback/ HTTP/1.1
Content-Type: application/json

{
    "jws"     : "{JWS}",
    "payload" : {payload}
}
```
Dit request geeft een lege response met een van de volgende statuscodes:

Statuscode | Omschrijving
---        | ---
204        | De terugkoppeling is in goede orde ontvangen.
400        | Het request object was niet in het juiste formaat.
401        | De JWS is niet geldig.

## Beheer het autorisatieniveau
De gebruiker die via Toegang.org op de toegangsurl terechtkomt is geautoriseerd om de content te benaderen, 
maar er zal nog een aanvullende authenticatie moeten plaatsvinden om de gebruiker toegang te geven tot persoonlijke gegevens,
zoals toetsresultaten.
