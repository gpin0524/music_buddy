package com.github.pwittchen.neurosky.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class FirstTimeIntroActivity  extends Activity
{

    Button btn_endintro;

    Intent i;

    private ViewPager viewPager; //宣告 ViewPager 元件
    private ArrayList<View> viewPager_List;
    private MyViewPagerAdapter mAdapter;

    private RadioGroup radioGroup;
    private RadioButton radioButton1,
            radioButton2,
            radioButton3,
            radioButton4,
            radioButton5,
            radioButton6,
        radioButton7,
        radioButton8,
        radioButton9,
        radioButton10,
        radioButton11;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time_intro);

        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioButton1 = (RadioButton) findViewById(R.id.radioButton1);
        radioButton2 = (RadioButton) findViewById(R.id.radioButton2);
        radioButton3 = (RadioButton) findViewById(R.id.radioButton3);
        radioButton4 = (RadioButton) findViewById(R.id.radioButton4);
        radioButton5 = (RadioButton) findViewById(R.id.radioButton5);
        radioButton6 = (RadioButton) findViewById(R.id.radioButton6);
        radioButton6 = (RadioButton) findViewById(R.id.radioButton7);
        radioButton6 = (RadioButton) findViewById(R.id.radioButton8);
        radioButton6 = (RadioButton) findViewById(R.id.radioButton9);
        radioButton6 = (RadioButton) findViewById(R.id.radioButton10);
        radioButton6 = (RadioButton) findViewById(R.id.radioButton11);

        btn_endintro = findViewById(R.id.btn_endintro);

        viewPager = (ViewPager) findViewById(R.id.pager);

        final LayoutInflater mInflater = getLayoutInflater().from(this);

//宣告四個 View 以儲存四種不同 layout。
        View viewPager1 = mInflater.inflate(R.layout.first_time_intro_page, null);
        View viewPager2 = mInflater.inflate(R.layout.first_time_intro_page2, null);
        View viewPager3 = mInflater.inflate(R.layout.first_time_intro_page3, null);
        View viewPager4 = mInflater.inflate(R.layout.first_time_intro_page4, null);
        View viewPager5 = mInflater.inflate(R.layout.first_time_intro_page5, null);
        View viewPager6 = mInflater.inflate(R.layout.first_time_intro_page6, null);
        View viewPager7 = mInflater.inflate(R.layout.first_time_intro_page7, null);
        View viewPager8 = mInflater.inflate(R.layout.first_time_intro_page8, null);
        View viewPager9 = mInflater.inflate(R.layout.first_time_intro_page9, null);
        View viewPager10 = mInflater.inflate(R.layout.first_time_intro_page10, null);
        View viewPager11 = mInflater.inflate(R.layout.first_time_intro_page11, null);

        viewPager_List = new ArrayList<View>();
        viewPager_List.add(viewPager1);
        viewPager_List.add(viewPager2);
        viewPager_List.add(viewPager3);
        viewPager_List.add(viewPager4);
        viewPager_List.add(viewPager5);
        viewPager_List.add(viewPager6);
        viewPager_List.add(viewPager7);
        viewPager_List.add(viewPager8);
        viewPager_List.add(viewPager9);
        viewPager_List.add(viewPager10);
        viewPager_List.add(viewPager11); //將四種不同 layout 加入 Arraylist 中

        viewPager.setAdapter(new MyViewPagerAdapter(viewPager_List)); //將 Arratlist 設定給 viewPager
        viewPager.setCurrentItem(0);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() //ViewPager 頁面滑動監聽器
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {}

            @Override
            public void onPageSelected(int position) //當頁面滑動到其中一頁時，觸發該頁對應的 RadioButton 按鈕
            {
                switch (position)
                {
                    case 0:
                        radioGroup.check(R.id.radioButton1);
                        break;

                    case 1:
                        radioGroup.check(R.id.radioButton2);
                        break;

                    case 2:
                        radioGroup.check(R.id.radioButton3);
                        break;

                    case 3:
                        radioGroup.check(R.id.radioButton4);
                        break;

                    case 4:
                        radioGroup.check(R.id.radioButton5);
                        break;

                    case 5:
                        radioGroup.check(R.id.radioButton6);
                        break;

                    case 6:
                        radioGroup.check(R.id.radioButton7);
                        break;

                    case 7:
                        radioGroup.check(R.id.radioButton8);
                        break;

                    case 8:
                        radioGroup.check(R.id.radioButton9);
                        break;

                    case 9:
                        radioGroup.check(R.id.radioButton10);
                        break;

                    case 10:
                        radioGroup.check(R.id.radioButton11);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {}
        });

        btn_endintro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i = new Intent(FirstTimeIntroActivity.this, SituationSelect.class);
                startActivity(i);
            }
        });
    }

    public class MyViewPagerAdapter extends PagerAdapter
    {
        private List<View> mListViews;

        public MyViewPagerAdapter(List<View> mListViews)
        {
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object)
        {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position)
        {
            View view = mListViews.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public int getCount()
        {
            return  mListViews.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1)
        {
            return arg0 == arg1;
        }
    }
}










