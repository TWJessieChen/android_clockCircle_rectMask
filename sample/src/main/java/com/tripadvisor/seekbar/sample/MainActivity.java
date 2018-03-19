/*
 * Copyright (C) 2014 TripAdvisor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author ksarmalkar 7/28/2014
 */

package com.tripadvisor.seekbar.sample;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.tripadvisor.seekbar.ClockView;
import com.tripadvisor.seekbar.util.annotations.VisibleForTesting;

import org.joda.time.DateTime;

import java.util.ArrayList;

import static com.tripadvisor.seekbar.ClockView.ClockTimeUpdateListener;

public class MainActivity extends Activity implements Handler.Callback{

    private static ClockView sMinDepartTimeClockView;
    private static ClockView sMaxDepartTimeClockView;
    private static PlaceholderFragment sPlaceholderFragment;
    private TextView tv_startTime;
    private TextView tv_endTime;

    private ClockCircleTwoDotView clockCircleTwoDotView ;

    private RectMaskView rectMaskView;

    private Button button;

    private ListView listView;
    private String[] list = {"鉛筆","原子筆","鋼筆","毛筆","彩色筆","原子筆","鋼筆","毛筆","彩色筆","原子筆","鋼筆","毛筆","彩色筆","原子筆","鋼筆","毛筆","彩色筆","原子筆","鋼筆","毛筆","彩色筆"};
    private ArrayAdapter<String> listAdapter;

    private ArrayList<RectMaskView.PaintStatus> paintLists = new ArrayList<RectMaskView.PaintStatus>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_startTime = (TextView) findViewById(R.id.start_time);
        tv_endTime = (TextView) findViewById(R.id.end_time);
        tv_startTime.setText("00:00");
        tv_endTime.setText("24:00");

        button = (Button) findViewById(R.id.btn);

        rectMaskView = (RectMaskView) findViewById(R.id.rect_mask_view);

        for(int index = 0; index < 512; index++) {
            paintLists.add(RectMaskView.PaintStatus.UNSELECT);
        }

        rectMaskView.onDrawRect(paintLists);

        button.setOnClickListener(new Button.OnClickListener(){

            @Override

            public void onClick(View v) {
                rectMaskView = (RectMaskView) findViewById(R.id.rect_mask_view);

                paintLists.clear();

                for(int index = 0; index < 512; index++) {
                    int a = (int)(Math.random()* 3);
                    if(a == 0) {
                        paintLists.add(RectMaskView.PaintStatus.SELECT);
                    }else if(a == 1) {
                        paintLists.add(RectMaskView.PaintStatus.UNSELECT);
                    } else if(a == 2) {
                        paintLists.add(RectMaskView.PaintStatus.TRIGGER);
                    }
                }

                rectMaskView.onDrawRect(paintLists);
            }

        });

//        listView = (ListView)findViewById(R.id.list_view);
//        listAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,list);
//        listView.setAdapter(listAdapter);
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
////                Toast.makeText(getApplicationContext(), "你選擇的是" + list[position], Toast.LENGTH_SHORT).show();
//            }
//        });

        clockCircleTwoDotView = (ClockCircleTwoDotView) findViewById(R.id.clock_circle_two_dot_view);

