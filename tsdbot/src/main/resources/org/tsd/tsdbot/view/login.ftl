<#-- @ftlvariable name="" type="org.tsd.tsdbot.view.LoginView" -->
<#import "layout.ftl" as layout>
<@layout.layout title="TSDHQ Login">

    <script type="text/javascript">
        $(function() {
            $("#loginForm").submit(function (event) {
                var data = {};
                $("#loginForm").serializeArray().map(function(x){data[x.name] = x.value;});
                $.ajax({
                    type: "POST",
                    url: "/login",
                    data: JSON.stringify(data),
                    contentType:"application/json",
                    success: function (data, status, jqXHR) {
                        console.log("Login success");
                        window.location.href = '/dashboard'
                    },
                    error: function(xhr, status, error) {
                        $.bootstrapGrowl("Username or password is incorrect", {
                            type: 'danger',
                            width: 'auto',
                            allow_dismiss: true
                        });
                    }
                });
                event.preventDefault();
            })
        });
    </script>

    <div class="col-md-9" style="float: none; margin: 0 auto;">
        <form id="loginForm">
            <fieldset>
                <legend>Login</legend>
                <div class="form-group">
                    <label for="username">Username</label>
                    <input name="username" type="text" class="form-control" id="username" placeholder="Enter username">
                </div>
                <div class="form-group">
                    <label for="password">Password</label>
                    <input name="password" type="password" class="form-control" id="password" placeholder="Password">
                </div>
                <button type="submit" class="btn btn-primary">Login</button>
            </fieldset>
        </form>
    </div>
</@layout.layout>