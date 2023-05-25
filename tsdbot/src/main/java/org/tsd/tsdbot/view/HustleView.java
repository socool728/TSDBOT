package org.tsd.tsdbot.view;

import com.googlecode.wickedcharts.highcharts.jackson.JsonRenderer;
import com.googlecode.wickedcharts.highcharts.options.*;
import com.googlecode.wickedcharts.highcharts.options.series.SimpleSeries;
import org.tsd.Constants;
import org.tsd.tsdbot.hustle.HustleDataPoint;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class HustleView extends TSDHQView {

    private final List<HustleDataPoint> dataPoints;

    public HustleView(List<HustleDataPoint> dataPoints) {
        super(Constants.View.HUSTLE_VIEW);
        this.dataPoints = dataPoints;
    }

    public String getChartJson() {
        LinkedHashMap<LocalDateTime, Double> data = new LinkedHashMap<>();
        for (HustleDataPoint dataPoint : dataPoints) {
            data.put(dataPoint.getDate(), dataPoint.getNewHhr());
        }

        List<String> xAxisPoints = data.keySet()
                .stream()
                .map(time -> time.format(DateTimeFormatter.ofPattern("MMM dd HH:mm:ss")))
                .collect(Collectors.toList());

        Options options = new Options()
                .setChartOptions(new ChartOptions().setType(SeriesType.LINE).setRenderTo("hustleChart"))
                .setTitle(new Title("Hustle-to-hate ratio"))
                .setxAxis(new Axis().setCategories(xAxisPoints))
                .setyAxis(new Axis().setTitle(new Title("Hustle/Hate Ratio")))
                .addSeries(new SimpleSeries().setName("HHR").setData(new LinkedList<>(data.values())));

        return new JsonRenderer().toJson(options);
    }
}
