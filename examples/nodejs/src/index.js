const express = require('express');
const bodyParser = require('body-parser');
const HomePage = require('./home');
const ProductPage = require('./product');
const ProcessJWS = require('./processjws');
const CreateLicenseBackend = require('./licensecreation');

// If your project doesn't use express, check validator.js for code that validates our token.
// (depends on node-jose library)
const app = express();
app.use(bodyParser.urlencoded());

// overview page linking to all functionality of this example
app.get('/', HomePage);

// (Step 1) publish an example product page to which TOEGANG.ORG can redirect
app.get('/product-a', ProductPage);

// endpoint that authorizes the user based on JWS (Steps 3-5)
app.post('/process-jws', ProcessJWS);

// A license-creation endpoint.
// As this resource will create licenses on accessing the mounted url, it serves demonstration purposes only.
app.get('/create-licenses', CreateLicenseBackend);

app.listen(3000, () => console.log('Toegang.org Example project started on http://localhost:3000'));
