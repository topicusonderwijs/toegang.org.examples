const http = require('http');
const url = require('url');
const express = require('express');
const bodyParser = require('body-parser');
const MainPage = require('./mainpage');
const ToegangCallback = require('./toegangcallback');
const CreateLicenseBackend = require('./licensecreation');
const config = require('./config');


// If your project doesn't use express, check validator.js for code that validates our token.
// (depends on node-jose library)
const app = express();
app.use(bodyParser.urlencoded());
// mount the frontend application/page (for validation and licensecreate purposes)
app.get('/', MainPage);
// mount the validation callback page
app.post('/toegang', ToegangCallback);
// mount the license-create rest backend.
// As this resource will create a license on accessing the mounted url, it serves demonstation purposes only.
app.get('/license', CreateLicenseBackend);
app.listen(3000, () => console.log('Toegang.org Example project started'));
