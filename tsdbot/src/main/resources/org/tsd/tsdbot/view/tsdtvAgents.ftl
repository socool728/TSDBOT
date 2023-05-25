<#-- @ftlvariable name="" type="org.tsd.tsdbot.view.DashboardView" -->
<#import "layout.ftl" as layout>
<@layout.layout title="TSDTV Agents">

<script type="text/javascript">
    (function refresh_agents_list() {
        $.get('/tsdtv/agent/list',{},function(responseJson) {

            var htmlString = '';

            htmlString +=
                    '<thead>' +
                    '<tr class="table-info">' +
                        '<th>ID</th>' +
                        '<th>Status</th>' +
                        '<th>Last Heartbeat</th>' +
                        '<th>Bitrate</th>' +
                        '<th>Inventory Last Updated</th>' +
                        '<th>Movies</th>' +
                        '<th>Series</th>' +
                    '</tr>' +
                    '</thead>';

            htmlString +=
                    '<tbody>';

            $.each(responseJson, function(i, onlineAgent) {
                var inventory = onlineAgent.inventory;

                var lastHeartbeat = (onlineAgent.lastHeartbeat) ?
                        moment(onlineAgent.lastHeartbeat*1000).tz('America/Chicago').format('MMM DD HH:mm:ss z')
                        : 'N/A';

                var inventoryLastUpdated = (onlineAgent.inventoryLastUpdated) ?
                        moment(onlineAgent.inventoryLastUpdated*1000).tz('America/Chicago').format('MMM DD HH:mm:ss z')
                        : 'N/A';

                var bitrate;
                if (onlineAgent.bitrate) {
                    if (onlineAgent.bitrate < 1000000) {
                        bitrate = (onlineAgent.bitrate/1000)+' Kbit/s';
                    } else {
                        bitrate = (onlineAgent.bitrate/1000000)+' Mbit/s';
                    }
                } else {
                    bitrate = 'N/A';
                }

                htmlString +=
                        ('<tr class="table-dark">'
                                + '<th scope="row">'+onlineAgent.agent.agentId+'</th>'
                                + '<td>'+onlineAgent.agent.status+'</td>'
                                + '<td>'+lastHeartbeat+'</td>'
                                + '<td>'+bitrate+'</td>'
                                + '<td>'+inventoryLastUpdated+'</td>'
                                + '<td>'+(inventory ? inventory.movies.length : '0')+'</td>'
                                + '<td>'+(inventory ? inventory.series.length : '0')+'</td>'
                        +'</tr>');
            });

            htmlString +=
                    '</tbody>';

            $("#agentsTable").html(htmlString).text();

        }).then(function() {
            setTimeout(refresh_agents_list, 1000 * 5);
        });
    })();
</script>

    <div class="col-md-9" style="float: none; margin: 0 auto;">
        <table id="agentsTable" class="table table-hover">

        </table>
    </div>
</@layout.layout>