<html>
    <body>
        <form method="post" action="toegangcallback.php">
            <label for="jwt">JWS: </label><input id="tokenholder" type="text" name="jws"/>
            <input type="submit" value="Validate"/>
        </form>
        <script>
           if(window.location.hash){
                document.getElementById('tokenholder').value = window.location.hash.substr(1);
           }
        </script>
    </body>
</html>
