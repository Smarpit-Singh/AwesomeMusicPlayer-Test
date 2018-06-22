package com.example.android.awesomemusicplayer.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.example.android.awesomemusicplayer.R;
import com.example.android.awesomemusicplayer.adapter.SectionsPageAdapter;
import com.example.android.awesomemusicplayer.fragments.FragmentAlbum;
import com.example.android.awesomemusicplayer.fragments.FragmentPlayList;


public class TabActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    int pos;
    private SectionsPageAdapter mSectionsPageAdapter;
    TabLayout tabLayout;
    private ViewPager mViewPager;

    FragmentPlayList fragmentPlayList = new FragmentPlayList();
    FragmentAlbum albumFragment = new FragmentAlbum();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Awesome Player Test");

        mSectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        setupViewPager(mViewPager);

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

    }


    private void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(fragmentPlayList, "Songs");
        adapter.addFragment(albumFragment, "Albums");

        viewPager.setAdapter(adapter);
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {

            startActivity(new Intent(getApplicationContext(), SearchActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}