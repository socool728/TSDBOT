<#macro layout title>
    <!DOCTYPE html>
    <html lang="en">
        <head>
            <meta charset="utf-8">
            <meta http-equiv="X-UA-Compatible" content="IE=edge">
            <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
            <meta name="description" content="">
            <meta name="author" content="">
            <title>${title?html}</title>

            <!-- Darkly CSS -->
            <link rel="stylesheet" href="/assets/darkly/css/bootstrap.min.css"/>

            <!--VideoJS CSS-->
            <link href="http://vjs.zencdn.net/6.2.8/video-js.css" rel="stylesheet">

            <!--TSDTV CSS-->
            <link href="/assets/css/tsdtv.css" rel="stylesheet">

            <!--jQuery-->
            <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>

            <!--moment.js-->
            <script src="https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.19.1/moment.min.js"></script>
            <script src="https://cdnjs.cloudflare.com/ajax/libs/moment-timezone/0.5.14/moment-timezone-with-data.min.js"></script>

            <!--Bootstrap Growl-->
            <script src="/assets/js/bootstrap-growl.min.js"></script>

            <script type="text/javascript">

                //https://codepen.io/gabrieleromanato/pen/LpLVeQ
                function toJSONString( form ) {
                    var obj = {};
                    var elements = form.querySelectorAll( "input, select, textarea, hidden" );
                    for( var i = 0; i < elements.length; ++i ) {
                        var element = elements[i];
                        var name = element.name;
                        var value = element.value;

                        if( name ) {
                            obj[ name ] = value;
                        }
                    }

                    return JSON.stringify( obj );
                }

                $(function() {

                    // play()
                    $(document).on("click", ".logout", function () {
                        $.ajax({
                            type: "POST",
                            url: "/logout",
                            contentType: "application/json; charset=utf-8",
                            success: function (msg) {
                                console.log("logged out");
                                window.location.href = '/'
                            },
                            error: function (xhr, status, error) {
                                $.bootstrapGrowl(xhr.responseText, {
                                    type: 'danger',
                                    width: 'auto',
                                    allow_dismiss: true
                                });
                            }
                        });
                        event.preventDefault();
                    });
                });
            </script>
        </head>
        <body>

        <!--Nav-->
        <div class="navbar navbar-default navbar-fixed-top">
            <div class="container">
                <div class="navbar-header">
                    <a href="/" class="navbar-brand">TSDHQ</a>
                    <button class="navbar-toggle" type="button" data-toggle="collapse" data-target="#navbar-main">
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                    </button>
                </div>
                <div class="navbar-collapse collapse" id="navbar-main">
                    <ul class="nav navbar-nav">
                        <li>
                            <a href="/filenames">Filenames</a>
                        </li>
                        <li>
                            <a href="/tsdtv">TSDTV</a>
                        </li>

                        <#if loggedInUser?? && loggedInUser.role.level gt 0>
                        <li>
                            <a href="/tsdtv/agent">Agents</a>
                        </li>
                        </#if>
                    </ul>

                    <ul class="nav navbar-nav navbar-right">
                        <li><a href="#">A TSD Production</a></li>
                        <#if loggedInUser??>
                            <li><button class="btn btn-secondary my-2 my-sm-0 logout">Logout</button></li>
                        </#if>
                    </ul>
                </div>
            </div>
        </div>

        <!--Content-->
        <div class="container-fluid" style="padding-top: 65px;">
            <div class="row">
                <#nested/>
            </div>
        </div>

        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"
                integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa"
                crossorigin="anonymous"></script>
        <script src="http://vjs.zencdn.net/6.2.8/video.js"></script>
        <script src="https://unpkg.com/videojs-flash@2.0.0/dist/videojs-flash.min.js"></script>
        </body>
    </html>
</#macro>