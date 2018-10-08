module.exports = async function(req, res, next) {
    res.write(`<html><body>
                    Test product page with JWS in hash:<br/>
                    <form>
                        <tt>/product-a #</tt>
                        <input id="tokenholder" type="text" name="token" placeholder="Paste JWS here"><br/>
                        <input type="button" value="Go" onclick="go()">
                    </form>
                    or
                    <a href="/create-licenses">Create some licenses</a>
                    <script>
                        function go() {
                            window.location = '/product-a#' + document.getElementById('tokenholder').value;
                        }
                    </script>
                </body></html>`);
    res.end();
    return;
}