        clockCircleTwoDotView.setClockCallBackFunction(new ClockCircleTwoDotView.ClockCallBackFunction() {
            @Override
            public void reFreshStatTimeFunction() {
//                Log.d("MainActivity","reFreshStatTimeFunction");
                if(Math.round(ClockCircleTwoDotBean.getStartAngle()/30) < 10) {
                    tv_startTime.setText("0" + Math.round(ClockCircleTwoDotBean.getStartAngle()/30) + ":00");
                }else {
                    tv_startTime.setText(Math.round(ClockCircleTwoDotBean.getStartAngle()/30) + ":00");
                }
            }

            @Override
            public void reFreshEndTimeFunction() {
//                Log.d("MainActivity","reFreshEndTimeFunction");
                if(Math.round(ClockCircleTwoDotBean.getEndAngle()/30) < 10) {
                    tv_endTime.setText("0" + Math.round(ClockCircleTwoDotBean.getEndAngle() / 30) + ":00");
                }else {
                    tv_endTime.setText(Math.round(ClockCircleTwoDotBean.getEndAngle() / 30) + ":00");
                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
//        MainController.getInstance().registerUiListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        MainController.getInstance().deregisterUiListener(this);
    }

    public static ClockView getMinDepartTimeClockView() {
        return sMinDepartTimeClockView;
    }

    public static ClockView getMaxDepartTimeClockView() {
        return sMaxDepartTimeClockView;
    }

    public static PlaceholderFragment getPlaceholderFragment() {
        return sPlaceholderFragment;
    }

    @Override
    public boolean handleMessage(Message msg) {

        switch (msg.what) {
            case MainController.MSG_START_TIME:

                if(Math.round(Constants.getStartAngle()/30) < 10) {
                    tv_startTime.setText("0" + Math.round(Constants.getStartAngle()/30) + ":00");
                }else {
                    tv_startTime.setText(Math.round(Constants.getStartAngle()/30) + ":00");
                }

                break;
            case MainController.MSG_END_TIME:

                if(Math.round(Constants.getEndAngle()/30) < 10) {
                    tv_endTime.setText("0" + Math.round(Constants.getEndAngle() / 30) + ":00");
                }else {
                    tv_endTime.setText(Math.round(Constants.getEndAngle() / 30) + ":00");
                }
                break;
        }

        return false;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements ClockTimeUpdateListener {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            final DateTime minTime = new DateTime(2014, 4, 25, 7, 0);
            final DateTime maxTime = new DateTime(2014, 4, 26, 5, 0);

            sMinDepartTimeClockView = (ClockView) rootView.findViewById(R.id.min_depart_time_clock_view);
            sMinDepartTimeClockView.setBounds(minTime, maxTime, false);

            sMaxDepartTimeClockView = (ClockView) rootView.findViewById(R.id.max_depart_time_clock_view);
            sMaxDepartTimeClockView.setBounds(minTime, maxTime, true);

            ClockView mMinArrivalTimeClockView = (ClockView) rootView.findViewById(R.id
                    .min_arrive_time_clock_view);
            mMinArrivalTimeClockView.setBounds(minTime, maxTime, false);
            mMinArrivalTimeClockView.setNewCurrentTime(new DateTime(2014, 4, 25, 10, 0));
            ClockView mMaxArrivalTimeClockView = (ClockView) rootView.findViewById(R.id
                    .max_arrive_time_clock_view);
            mMaxArrivalTimeClockView.setBounds(minTime, maxTime, true);
            mMaxArrivalTimeClockView.setNewCurrentTime(new DateTime(2014, 4, 25, 10, 0));

            final ClockView minRandomTime = (ClockView) rootView.findViewById(R.id.min_random_time_clock_view);
            minRandomTime.setBounds(minTime, maxTime, false);
            minRandomTime.setNewCurrentTime(new DateTime(2014, 4, 25, 10, 0));

            final ClockView maxRandomTime = (ClockView) rootView.findViewById(R.id.max_random_time_clock_view);
            maxRandomTime.setBounds(minTime, maxTime, false);
            maxRandomTime.setNewCurrentTime(new DateTime(2014, 4, 25, 10, 0));

            return rootView;
        }

        @VisibleForTesting
        public void changeClockTimeForTests(DateTime dateTime, boolean isMaxTime) {
            if (isMaxTime) {
                sMaxDepartTimeClockView.setClockTimeUpdateListener(this);
                sMaxDepartTimeClockView.setNewCurrentTime(dateTime);
            } else {
                sMinDepartTimeClockView.setClockTimeUpdateListener(this);
                sMinDepartTimeClockView.setNewCurrentTime(dateTime);
            }
        }

        @Override
        public void onClockTimeUpdate(ClockView clockView, DateTime currentTime) {
            if (clockView.equals(sMinDepartTimeClockView)) {
                if (currentTime.compareTo(sMaxDepartTimeClockView.getNewCurrentTime()) >= 0) {
                    sMinDepartTimeClockView.setNewCurrentTime(sMinDepartTimeClockView.getNewCurrentTime().minusHours(1));
                }
            } else if (clockView.equals(sMaxDepartTimeClockView)) {
                if (currentTime.compareTo(sMinDepartTimeClockView.getNewCurrentTime()) <= 0) {
                    sMaxDepartTimeClockView.setNewCurrentTime(sMaxDepartTimeClockView.getNewCurrentTime().plusHours(1));
                }
            }
        }
    }
}
