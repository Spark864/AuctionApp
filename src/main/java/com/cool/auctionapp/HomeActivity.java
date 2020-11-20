package com.cool.auctionapp;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cool.AuctionApp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private NavigationView navigationView;
    private RecyclerView recyclerView;

    private DatabaseReference mDatabase;
    private FirebaseRecyclerAdapter<Post, PostViewHolder> mAdapter;

    private ProgressBar progressBar;

    private Button btnEnterBid;
    private Dialog MyDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Posts");

        mDrawerLayout = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.nav_view);
        recyclerView = findViewById(R.id.recycler_view_home);

        progressBar = findViewById(R.id.progress_bar_home);

        navigationView.setNavigationItemSelectedListener(this);

        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void AlertDialog(final PostViewHolder holder){
        MyDialog = new Dialog(HomeActivity.this);
        MyDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        MyDialog.setContentView(R.layout.bid_dialog);
        MyDialog.setTitle("Bid");

        btnEnterBid = MyDialog.findViewById(R.id.btnEnterBidDialog);
        final EditText editEnterBid = MyDialog.findViewById(R.id.editEnterBid);

        btnEnterBid.setEnabled(true);

        btnEnterBid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.bidAmount = editEnterBid.getText().toString().trim();
                holder.didBid = true;
                Toast.makeText(getApplicationContext(), "Your Bid is: " + holder.bidAmount, Toast.LENGTH_SHORT).show();
                MyDialog.dismiss();
            }
        });

        MyDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        progressBar.setVisibility(View.VISIBLE);
        progressBar.bringToFront();
        recyclerView.setVisibility(View.GONE);

        FirebaseRecyclerOptions<Post> options = new FirebaseRecyclerOptions.Builder<Post>()
                .setQuery(mDatabase, Post.class)
                .build();


        mAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final PostViewHolder holder, int position, @NonNull Post model) {
                holder.itemName.setText(model.getItemName());
                holder.itemDesc.setText(model.getDescription());
                holder.baseBid.setText("Base Bid: Rs." + model.getBaseBid());
                holder.startTime.setText("Start Time: " + model.getStartTime());
                holder.endTime.setText("End Time: " + model.getEndTime());

                Picasso.get().load(model.getImageUrl()).into(holder.imageView);

                holder.bidBn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!holder.didBid)
                            AlertDialog(holder);
                        else
                            Toast.makeText(getApplicationContext(), "Your Bid is: " + holder.bidAmount, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @NonNull
            @Override
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.model_post, viewGroup, false);

                return new PostViewHolder(view);

            }
        };

        mAdapter.startListening();
        recyclerView.setAdapter(mAdapter);

        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView itemName;
        TextView itemDesc;
        TextView baseBid;
        TextView startTime;
        TextView endTime;
        ImageView imageView;
        Button bidBn;
        boolean didBid;
        String bidAmount;

        public PostViewHolder(View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            itemDesc = itemView.findViewById(R.id.description);
            baseBid = itemView.findViewById(R.id.base_bid);
            startTime = itemView.findViewById(R.id.start_time);
            endTime = itemView.findViewById(R.id.end_time);
            imageView = itemView.findViewById(R.id.image_view);
            bidBn = itemView.findViewById(R.id.btnEnterBid);
            didBid = false;
            bidAmount = "";
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add) {

             Intent AddPostActivity = new Intent(HomeActivity.this, AddPostActivity.class);
             startActivity(AddPostActivity);

        } else if (id == R.id.logout) {

            FirebaseAuth.getInstance().signOut();
            Intent loginActivity = new Intent(HomeActivity.this, LogInActivity.class);
            startActivity(loginActivity);
            finish();

        }

        DrawerLayout drawer = findViewById(R.id.drawer);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}


