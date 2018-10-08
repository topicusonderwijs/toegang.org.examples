## TOEGANG.ORG Node.js key validation example ##

This repository includes a very basic Node.js application that can validate keys received from the toegang.org service.
There are two ways of validating the logged in user:
* Validate the token (JWS) signed by toegang.org
* Do an async call to our callback api to validate the token
Both ways are available in this demo application.

In addition to the validation classes we now offer an example
implementation for the creation of a license in our backend for any of your products, which includes:
* example of the oauth2 client_credentials flow authentication
* example of the creation of a example license using our licensing API for a already known product.
If you would like add integration of toegang.org in a new product of yours,
please contact support as we need to create this product for your. You can reach us at info@toegang.org and/or +31387114424.
