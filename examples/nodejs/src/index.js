const http = require('http');
const url = require('url');
const express = require('express');
const bodyParser = require('body-parser');
const MainPage = require('./mainpage');
const ToegangCallback = require('./toegangcallback');


// If your project doesn't use express, check validator.js for code that validates our token.
// (depends on node-jose library)

const app = express();
app.use(bodyParser.urlencoded());
// mount the input form
app.get('/', MainPage);
// mount the validation callback page
app.post('/toegang', ToegangCallback);

app.listen(3000, () => console.log('Toegang.org Example project started'));