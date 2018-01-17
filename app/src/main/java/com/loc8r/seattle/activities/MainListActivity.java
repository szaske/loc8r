package com.loc8r.seattle.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.loc8r.seattle.R;
import com.loc8r.seattle.adapters.Main_List_Adapter;
import com.loc8r.seattle.models.ListItem;

import java.util.ArrayList;
import java.util.List;

public class MainListActivity extends LoggedInActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);


        //Implement a List in the Activity view
        List<ListItem> data = fill_with_data();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rvMainList);
        Main_List_Adapter adapter = new Main_List_Adapter(data, new Main_List_Adapter.OnMenuClickListener() {
            @Override public void OnMenuClick(ListItem item) {
                clicked(item);
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private List<ListItem> fill_with_data() {

        List<ListItem> data = new ArrayList<>();

        data.add(new ListItem("Explore Seattle", R.drawable.ice_cream_icon));
        data.add(new ListItem("Seattle Passport", R.drawable.ice_cream_icon));
        data.add(new ListItem("Settings", R.drawable.ice_cream_icon));

        return data;
    }

    private void clicked(ListItem item){
        switch (item.getTitle()) {
            case "Number 2":
                Intent intent = new Intent(MainListActivity.this, NearbyPOIActivity.class);
                startActivity(intent);
                break;
            default:
                Toast.makeText(getApplicationContext(), "Item " + item.getTitle() + " Clicked", Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem mapItem = menu.findItem(R.id.menu_map);

        return super.onPrepareOptionsMenu(menu);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu)
//    {
//        super.onCreateOptionsMenu(menu);
//        getMenuInflater().inflate(R.menu.options_menu, menu);
//        return true;
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_filter:
                //showFilterDialog();
                break;
            case R.id.menu_map:
                //openMap();
                break;
            case R.id.menu_log_out:
                //showLogoutDialog();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


}
