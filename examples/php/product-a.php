<html>
<body>
    <form id="redirectform" method="POST" action="/process-jws.php">
        <input id="tokenholder" type="hidden" name="jws">
    </form>
    <script>
        if(window.location.hash){
            // (Step 2a) extract JWS from URL hash
            document.getElementById('tokenholder').value = window.location.hash.substr(1);
            document.getElementById('redirectform').submit();
        }
    </script>
</body>
</html>
