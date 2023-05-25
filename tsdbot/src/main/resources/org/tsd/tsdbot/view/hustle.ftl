<#-- @ftlvariable name="" type="org.tsd.tsdbot.view.HustleView" -->
<#import "layout.ftl" as layout>
<@layout.layout title="Hustin">
    <script src="https://code.highcharts.com/highcharts.src.js"></script>
    <div class="row">
        <div class="col-12">
            <h1>Maybe we should hustle as hard as we hate</h1>
            <div id="hustleChart" style="width:100%; height:400px;"></div>
        </div>
    </div>
    <script>
        $(function () {
            var myChart = Highcharts.chart( ${chartJson} );
        });
    </script>
</@layout.layout>