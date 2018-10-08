// (Step 2a) this page reads the JWS from the URL hash and POSTs it to /process-jws
module.exports = async function(req, res, next) {
    res.write(`<html><body>
                    <form id="redirectform" method="POST" action="/process-jws">
                        <input id="tokenholder" type="hidden" name="token">
                    </form>
                    <script>
                        if(window.location.hash){
                            document.getElementById('tokenholder').value = window.location.hash.substr(1);
                            document.getElementById('redirectform').submit();
                        }
                    </script>
                </body></html>`);
    res.end();
    return;
}