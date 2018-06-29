## Toegang.org nodejs key validation example ##

This repository includes a very basic nodejs application that can validate keys received from the toegang-org callback service.
There are two ways of validating the logged in user:
* Validate the token (JWT) signed by toegang.org
* Do an async call to our callback api to validate the token
Both ways are available in this demo application.