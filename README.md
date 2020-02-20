This documentation is also available in [dutch](https://github.com/topicusonderwijs/toegang.org.examples/blob/master/README.NL.md)

# TOEGANG.ORG

Digital content is sold in the Netherlands in a way that has led to a complex process of identification and authorization of the end user.
TOEGANG.ORG helps the publisher by taking care of the technical side of this process and providing support where necessary.

If a publisher wants to use TOEGANG.ORG, it's learning environment must be connected to the TOEGANG.ORG environment.
When this is implemented, TOEGANG.ORG functions as a gatekeeper that only redirects users with a valid license to the learning environment.
The latter then only needs to check whether the user used a valid token issued by TOEGANG.ORG.
This page provides the technical specifications, with examples in commonly used programming languages.

## Implementation process

The technical support teamof TOEGANG.ORG aids in the implementation of the connection of the learning environment to TOEGANG.ORG.
We first link a test environment of TOEGANG.ORG to a test environment of the publisher. We also have to configure the following

- Your digital content ("products") is registered with TOEGANG.ORG. For each product you can specify a separate endpoint (URL) at which the user enters the learning environment.
  If desired, this can also be the same endpoint (as explained below, we also include the EAN in the JWS token).
- We create OAuth2 credentials that allow the publisher to create additional licenses, among other things.

In order to connect the learning environment to toegang.org a publisher has to follow the steps explained below.

## Step 1. Create an endpoint where TOEGANG.ORG users can enter

When a user with a TLink license code or EAN arrives at TOEGANG.ORG, we determine his / her identity and check the authorization for that license.
If everything is correct, we will forward the user to the URL configured for the requested product.

```
https://uitgever.nl/product-a
```

This URL should be accessible in the learning environment for users without a session.

## Step 2. Read the JWS from the URL

When we forward a user to this URL, we always send a JWS token ([JSON Web Signature](https://tools.ietf.org/html/rfc7515)) which functions as a adminssion ticket.
We will send this JWS in the hash of the URL.

### JWS in hash

We complete the URL with a hash fragment containing the JWS.
The complete URL therefore looks like this:

```
{product-url}#{JWS}
```

For example:

```
https://uitgever.nl/product-a#eyJhbGciOiJSUzI1NiIsImtpZCI6InpKYzVGYkFHelM2Ul9BOWN1W...
```

This hash fragment does not arrive directly on the uitgever.nl server; the server only sees a GET request at https://uitgever.nl/product-a.
This prevents the JWS token, containing identity data from the user, from being 'leaked' into request logs.
The user's browser does see the hash fragment and the webpage that is served on https://uitgever.nl/product-a can also read it with Javascript and process it.

## Step 3. Decode the JWS and verify the signature

The JWS token is a string generated by TOEGANG.ORG specifically for this login, it's purpose is to provide the identity of the user as well as proof that the user is authorized to access the digital content.
The signature in the JWS guarantees that the token comes from TOEGANG.org.
It is important that the publisher also verifies this; if not, anyone who knows the product URL could gain access to the product.
The token can be _verified_ in two ways:

- Through a verification endpoint made available by TOEGANG.ORG;
- By performing the verification yourself using the public key of TOEGANG.ORG.

### (3a) Verification endpoint

the verification endpoint verifies the JWS and sends back the payload.
The endpoint can be reached at `https://api.toegang.org/jwt/verify`, for the test environment the correct url is `https://api-test.toegang.org/jwt/verify`.
The JWS must be sent in the body of a POST request as a JSON object (with 1 field `jws`):

```http request
POST /jwt/verify HTTP/1.1
Content-Type: application/json

{
  "jws": "eyJhbGciOiJSUzI1NiIsImtpZCI6InpKYzVGYkFHelM2Ul9BOWN1W..."
}
```

An example JWS for testing this is made available at `https://api-test.toegang.org/jwt/testjwt`.

The body of the response is a JSON object. The payload field contains information about the user.

### (3b) Verify the JWS yourself

To be able to verify the JWS itself, the public key of TOEGANG.ORG is required.
This key can be requested at the endpoint: `https://api.toegang.org/jwt/jwks`.
Or if you are testing your implementation: `https://api-test.toegang.org/jwt/jwks`.  
The response object is a JSON Web Key Set (JWKS), for more information see the [specification](https://tools.ietf.org/html/rfc7517#page-25).

The following examples are available:

- PHP  
  https://github.com/topicusonderwijs/toegang.org.examples/tree/master/examples/php;
- Node.js  
  https://github.com/topicusonderwijs/toegang.org.examples/tree/master/examples/nodejs.

At least make sure the signature is verified and the timestamp in the exp (expires) field is in the future

## Step 4. Process information from the JWS

A verified JWS contains the fields described below.

| Field   | Mandatory | Omschrijving                                                                                                                  |
| ------- | :-------: | ----------------------------------------------------------------------------------------------------------------------------- |
| aud     |    Yes    | Unique identifier of the publisher's organization (UUID)                                                                      |
| ean     |    Yes    | EAN of the product                                                                                                            |
| exp     |    Yes    | Timestamp when the token expires (**milliseconds** since Unix epoch)                                                          |
| ref     |    Yes    | Support code for the service desk to be able to follow the user through the process (XXXXXXXX - XXXXXXXX)                     |
| sub     |    Yes    | ID of the account that is unique to the publisher (32 or 64 (= old) hex characters)                                           |
| tlink   |    Yes    | TLink code of the license (XXXXXXXX)                                                                                          |
| email   |    No     | Verified email of the user                                                                                                    |
| extids  |    No     | Unique identifier of a user at other publishers (only supplied when sublicenses are used) - [{"orgid": ..., "externId": ...}] |
| fn      |    No     | First name of the user                                                                                                        |
| lac     |    No     | Linked accounts; array with historical identifiers of this user due to a merger or account merge, for example                 |
| org     |    No     | ESN of the school of the user or organization that includes that school (UUID)                                                |
| orgname |    No     | Name of the school of the user or organization that includes that school                                                      |
| rol     |    No     | Role of the logged in user (eduPersonAffiliation)                                                                             |
| rnd     |    No     | Random UUID to detect replay attacks                                                                                          |
| endDate |    No     | _(coming soon)_ End date of the license (2020-04-01)                                                                          |

The publisher should be able to log in the user based on this data.
Log in the user for the product in the `ean` field.
Example payload:

```
{
	"aud":	"9089c018-daf8-41a6-8d78-068e6053f42d",
	"ean":	"1234567890123",
	"exp":	1581595892039,
	"ref":	"BEXC4NX2 - B64EDT5M",
	"sub":	"01021a5b98a14385a4064cb66bc9a082",
	"tlink":"TLINK123",
	"email":"hans@weekend.nl",
	"extids":[{orgid: "F33E1046-ADB2-4925-FBA6-B00AFFCF3B00", externId: "02131a5b88b14385a4064cb66bc9a073"}],
	"fn":	"Hans",
	"lac":	["861913bf76c24e5583f7bb844a190863", "2d4020edeaa94759947193435838d077"],
	"org":	"D45E1046-ADB2-4925-FBA6-B00AFFCF3B99",
	"orgname":"Zondag College",
	"rol":	"employee",
	"rnd":	"3b2c2294-03cf-4d45-9e07-1ec01584800a",
	"endDate":"2020-02-13"
}
```

**PLEASE NOTE**: because TOEGANG.ORG cannot give a strong guarantee with regard to the identity of the user, it is not advisable to link personal data to the user.
For the future, functionality is planned to allow the publisher to enforce stronger authentication.

New fields may be added in the future, keep this in mind during implementation.

## Step 5. Callback to TOEGANG.ORG

To make it possible for TOEGANG.ORG to monitor the link with the publisher, the learning environment must provide a callback when the user has been well received.
If no callback is supplied, TOEGANG.ORG will assume that the link with the publisher is not working.
The callback is especially important for products with a license that may only be used a certain number of times (type = NUMBER), because the license is only debited upon receiving the callback.

The endpoint to provide the callback to is: `https://api.toegang.org/callback/`; for the test environment it is `https://api-test.toegang.org/callback/`.
An example request:

```http request
POST /callback/ HTTP/1.1
Content-Type: application/json

{
    "jws"     : "eyJhbGciOiJSUzI1NiIsImtpZCI6InpKYzVGYkFHelM2Ul9BOWN1W...",
    "payload" : { "org":"SchoolX","sub": ... }
}
```

The request body consists of a JSON object with two fields:

1.  `jws` has to contain the just received token
2.  `payload` has to contain the the `payload`-object of the verified JWS. This field allows TOEGANG.ORG to check if the JWS has been successfully decoded.

The response is always an empty body with one of the following status codes.

| Statuscode | Omschrijving                                     |
| ---------- | ------------------------------------------------ |
| 204        | The request was valid                            |
| 400        | The request object was not in the correct format |
| 401        | The JWS is not valid                             |

## Testing

After the publisher implements the above steps, the login flow can be tested.
First thing that we need to test are licensecodes.  
To get licensecodes you can either request them from our support staff or create them yourself by using our API (see below).
A user can then log in using one of the following URLs:

- `https://test.toegang.org/{TLink}` (`{TLink}` has to be replaced by the actial Tlink-code)
- `https://test.toegang.org/{EAN}` (`{EAN}` has to be replaced by the EAN of the product. note: a `Kennisnet` account is needed)

When a user logs in for the first time, TOEGANG.ORG asks for some personal information.
The user is then redirected to the publisher's environment.

## Autorisation

In order to use the ACCESS.ORG API you need OAuth2 "client credentials" and the relevant product must be configured with ACCESS.ORG, as discussed above.
For this you first need a temporary Access Token; this can be obtained by making an OAuth2 client token request at `https://idp.toegang.org/oidc/token` (test: `https://idp-test.toegang.org/oidc/token`).
The OAuth2 client name and client secret separated by `:` and then URL-safe Base64-encoded, must be included in the `Authorization` header.

```http request
POST /token HTTP/1.1
Content-Type: application/x-www-form-urlencoded
Authorization: Basic Y2xpZW50LWlkOmNsaWVudC1zZWNyZXQ=

grant_type=client_credentials
```

An example response:

```
{
    "access_token": "YmFkN2Y3ZmItOTUzZC00M2YyLWExNmUtYW...",
    "expires_in": 3600,
    "token_type": "Bearer"
}
```

You can then use this access_token to make API calls to `https://api.toegang.org` (test:
`https://api-test.toegang.org`) by supplying the access_code in the Authorization header. See the example below.

## Creating licenses

Publishers can create licenses via the TOEGANG.ORG API. A request looks like this:

```http request
POST /tlinklicenses/getLicenseCodes?productId=9789999999664&requestReferenceId=123&amount=50&distributorId=Edubert HTTP/1.1
Authorization: Bearer YmFkN2Y3ZmItOTUzZC00M2YyLWExNmUtYW...
```

Retrieving an Authorization token is explained in the Authorization chapter

| parameter            | betekenis                                                                                 |
| -------------------- | ----------------------------------------------------------------------------------------- |
| `productId`          | Product EAN                                                                               |
| `requestReferenceId` | Reference created by the publisher for this batch. Max 160 characters. Needs to be unique |
| `amount`             | Amount of licenses to be created                                                          |
| `distributorId`      | Name of the publisher as agreed with TOEGANG.ORG                                          |

A successful request leads to a response that looks like this:

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

`codes` is an array containing the the amount of license codes requested.
`startDate` start date of the product if the start date is in the future else it's the date of today.
`endDate` end date of the product.

We offer example implementations for both PHP and NodeJS of this call.

## Retrieve licenses

It is possible to retrieve all licenses of a user by making a call to the licenses API.
This requires the Account ID of the user, which can be found in the 'sub' of JWS (see step 4).
This Account ID must replace {{user-id}} in the request below. In addition, the authorization code (see the authorization chapter) must also be sent.

```http request
GET /accounts/{{user-id}}/licenses HTTP/1.1
Authorization: Bearer YmFkN2Y3ZmItOTUzZC00M2YyLWExNmUtYW...
```

The response looks like this:

```http request
{
    "licenseState": "ACTIVE",
    "startDate": "2019-05-07",
    "activationDate": "2019-05-07T12:52:05.066Z",
    "endDate": "2019-06-01",
    "product": {
        "ean": "9789491795664",
        "licenseType": "SCHOOLJAARLICENTIE"
    }
}
```

| parameter        | description                                                        |
| ---------------- | ------------------------------------------------------------------ |
| `licenseState`   | License status (only ACTIVE licenses are retrieved with this call) |
| `startDate`      | Start date of the license                                          |
| `activationDate` | Activation date of the license                                     |
| `endDate`        | End date of the license                                            |
| `product`        | product container                                                  |
| `ean`            | EAN of the product                                                 |
| `licenseType`    | License type of the product                                        |

## Retrieving KNF attributes

To retrieve the KNF attributes of a user you need one of the TLinks of this user.
The attributes are only available if the user logged in once via Kennisnet.
The TLink can be obtained from the JWS payload (see step 4)

```http request
GET /accounts/attributes/{{tlink}} HTTP/1.1
Authorization: Bearer YmFkN2Y3ZmItOTUzZC00M2YyLWExNmUtYW...
```

The response looks like this:

```http request
{
	"uid": ["4fa672@iets"],
	"eckId": ["https://ketenid.nl/201703/ef863ed"],
	"givenName": ["Hans"],
	"digiDeliveryId": ["ED8AE607-WI3N-414C-T87A-624E74S7T005"],
	"nlEduPersonRealId": ["4567234@som.today"],
	"nlEduPersonUnit": ["1B"],
	"eduPersonAffiliation": ["student"],
	"nlEduPersonProfileId": ["4567234@1.som.today"],
	"nlEduPersonHomeOrganization": ["Demoschool"],
	"nlEduPersonHomeOrganizationId": ["99DE"],
	"sn": ["Weekend"],
	"nlEduPersonTussenvoegsels": ["van der"],
	"ou": ["1B"],
	"mail": ["hans@weekend.nl"],
	"entree_uid": ["4fa672@iets"],
}
```

These are the most common attributes. It is possible that more or fewer attributes are known from the user.
If a user has multiple KNF accounts, the arrays contain more than 1 element.
The KNF attributes provided are explained in detail in the following link: https://developers.wiki.kennisnet.nl/index.php?title=KNF:Attributen_overzicht_voor_Identity_Providers

## Postman voorbeelden

All the above requests can also be tested with [Postman](https://www.getpostman.com). Import the
[TOEGANG.ORG collection](examples/toegang.org.examples.postman_collection.json) and enter your own
`client-name`, `client-secret`, `client-id` and `tlink`.
