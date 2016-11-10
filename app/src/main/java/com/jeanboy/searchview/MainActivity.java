package com.jeanboy.searchview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.jeanboy.searchview.view.SearchView;

public class MainActivity extends AppCompatActivity {

    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (searchView.isSearching()) {
                    searchView.stopSearch();
                } else {
                    searchView.startSearch();
                }
            }
        });
    }
}
