package gachon.termproject.joker.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import gachon.termproject.joker.Content.NotificationContent;
import gachon.termproject.joker.Content.PostContent;
import gachon.termproject.joker.R;
import gachon.termproject.joker.UserInfo;
import gachon.termproject.joker.adapter.PostCommentAdapter;
import gachon.termproject.joker.Content.PostCommentContent;
import gachon.termproject.joker.FirebaseDeleter;
import gachon.termproject.joker.fragment.MyInfoFragment;

public class SeePostActivity extends AppCompatActivity {
    public static DatabaseReference databaseReference;
    public static ValueEventListener commentsListener;
    private LinearLayout container;
    private RecyclerView commentSection;
    private SwipeRefreshLayout refresher;
    private ArrayList<PostCommentContent> postCommentList;
    private PostContent postContent;
    private PostCommentAdapter postCommentAdapter;
    private PostCommentContent postComment;
    private boolean isWriter;
    private Intent intent;
    private String pushToken;
    private String postId;
    private String category;
    private String userId;
    private String profileImg;
    private String nickname;
    private String expertName;
    private String intro;
    private String comment;
    private String expertId;
    private ArrayList<String> content;
    private ArrayList<String> images;
    public ArrayList<String> location;
    private List<ImageView> iv = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_post);

        //toolbar??? activity bar??? ??????!
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true); //?????? ?????????????
        actionBar.setDisplayShowTitleEnabled(false); //?????? ?????? ??????

        // ????????? ????????? ????????????
        intent = getIntent();
        category = intent.getStringExtra("category");
        postId = intent.getStringExtra("postId");
        pushToken = intent.getStringExtra("pushToken");
        userId = intent.getStringExtra("userId");
        profileImg = intent.getStringExtra("profileImg");
        nickname = intent.getStringExtra("nickname");
        intro = intent.getStringExtra("intro");
        content = intent.getStringArrayListExtra("content");
        images = intent.getStringArrayListExtra("images");
        location = intent.getStringArrayListExtra("location");
        postContent = intent.getParcelableExtra("postContent");

        // ????????? ?????? ??????
        if (MainActivity.userPostsIdList != null) {
            for (String myPostId : MainActivity.userPostsIdList) {
                if (postId.equals(myPostId))
                    isWriter = true;
            }
        }


        refresher = findViewById(R.id.refresh_layout);

        // ??????, ?????????, ???????????? ??????
        TextView title = findViewById(R.id.title);
        TextView nickname = findViewById(R.id.postNickname);
        TextView time = findViewById(R.id.postTime);
        title.setText(intent.getStringExtra("title"));
        nickname.setText(intent.getStringExtra("nickname"));
        time.setText(intent.getStringExtra("time"));

        // ????????? ?????? ?????? (image ????????????)
        ImageView profile = findViewById(R.id.postProfile);
        profile.setBackground(new ShapeDrawable(new OvalShape()));
        profile.setClipToOutline(true);
        if (!profileImg.equals("None"))
            Glide.with(this).load(profileImg).into(profile);

        TextView expert_name = findViewById(R.id.seepost_review_expertname);
        View line1 = findViewById(R.id.seepost_review_line1);
        View line2 = findViewById(R.id.seepost_review_line2);
        View margin = findViewById(R.id.seepost_formargin);

        //??????????????? ????????? ?????? ??????
        if (category.equals("review")) {
            expert_name.setVisibility(View.VISIBLE);
            line1.setVisibility(View.VISIBLE);
            line2.setVisibility(View.VISIBLE);
            margin.setVisibility(View.GONE);

            expertId = intent.getStringExtra("expertId");

            DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(expertId);

            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // ????????? ?????????, ????????? ?????? Url ??? ????????????
                            expertName = document.getString("nickname");
                            expert_name.setText("????????? : " + expertName);
                        }
                    }
                }
            });

        } else {
            expert_name.setVisibility(View.GONE);
            line1.setVisibility(View.GONE);
            line2.setVisibility(View.GONE);
            margin.setVisibility(View.VISIBLE);
        }

        // ????????? ?????? ?????? ?????? ??????
        container = findViewById(R.id.seepost_content);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        //TextView ?????? ??? layout_width, layout_height, gravity, ?????? ??? ??????
        TextView text_content = new TextView(SeePostActivity.this);
        text_content.setLayoutParams(lp);
        text_content.setText(content.get(0));
        text_content.setTextSize(dpToPx(7));
        text_content.setTextColor(Color.BLACK);

        // ??? ??????
        container.addView(text_content);

        // ????????? ????????? ??????
        if (images != null) {
            LinearLayout imageContainer = findViewById(R.id.seepost_imagecontainer);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dpToPx(150), dpToPx(150));
            layoutParams.setMargins(dpToPx(10), 0, dpToPx(10), 0);

            // imageView ?????????
            for (int i = 0; i < images.size(); i++) {
                if (images.get(0).compareTo("") == 0) break;

                ImageView imageView = new ImageView(SeePostActivity.this);
                imageView.setLayoutParams(layoutParams);
                imageView.setScaleType(ImageView.ScaleType.CENTER);
                Glide.with(SeePostActivity.this).load(images.get(i)).into(imageView);
                imageView.setId(i);
                iv.add(imageView);

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), FullScreenImageActivity.class);
                        for (int j = 0; j < images.size(); j++) {
                            if (v.getId() == iv.get(j).getId()) {
                                intent.putExtra("img", images.get(j));
                            }
                        }
                        startActivity(intent);
                    }
                });

                imageContainer.addView(imageView);
            }
        }

        // ?????? ????????????
        databaseReference = FirebaseDatabase.getInstance().getReference("Posts/" + category + "/" + postId + "/comments");
        commentSection = findViewById(R.id.comment_listview);

        postCommentList = new ArrayList<>();
        postCommentAdapter = new PostCommentAdapter(getApplicationContext(), postCommentList, databaseReference);

        commentSection.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        commentSection.setHasFixedSize(true);
        commentSection.setAdapter(postCommentAdapter);

        commentsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postCommentList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    postComment = snapshot.getValue(PostCommentContent.class);
                    postComment.setUserId(snapshot.child("userId").getValue().toString());
                    postCommentList.add(postComment);
                }
                postCommentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        databaseReference.addListenerForSingleValueEvent(commentsListener);

        // ?????? ???????????? ????????? ??????
        refresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                databaseReference.addListenerForSingleValueEvent(commentsListener);
                refresher.setRefreshing(false);
            }
        });


        // ?????? ??????
        ImageButton uploadComment = findViewById(R.id.see_post_comment_send_button);
        uploadComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText commentContent = findViewById(R.id.see_post_comment_text);
                comment = commentContent.getText().toString();
                Date currentTime = new Date();
                String updateTime = new SimpleDateFormat("yyyy-MM-dd k:mm", Locale.getDefault()).format(currentTime);
                String commentId = String.valueOf(System.currentTimeMillis());
                PostCommentContent postCommentContent = new PostCommentContent(category, UserInfo.getUserId(), UserInfo.getNickname(), UserInfo.getProfileImg(), updateTime, commentId, comment, UserInfo.getIntroduction(), UserInfo.getPushToken(), UserInfo.getLocation());

                //????????? ?????????
                InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                if (comment.length() == 0) {
                    Toast.makeText(getApplicationContext(), "1??? ?????? ????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // DB??? ?????????
                databaseReference.child(commentId).setValue(postCommentContent)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                MainActivity.userCommentsIdList.add(0, commentId);
                                int length = MainActivity.postsOfCommentsList.size();
                                if (length == 0) {
                                    MainActivity.postsOfCommentsList.add(0, postContent);
                                } else {
                                    for (int i = 0; i < length; i++) {
                                        if (MainActivity.postsOfCommentsList.get(i).getPostId().equals(postId))
                                            break;
                                        else if (i == length - 1)
                                            MainActivity.postsOfCommentsList.add(0, postContent);
                                    }
                                }
                                if (MyInfoFragment.comment != null)
                                    MyInfoFragment.comment.adapter.notifyDataSetChanged();

                                if (!isWriter) sendFCM();
                                databaseReference.addListenerForSingleValueEvent(commentsListener);
                            }
                        });

                //?????? ????????????
                commentContent.setText("");
            }
        });
    }

    //?????? ?????? ??????
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            //????????? ??? ????????? - ??????
            case R.id.delete:
                FirebaseDeleter.postDelete(this, "Posts", category, postId, images);
                finish();
                break;

            //?????? ??? ????????? - ??????????????? / ??????
            case R.id.show_profile:
                Intent intent2 = new Intent(getApplicationContext(), SeeProfileActivity.class);
                intent2.putExtra("userId", userId);
                intent2.putExtra("nickname", nickname);
                intent2.putExtra("profileImg", profileImg);
                intent2.putExtra("intro", intro);
                intent2.putExtra("pushToken", pushToken);
                intent2.putExtra("expertId", expertId);
                intent2.putStringArrayListExtra("location", location);
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent2);
                break;
            case R.id.decelerate:
                Toast.makeText(getApplicationContext(), intent.getStringExtra("nickname") + "(???)??? ?????????????????????.", Toast.LENGTH_SHORT).show();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //?????? ???????????? my post menu, ?????? ??? ????????? other post menu??? ???????????? ?????????
        MenuInflater inflater = getMenuInflater();

        if (isWriter)
            inflater.inflate(R.menu.my_post_menu, menu);
        else
            inflater.inflate(R.menu.others_post_menu, menu);

        // To display icon on overflow menu
        if (menu instanceof MenuBuilder) {
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void sendFCM() {
        Gson gson = new Gson();

        NotificationContent notificationContent = new NotificationContent();
        notificationContent.to = pushToken;
        notificationContent.notification.title = "?????? ??????";
        notificationContent.notification.body = UserInfo.getNickname() + "?????? ????????? ???????????????.";
        notificationContent.data.title = "?????? ??????";
        notificationContent.data.body = UserInfo.getNickname() + "?????? ????????? ???????????????.";


        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), gson.toJson(notificationContent));
        Request request = new Request.Builder().header("Content-Type", "application/json")
                .addHeader("Authorization", "key=AAAAm5WD8Bo:APA91bFr1BYENkzDe9KpX7JCk50IPp3ZtVc8LKSUvMmCxAZVadIB76K1OveBIm027j7ZH3naHZ65tuc9KeTNBqyWLOh6Ox0EyeRtBx2IdpVkl0n8ihZUMLY-I32WWAdObT-Mq-k2SUxV")
                .url("https://fcm.googleapis.com/fcm/send")
                .post(requestBody).build();

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {

            }
        });
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}