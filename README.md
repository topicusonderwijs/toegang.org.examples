# TOEGANG.ORG voor uitgevers

De manier waarop licenties voor elektronisch lesmateriaal in Nederland in het algemeen worden verkocht heeft geleid tot
een complex proces van identificatie en autorisatie van de eindgebruiker. TOEGANG.ORG ontzorgt de uitgever door dit proces
technisch voor zijn rekening te nemen en waar nodig menselijk te ondersteunen. 

Als een uitgever gebruik wil maken van TOEGANG.ORG, dient de gebruiksomgeving van een uitgever aangesloten te worden
op de omgeving van TOEGANG.ORG. Wanneer dit geïmplementeerd is, functioneert TOEGANG.ORG als poortwachter die alleen
gebruikers met een geldige licentie doorstuurt naar de gebruiksomgeving. De laatste hoeft dan alleen nog te controleren
of de gebruiker een geldig toegangsbewijs van TOEGANG.ORG bezit. Deze pagina geeft hiervoor de technische specificaties, 
met voorbeelden in veelgebruikte programmeertalen.

## Implementatieproces

Het implementeren van de aansluiting van een gebruiksomgeving op TOEGANG.ORG vindt plaats in samenwerking met het technische
supportteam van TOEGANG.ORG. We koppelen eerst een testomgeving van TOEGANG.ORG aan een testomgeving van de uitgever. Aan de
kant van TOEGANG.ORG zullen we de volgende zaken inrichten:

* Uw elektronische leermiddelen ("producten") worden geregistreerd bij TOEGANG.ORG. Voor elk product kunt u een
apart endpoint (URL) opgeven waarop de gebruiker in de gebruiksomgeving binnenkomt. Dit kan naar wens ook hetzelfde endpoint
zijn (zoals hieronder uitgelegd nemen we het EAN ook op in het JWS token).
* We maken OAuth2-credentials aan waarmee de uitgever o.a. zelf extra licenties uit kan geven.

De stappen die aan de kant van de gebruiksomgeving genomen moeten worden staan hieronder uitgelegd.

## Stap 1. Maak een endpoint waarop TOEGANG.ORG-gebruikers binnen kunnen komen

Wanneer een gebruiker met een TLink-licentiecode of EAN bij TOEGANG.ORG aankomt, bepalen wij zijn/haar identiteit en checken
we de autorisatie voor die licentie. Klopt deze, dan sturen we de gebruiker door naar de URL die we voor u hebben geregistreerd bij
het betreffende product, bijvoorbeeld
```
https://uitgever.nl/product-a
```
Deze URL moeten opengezet worden in de gebruiksomgeving (voor gebruikers zonder sessie).

## Stap 2. Lees de JWS uit

