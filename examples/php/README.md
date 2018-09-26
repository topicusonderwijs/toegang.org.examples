## Toegang.org PHP key validation example ##

This repository includes a very basic PHP application that can validate keys received from the toegang-org callback service.
There are two ways of validating the logged in user:
* Validate the token (JWT) signed by toegang.org
* Do an async call to our callback api to validate the token

Both ways are available in this demo application.

## Installation
Requirements: PHP (>= 5.3) & php-curl (install via package manager of your choice)
To install the small set of dependencies for this example application you need to run [Composer](https://getcomposer.org/):

```composer install```

