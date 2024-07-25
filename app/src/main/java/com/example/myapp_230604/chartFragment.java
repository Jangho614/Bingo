package com.example.myapp_230604;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class chartFragment extends Fragment {

    private View rootView;
    private PieChart pieChart;
    private HorizontalBarChart horizontalBarChart;
    private BarChart barChart;

    private HorizontalBarChart chart;

    private TextView metalTextView;
    private TextView plasticTextView;
    private TextView paperTextView;
    private TextView glassTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_chart, container, false);

        pieChart = rootView.findViewById(R.id.chart1);
        horizontalBarChart = rootView.findViewById(R.id.chart2);
        barChart = rootView.findViewById(R.id.chart3);
        chart = rootView.findViewById(R.id.chart4);

        metalTextView = rootView.findViewById(R.id.metalTextView);
        plasticTextView = rootView.findViewById(R.id.plasticTextView);
        paperTextView = rootView.findViewById(R.id.paperTextView);
        glassTextView = rootView.findViewById(R.id.glassTextView);

        ViewGroup.LayoutParams params = pieChart.getLayoutParams();
        pieChart.setLayoutParams(params);


        horizontalBarChart.getXAxis().setDrawGridLines(false);
        horizontalBarChart.getAxisLeft().setDrawGridLines(true);
        horizontalBarChart.getAxisRight().setDrawGridLines(true);
        //barChart.setExtraOffsets(10f, 10f, 10f, 10f);

        barChart.getXAxis().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setDrawGridLines(false);

        setupPieChart();
        setupBarChart2(horizontalBarChart);
        setupBarChart1(barChart);
        setupBarChart(chart);

        return rootView;
    }
    private void setupPieChart() {
        pieChart.setDrawHoleEnabled(false);
        pieChart.setHoleRadius(10f);

        pieChart.setTouchEnabled(true);
        pieChart.getDescription().setEnabled(false);

        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setEntryLabelTextSize(15);
        pieChart.setEntryLabelColor(Color.BLACK);


        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(10f, "금속"));
        entries.add(new PieEntry(40f, "플라스틱"));
        entries.add(new PieEntry(30f, "종이"));
        entries.add(new PieEntry(20f, "유리"));


        PieDataSet dataSet = new PieDataSet(entries, "분리수거 통계");
        dataSet.setValueTextSize(0f);
        dataSet.setColors(Color.rgb(84, 125, 92),
                Color.rgb(36, 227, 87),
                Color.rgb(23, 207, 164),
                Color.rgb(8, 252, 138));

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate();

        updateTextViews(entries);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);
    }
    private void updateTextViews(List<PieEntry> entries) {
        setColoredText(metalTextView, "금속: ", Color.rgb(84, 125, 92), entries.get(0).getValue());
        setColoredText(plasticTextView, "플라스틱: ", Color.rgb(36, 227, 87), entries.get(1).getValue());
        setColoredText(paperTextView, "종이: ", Color.rgb(23, 207, 164), entries.get(2).getValue());
        setColoredText(glassTextView, "유리: ", Color.rgb(8, 252, 138), entries.get(3).getValue());
    }

    private void setColoredText(TextView textView, String prefix, int color, float value) {
        String text = prefix + value;
        SpannableString spannableString = new SpannableString(text);

        // 특정 범위의 문자에 색상을 적용
        spannableString.setSpan(new ForegroundColorSpan(color), prefix.length(), text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(spannableString);
    }

    private void setupBarChart2(BarChart barChart) {
        horizontalBarChart.setTouchEnabled(true);
        horizontalBarChart.setPinchZoom(true);
        horizontalBarChart.getDescription().setEnabled(false);
        horizontalBarChart.setDrawGridBackground(false);
        horizontalBarChart.setDrawBorders(false);

        List<BarEntry> entries = new ArrayList<>();

        entries.add(new BarEntry(0, 74, "금속"));
        entries.add(new BarEntry(1, 43, "플라스틱"));
        entries.add(new BarEntry(2, 85, "종이"));
        entries.add(new BarEntry(3, 54, "유리"));

        BarDataSet dataSet = new BarDataSet(entries, "재질");
        dataSet.setDrawValues(true);

        dataSet.setColors(Color.rgb(84, 125, 92),
                Color.rgb(36, 227, 87),
                Color.rgb(23, 207, 164),
                Color.rgb(8, 252, 138));

        BarData data = new BarData(dataSet);
        horizontalBarChart.setData(data);
        horizontalBarChart.invalidate();

        horizontalBarChart.getAxisRight().setEnabled(false);

        YAxis yAxis = horizontalBarChart.getAxisLeft();
        yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        yAxis.setAxisMinimum(1f);

        XAxis xAxis = horizontalBarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"금속", "플라스틱", "종이", "유리"}));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(12f);

        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        Legend legend = barChart.getLegend();
        legend.setEnabled(false);
    }


    private void setupBarChart1(BarChart barChart1) {
        this.barChart.setTouchEnabled(true);
        this.barChart.setPinchZoom(true);
        this.barChart.getDescription().setEnabled(false);

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 10f, "월"));
        entries.add(new BarEntry(1, 40f, "화"));
        entries.add(new BarEntry(2, 30f, "수"));
        entries.add(new BarEntry(3, 50f, "목"));
        entries.add(new BarEntry(4, 70f, "금"));
        entries.add(new BarEntry(5, 30f, "토"));
        entries.add(new BarEntry(6, 60f, "일"));

        BarDataSet dataSet = new BarDataSet(entries, "요일");
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(10f);
        dataSet.setColors(Color.rgb(84, 125, 92),
                Color.rgb(36, 227, 87),
                Color.rgb(23, 207, 164),
                Color.rgb(53, 255, 108),
                Color.rgb(5, 147, 95),
                Color.rgb(35, 250, 40),
                Color.rgb(34, 252, 154));

        BarData data = new BarData(dataSet);
        this.barChart.setData(data);
        this.barChart.invalidate();
        this.barChart.animateY(1500);
        this.barChart.setExtraOffsets(0, 0, 0, 5);

        XAxis xAxis = this.barChart.getXAxis();
        xAxis.setTextSize(12f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"월","화","수","목","금","토","일"}));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

        Legend legend = this.barChart.getLegend();
        legend.setEnabled(false);
    }
    private void setupBarChart(HorizontalBarChart barChart) {
        List<BarEntry> entries = new ArrayList<>();
        float pos_value = 30f;
        float neg_value = -70f;

        entries.add(new BarEntry(0, pos_value)); // 왼쪽 막대
        entries.add(new BarEntry(0, neg_value)); // 오른쪽 막대 (음수로 설정하여 왼쪽으로 이동)

        BarDataSet dataSet = new BarDataSet(entries, "Values");
        dataSet.setColors(new int[]{Color.rgb(99, 224, 94),Color.rgb(191, 34, 50)});

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        // 막대의 너비 설정
        float barWidth = 9f; // 예시로 0.5f로 설정
        barData.setBarWidth(barWidth);

        barChart.getAxisLeft().setDrawLabels(false); // y축 레이블 숨기기
        barChart.getAxisLeft().setDrawGridLines(false); // y축 그리드 선 숨기기
        barChart.getAxisLeft().setDrawAxisLine(false); // y축 축선 숨기기
        barChart.getXAxis().setDrawLabels(false); // x축 레이블 숨기기
        barChart.getXAxis().setDrawGridLines(false); // x축 그리드 선 숨기기
        barChart.getXAxis().setDrawAxisLine(false); // x축 축선 숨기기

        // y축의 중앙으로 설정
        barChart.getAxisLeft().setAxisMinimum(neg_value);
        barChart.getAxisLeft().setAxisMaximum(pos_value);
        barChart.getAxisRight().setEnabled(false);

        // x축 설정
        barChart.getXAxis().setAxisMinimum(-10f);
        barChart.getXAxis().setAxisMaximum(10f);
        barChart.getXAxis().setDrawGridLines(false);

        barChart.invalidate(); // 차트 갱신
    }


}

