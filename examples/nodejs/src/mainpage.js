const url = require('url');
const validator = require('./validator');

module.exports = async function(req, res, next) {
    res.write(`<html><body>
                    If no token received, enter it manually here to test validation:<br/>
                    <form method="POST" action="/toegang">
                        <input id="tokenholder" type="text" name="token">
                        <input type="submit" value="Validate">
                    </form>
                    or
                    <a href="/license">Create a license</a>
                    <script>
                        if(window.location.hash){
                            document.getElementById('tokenholder').value = window.location.hash.substr(1);
                        }
                    </script>
                </body></html>`);
    res.end();
    return;
}