Wanneer we een gebruiker doorsturen
naar deze URL sturen we altijd ook een JWS token ([JSON Web Signature](https://tools.ietf.org/html/rfc7515)) mee als "toegangsbewijs".
Dit kan op twee verschillende manieren (de keuze is aan de uitgever, en dient van tevoren aan ons team doorgegeven te worden):

### (2a) JWS in hash

We vullen de URL aan met een hash fragment met daarin de JWS. De complete URL ziet er dus schematisch als volgt uit:
```
{product-url}#{JWS}
```
Bijvoorbeeld:
```
https://uitgever.nl/product-a#eyJhbGciOiJSUzI1NiIsImtpZCI6InpKYzVGYkFHelM2Ul9BOWN1W...
```
Dit hash fragment komt niet direct op de `uitgever.nl` server aan; deze ziet alleen een `GET` request op `https://uitgever.nl/product-a`.
De browser van de gebruiker ziet het hash fragment wel, en de pagina die geserveerd wordt op `https://uitgever.nl/product-a` kan het ook
uitlezen met Javascript. Deze manier is dus vooral nuttig voor (single-page) Javascript frontend applicaties. De applicatie gebruikt het
token in de communicatie met zijn eigen backend als bewijs dat de gebruiker geautoriseerd is.

### (2b) JWS als POST body

In dit geval instrueren we de browser van de gebruiker om een POST request te doen op `https://uitgever.nl/product-a` met de JWS
in de body.

## Stap 3. Decodeer het JWS en verifieer de handtekening

Het JWS token is een door TOEGANG.ORG specifiek voor deze inlogactie gegenereerde string, die voor de gebruiksomgeving van de uitgever
zowel dient ter indicatie van de identiteit van de gebruiker, als ter bewijs dat deze geautoriseerd is. De digitale handtekening
in de JWS garandeert dat het token van TOEGANG.ORG afkomstig is. Het is van belang dat de uitgever dit ook *verifieert*;
zo niet, dan zou iedereen die de product-URL weet toegang kunnen krijgen tot het product.
Het token kan op twee manieren worden geverifieerd:

- Middels een door TOEGANG.ORG beschikbaar gesteld verificatie-endpoint;
- Door zelf de verificatie uit te voeren middels de public key van TOEGANG.ORG.

### (3a) Verificatie-endpoint
Het verificatie-endpoint verifieert het JWS en stuurt de inhoud weer terug.
Het endpoint is bereikbaar op `https://api.toegang.org/jwt/verify` -- voor de testomgeving op `https://api-test.toegang.org/jwt/verify`.
De JWS moet in de body van een `POST` request als JSON-object (met 1 veld `jws`) worden opgestuurd:

```http request
POST /jwt/verify HTTP/1.1
Content-Type: application/json

{
  "jws": "eyJhbGciOiJSUzI1NiIsImtpZCI6InpKYzVGYkFHelM2Ul9BOWN1W..."
}
```
Een voorbeeld-JWS om dit mee te testen wordt op `https://api-test.toegang.org/jwt/testjwt` beschikbaar gesteld.

De body van de response is een JSON-object waarvan het veld `payload` de informatie over de gebruiker bevat.

### (3b) Zelf de JWS verifiëren
Om de JWS zelf te kunnen verifiëren is de publieke sleutel van TOEGANG.ORG nodig. 
Deze sleutel kan worden opgevraagd op het endpoint: `https://api.toegang.org/jwt/jwks`. 
Op de test omgeving is dat `https://api-test.toegang.org/jwt/jwks`.  
Het response-object is een JSON Web Key Set (JWKS), voor meer informatie zie de [specificatie](https://tools.ietf.org/html/rfc7517#page-25).

Er zijn een aantal voorbeeld applicaties gemaakt waarin is uitgewerkt hoe de JWS kan worden gedecodeerd en geverifieerd. 
Er zijn voorbeeld applicaties in de volgende talen:
- PHP  
https://github.com/topicusonderwijs/toegang.org.examples/tree/master/examples/php;
- Node.js  
https://github.com/topicusonderwijs/toegang.org.examples/tree/master/examples/nodejs. 

Zorg er in ieder geval voor dat de handtekening geverifieerd wordt, en dat de timestamp in het `exp` (expires)
veld nog in de toekomst ligt.


## Stap 4. Verwerk informatie uit de JWS

De payload van de geverifieerde JWS bevat de volgende velden:

Veld    | Verplicht | Omschrijving
---     | :---:     | ---
org     | Nee       | ESN van de school van de gebruiker of organisatie waar die school onder valt
orgname | Nee       | Naam van de school van de gebruiker of organisatie waar die school onder valt
fn      | Nee       | Voornaam van de gebruiker
sub     | Ja        | Id van de account die uniek is voor de uitgever
exp     | Ja        | Timestamp van het tijdstip tot wanneer de JWS geldig is (milliseconden sinds Unix epoch).
tlink   | Ja        | De TLink-code van de licentie.
ean     | Ja        | De EAN van het product.
aud     | Ja        | De naam van de organisatie van de uitgever
ref     | Ja        | De meldcode voor de helpdesk om de gebruiker te kunnen volgen door het proces.
lac     | Nee       | Linked accounts; historische identifiers van deze gebruiker door bijvoorbeeld een fusie of account merge
rol	    | Nee	    | Rol van de ingelogde gebruiker (eduPersonAffiliation)

Op basis van deze gegevens zou de uitgever de gebruiker in moeten kunnen loggen.
Log de gebruiker in voor het product wat in het `ean` veld staat.

**LET OP**: omdat TOEGANG.ORG niet bij alle
inlogflows een sterke garantie kan geven m.b.t. de identiteit van de gebruiker is het niet aan te raden om persoonlijke
gegevens te koppelen aan de gebruiker. Voor de toekomst staat wel functionaliteit gepland waarmee de uitgever een
sterkere authenticatie af kan dwingen.

## Stap 5. Terugkoppeling naar TOEGANG.ORG

Om het mogelijk te maken voor TOEGANG.ORG om de koppeling met de uitgever te monitoren, dient de gebruiksomgeving
terug te koppelen dat de gebruiker goed is ontvangen. 
Als er geen terugkoppelingen worden gedaan, dan gaat TOEGANG.ORG er vanuit dat de koppeling met de uitgever niet werkt.  
De terugkoppeling is vooral van belang voor producten met een licentie die een bepaald aantal keer mag worden gebruikt (type = AANTAL),
omdat pas bij de terugkoppeling er van de de licentie wordt afgeschreven.

De terugkoppeling wordt gedaan op het endpoint `https://api.toegang.org/callback/`; op de testomgeving is dat
`https://api-test.toegang.org/callback/`. Het formaat is als volgt:

```http request
POST /callback/ HTTP/1.1
Content-Type: application/json

{
    "jws"     : "eyJhbGciOiJSUzI1NiIsImtpZCI6InpKYzVGYkFHelM2Ul9BOWN1W...",
    "payload" : { "org":"SchoolX","sub": ... }
}
```

De request body moet bestaan uit een JSON object met twee velden:
 1. `jws` bevat het token wat zojuist ontvangen is;
 2. `payload` bevat de waarde van het `payload`-object van de geverifieerde JWS. Door dit veld
    kan TOEGANG.ORG controleren dat het decoderen van de JWS gelukt is.

Dit request geeft een lege response met een van de volgende statuscodes:

Statuscode | Omschrijving
---        | ---
204        | De terugkoppeling is in goede orde ontvangen.
400        | Het request object was niet in het juiste formaat.
401        | De JWS is niet geldig.


## Testen

Als bovenstaande stappen aan de kant van de uitgever geïmplementeerd zijn, kan de inlogflow getest worden. Hiervoor moeten
er in de TOEGANG.ORG-testomgeving wel eerst licenties uitgegeven zijn voor de betreffende producten.
Dit kan op verschillende manieren gebeuren; hetzij op verzoek door onze supportmedewerkers, hetzij rechtstreeks door uzelf
via onze API (zie hieronder). Vervolgens kan er een gebruiker inloggen via een van de volgende URLs:
 - `https://test.toegang.org/{TLink}` (waar `{TLink}` vervangen wordt door een daadwerkelijke TLink-code van een licentie)
 - `https://test.toegang.org/{EAN}` (waar `{EAN}` vervangen wordt door het EAN van een van de geregistreerde producten; hierbij wordt
 de authenticatie gefedereerd naar Kennisnet)

Bij de eerste keer inloggen vraagt TOEGANG.ORG om een aantal persoonlijke gegevens. Daarna wordt de gebruiker doorgestuurd naar
de omgeving van de uitgever. 

## Aanmaken van licenties

Met behulp van de TOEGANG.ORG licentie-API kunnen uitgevers licenties aanmaken; de met dat proces gegenereerde "T-Link"
licentiecodes kunnen worden doorgegeven aan de eindgebruiken na bijvoorbeeld een aanschaf in een webshop. Om gebruik
te kunnen maken van de API heeft u OAuth2 "client credentials" nodig en moet het betreffende product ingericht zijn
bij TOEGANG.ORG, zoals hierboven besproken.

Om de licentie-API aan te spreken heeft u eerst een tijdelijk Access Token nodig; dit is te verkrijgen door een
OAuth2 client token request te doen op `https://idp.toegang.org/token` (test: `https://idp-test.toegang.org/token`).
Hierbij moeten de OAuth2 client name en client secret, gescheiden door `:` en vervolgens URL-safe Base64-encoded, in
de `Authorization` header meegegeven worden.

```http request
POST /token HTTP/1.1
Content-Type: application/x-www-form-urlencoded
Authorization: Basic Y2xpZW50LWlkOmNsaWVudC1zZWNyZXQ=

grant_type=client_credentials
```
Het response hiervan
```
{
    "access_token": "YmFkN2Y3ZmItOTUzZC00M2YyLWExNmUtYW...",
    "expires_in": 3600,
    "token_type": "Bearer"
}
```
bevat het token dat toegang geeft tot de licentie-API.

De licentie-API draait op `https://api.toegang.org` (testomgeving: 
`https://api-test.toegang.org`). Het formaat van het request is:

```http request
POST /tlinklicenses/getLicenseCodes?productId=9789999999664&requestReferenceId=123&amount=50&distributorId=Edubert HTTP/1.1
Authorization: Bearer YmFkN2Y3ZmItOTUzZC00M2YyLWExNmUtYW...
```

Het token in de `Authorization` header is hetgene dat zojuist verkregen is via de OAuth2 client credentials flow.
De verschillende parameters staan voor:

parameter            | betekenis
---                  | ---
`productId`          | EAN van het product waarvoor de licenties aangemaakt worden.
`requestReferenceId` | Door de uitgever aangemaakte referentie voor deze batch. Max. 160 karakters. Mag niet eerder gebruikt zijn.
`amount`             | Aantal licenties dat aangemaakt moet worden.
`distributorId`      | Naam van de uitgever zoals overeengekomen met TOEGANG.ORG

Bij een succesvol request ziet het response (JSON) er als volgt uit:
```http request
{
    "codes": [
        "B9Q4KXM6",
        "CJBCZEQT",
        ...
        ],
    "startDate": "2018-10-02",
    "endDate": "2024-08-01"
}
```
Hier is `codes` het lijstje met nieuwe TLink-licentiecodes (zo veel als aangevraagd). `startDate` is de startdatum van het
product als dat nog in de toekomst ligt, en anders de huidige datum. `endDate` is de einddatum van het product.

Voor dit request bieden we voorbeeldimplementaties voor zowel PHP als NodeJS.

## Postman voorbeelden

Al bovenstaande requests zijn ook te testen met [Postman](https://www.getpostman.com). Importeer de
[TOEGANG.ORG collection](examples/toegang.org.examples.postman_collection.json) en vul in de variables uw eigen
`client-name` en `client-secret` in.
