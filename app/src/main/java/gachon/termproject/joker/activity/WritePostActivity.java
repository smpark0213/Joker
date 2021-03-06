package gachon.termproject.joker.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import gachon.termproject.joker.PostImage;
import gachon.termproject.joker.Content.PostContent;
import gachon.termproject.joker.R;
import gachon.termproject.joker.UserInfo;
import gachon.termproject.joker.fragment.CommunityFreeFragment;
import gachon.termproject.joker.fragment.CommunityReviewFragment;
import gachon.termproject.joker.fragment.CommunityTipFragment;
import gachon.termproject.joker.fragment.MyInfoFragment;

public class WritePostActivity extends AppCompatActivity {
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private PostContent postContent;
    private Uri image;
    private ArrayList<String> contentList = new ArrayList<>();
    private ArrayList<Uri> imagesList = new ArrayList<>();
    private String userId = UserInfo.getUserId();
    private String nickname = UserInfo.getNickname();
    private String postId;
    private String expertId;
    private LinearLayout layout;
    private EditText title, content;
    private ImageButton imageAddButton;
    private Button register;
    private ArrayList<String> imagesUrl = new ArrayList<>();
    private int uploadFinishCount = 0;
    private RelativeLayout loaderLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);

        //toolbar??? activity bar??? ??????!
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true); //?????? ???????????? ????????? => ????????? ????????? ????????????
        actionBar.setHomeAsUpIndicator(R.drawable.close_grey_24x24);
        actionBar.setDisplayShowTitleEnabled(false); //?????? ??????
        TextView textview = findViewById(R.id.writepost_toolbar_textview);
        textview.setText("????????? ??????");

        // ???????????? ????????????
        loaderLayout = findViewById(R.id.loaderLayout);
        loaderLayout.setVisibility(View.GONE);


        layout = findViewById(R.id.writepost_layout);
        title = findViewById(R.id.writepost_title);
        content = findViewById(R.id.writepost_content);
        imageAddButton = findViewById(R.id.writepost_imageAddButton);
        register = findViewById(R.id.writepost_assign);

        // ?????? ??????????????? ???????????? ?????? ????????? ???????????? ?????? ????????????
        // ?????? ??????????????? ?????? ??????????????? ????????? ??? ????????? ????????? ????????? ????????????
        Intent intent = getIntent();
        String category = intent.getStringExtra("category");
        expertId = intent.getStringExtra("expertId");

        //??????????????? ????????? ?????? ??????
        TextView expert_name = findViewById(R.id.writepost_review_expertname);
        View line = findViewById(R.id.writepost_review_line);
        if (category.equals("review")) {
            expert_name.setVisibility(View.VISIBLE);
            line.setVisibility(View.VISIBLE);

            DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(expertId);

            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // ????????? ?????????, ????????? ?????? Url ??? ????????????
                            String expertname = document.getString("nickname");
                            expert_name.setText("????????? ????????? : " + expertname);
                        }
                    }
                }
            });


        } else {
            expert_name.setVisibility(View.GONE);
            line.setVisibility(View.GONE);

        }


        // ?????? ??????
        imageAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(imagesList.size() < 10){
                    selectFile();
                }
                else{
                    Toast.makeText(getApplicationContext(), "???????????? 10????????? ????????? ???????????????", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // ????????? ??????!
        register.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if(category.equals("review") && imagesList.size() == 0){
                    Toast.makeText(getApplicationContext(), "???????????? ????????? 1??? ?????? ??????????????? ?????????.", Toast.LENGTH_SHORT).show();
                }
                else if (title.length() > 0 && content.length() > 0) {
                    //?????????
                    loaderLayout.setVisibility(View.VISIBLE);
                    register.setEnabled(false);
                    post(category);
                } else if (title.length() <= 0) {
                    Toast.makeText(getApplicationContext(), "????????? ?????? 1??? ?????? ????????????.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "????????? ?????? 1??? ?????? ????????????.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ????????? ?????? ?????? ??? ???????????? ???????????? : ????????? ?????? ??????
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // ????????? ?????? ?????????
            image = data.getData();

            // ???????????? ??????
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(dpToPx(35), 0, dpToPx(35), 0);

            PostImage postimage = new PostImage(WritePostActivity.this, image, layoutParams);
            postimage.getBtn().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    layout.removeView(postimage);
                    imagesList.remove(image);
                }
            });

            layout.addView(postimage);
            imagesList.add(image);
        }
    }

    // ???????????? ??????
    private void selectFile() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "????????? ??????????????????."), 0);
    }

    // ??? ????????? ??????
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void post(String category) {
        System.out.println("yaa" + loaderLayout);


        // ????????? ?????? ?????????
        postId = String.valueOf(System.currentTimeMillis());

        //?????????
        contentList.add(content.getText().toString());

        if (imagesList.size() == 0) {// ????????? ?????????? ?????? ?????????
            Date currentTime = new Date(); // ????????? ?????? ??????
            String updateTime = new SimpleDateFormat("yyyy-MM-dd k:mm", Locale.getDefault()).format(currentTime);

            // ???????????? ??????
            postContent = new PostContent(category, userId, UserInfo.getProfileImg(), title.getText().toString(), nickname, updateTime, postId, UserInfo.getPushToken(), contentList, null, expertId, UserInfo.getIntroduction(), UserInfo.getLocation());

            // Firebase Realtime DB??? ??? ?????? ?????????
            databaseReference.child("Posts/" + category + "/" + postId).setValue(postContent).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    // ????????? ??? ????????? ????????? ???????????? ??? ????????? ????????? ??????
                    MainActivity.userPostsIdList.add(0, postId);
                    MainActivity.userPostsList.add(0, postContent);

                    // ??? ?????? ??? ????????? ?????? ????????????
                    if (category.equals("free")) CommunityFreeFragment.databaseReference.addListenerForSingleValueEvent(CommunityFreeFragment.postsListener);
                    if (category.equals("review")) CommunityReviewFragment.databaseReference.addListenerForSingleValueEvent(CommunityReviewFragment.postsListener);
                    if (category.equals("tip")) CommunityTipFragment.databaseReference.addListenerForSingleValueEvent(CommunityTipFragment.postsListener);
                    if (MyInfoFragment.post != null) MyInfoFragment.post.adapter.notifyDataSetChanged();

                    Toast.makeText(getApplicationContext(), "????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else { //????????? ????????? ????????????
            for (int i = 0; i < imagesList.size(); i++) {
                try {
                    image = imagesList.get(i);

                    // Firebase Storage??? ????????? ?????????
                    StorageReference imageReference = storageReference.child("imagesPosted/" + category + "/" + userId + "/" + postId + "/" + image.getLastPathSegment());

                    UploadTask uploadTask = imageReference.putFile(image);
                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            return imageReference.getDownloadUrl(); // URL??? ????????? ????????? ??? ??????????????? ?????? ??????
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() { // URL ?????? ?????? ???
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) { // URL??? ????????? ?????? Class(postContent)??? DB??? ????????????
                                Uri downloadUrl = task.getResult();
                                String url = downloadUrl.toString();

                                imagesUrl.add(url);
                                contentList.add("");
                                uploadFinishCount++;

                                if (uploadFinishCount == imagesList.size()) { // ????????? ???????????? ???????????? ?????? ??????????????? ?????????
                                    // ????????? ?????? ??????
                                    Date currentTime = new Date();
                                    String updateTime = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.getDefault()).format(currentTime);

                                    // ???????????? ??????
                                    postContent = new PostContent(category, userId, UserInfo.getProfileImg(), title.getText().toString(), nickname, updateTime, postId, UserInfo.getPushToken(), contentList, imagesUrl, expertId, UserInfo.getIntroduction(), UserInfo.getLocation());

                                    // Firebase Realtime DB??? ??? ?????? ?????????
                                    databaseReference.child("Posts/" + category + "/" + postId).setValue(postContent).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            // ????????? ??? ????????? ????????? ???????????? ??? ????????? ????????? ??????
                                            MainActivity.userPostsIdList.add(0, postId);
                                            MainActivity.userPostsList.add(0, postContent);

                                            // ??? ?????? ??? ????????? ?????? ????????????
                                            if (category.equals("free")) CommunityFreeFragment.databaseReference.addListenerForSingleValueEvent(CommunityFreeFragment.postsListener);
                                            if (category.equals("review")) CommunityReviewFragment.databaseReference.addListenerForSingleValueEvent(CommunityReviewFragment.postsListener);
                                            if (category.equals("tip")) CommunityTipFragment.databaseReference.addListenerForSingleValueEvent(CommunityTipFragment.postsListener);
                                            if (MyInfoFragment.post != null) MyInfoFragment.post.adapter.notifyDataSetChanged();

                                            Toast.makeText(getApplicationContext(), "????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    });
                                }

                                Toast.makeText(WritePostActivity.this, "?????????", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (NullPointerException e) {
                    Toast.makeText(WritePostActivity.this, "????????? ????????? ??????", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}