/***public class FirstTimeIntroActivity extends Activity {
    /** Called when the activity is first created.
    private ViewPager viewPager;
    private List<PageView> pageList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time_intro);

        viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(new SamplePagerAdapter());

        pageList = new ArrayList<>();
        pageList.add(new PageOneView(FirstTimeIntroActivity.this));
        pageList.add(new PageTwoView(FirstTimeIntroActivity.this));
        pageList.add(new PageThreeView(FirstTimeIntroActivity.this));
        pageList.add(new PageFourView(FirstTimeIntroActivity.this));
        pageList.add(new PageFiveView(FirstTimeIntroActivity.this));
        pageList.add(new PageSixView(FirstTimeIntroActivity.this));

        for(PageView view : pageList){
            view.refreshDrawableState();
        }
    }

    public class PageView extends RelativeLayout {
        public PageView(Context context) {
            super(context);
        }
    }

    public class PageOneView extends PageView{
        public PageOneView(Context context) {
            super(context);
            View view = LayoutInflater.from(context).inflate(R.layout.first_time_intro_page, null);
            ImageView imageView = view.findViewById(R.id.imageView);
            imageView.setImageResource(R.drawable.firstIntro_1);
            addView(view);
        }
    }
    public class PageTwoView extends PageView{
        public PageTwoView(Context context) {
            super(context);
            View view = LayoutInflater.from(context).inflate(R.layout.first_time_intro_page, null);
            ImageView imageView = view.findViewById(R.id.imageView);
            imageView.setImageResource(R.drawable.firstIntro_2);
            addView(view);
        }
    }
    public class PageThreeView extends PageView{
        public PageThreeView(Context context) {
            super(context);
            View view = LayoutInflater.from(context).inflate(R.layout.first_time_intro_page, null);
            ImageView imageView = view.findViewById(R.id.imageView);
            imageView.setImageResource(R.drawable.firstIntro_3);
            addView(view);
        }
    }
    public class PageFourView extends PageView{
        public PageFourView(Context context) {
            super(context);
            View view = LayoutInflater.from(context).inflate(R.layout.first_time_intro_page, null);
            ImageView imageView = view.findViewById(R.id.imageView);
            imageView.setImageResource(R.drawable.firstIntro_4);
            addView(view);
        }
    }
    public class PageFiveView extends PageView{
        public PageFiveView(Context context) {
            super(context);
            View view = LayoutInflater.from(context).inflate(R.layout.first_time_intro_page, null);
            ImageView imageView = view.findViewById(R.id.imageView);
            imageView.setImageResource(R.drawable.firstIntro_5);
            addView(view);
        }
    }
    public class PageSixView extends PageView{
        public PageSixView(Context context) {
            super(context);
            View view = LayoutInflater.from(context).inflate(R.layout.first_time_intro_page, null);
            ImageView imageView = view.findViewById(R.id.imageView);
            imageView.setImageResource(R.drawable.firstIntro_6);
            addView(view);
        }
    }

    private class SamplePagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return pageList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(pageList.get(position));
            return pageList.get(position);
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    }
}*/