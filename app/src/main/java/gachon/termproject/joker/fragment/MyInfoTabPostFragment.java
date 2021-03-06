package gachon.termproject.joker.fragment;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

import gachon.termproject.joker.Content.PostContent;
import gachon.termproject.joker.R;
import gachon.termproject.joker.UserInfo;
import gachon.termproject.joker.activity.MainActivity;
import gachon.termproject.joker.adapter.MyInfoTabPostAdapter;

public class MyInfoTabPostFragment extends Fragment {
    private View view;
    private RecyclerView contents;
    private int successCount = 0;
    private int failCount = 0;
    public static MyInfoTabPostAdapter adapter;
    public static DatabaseReference postsRef;
    public static OnSuccessListener onSuccessListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.myinfo_post, container, false);

        contents = view.findViewById(R.id.content_post_myinfo);
        adapter = new MyInfoTabPostAdapter(getActivity(), MainActivity.userPostsList);

        contents.setLayoutManager(new GridLayoutManager(getContext(), 3));
        contents.setHasFixedSize(true);
        contents.setAdapter(adapter);

        postsRef = FirebaseDatabase.getInstance().getReference("Posts");

        onSuccessListener = new OnSuccessListener<DataSnapshot>() { // DB??? Posts??? ????????? ???????????? ?????? ???????????????
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) { // Posts??? ????????? ????????? ??????

                } else { // Posts??? ????????? ????????? ????????? ????????? ?????? ???????????? ?????? ??????
                    MainActivity.userPostsList.clear();
                    final long categoryNum = dataSnapshot.getChildrenCount(); // ?????? Posts??? ?????? category ???
                    
                    dataSnapshot.getRef().addChildEventListener(new ChildEventListener() { // Posts??? ?????? ??????????????? ???????????? ???????????? ????????? ???????????????
                        @Override
                        public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

                            snapshot.getRef().orderByChild("userId").equalTo(UserInfo.getUserId()).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                                @Override
                                public void onSuccess(DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists()) { // ?????? ??? ?????? ???????????? ????????????
                                        failCount++;
                                    } else { // ?????? ??? ?????? ?????????
                                        dataSnapshot.getRef().addValueEventListener(new ValueEventListener() { // ?????? ??????
                                            @RequiresApi(api = Build.VERSION_CODES.N)
                                            @Override
                                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                for (DataSnapshot shot : snapshot.getChildren()) {
                                                    PostContent content = shot.getValue(PostContent.class);
                                                    MainActivity.userPostsIdList.add(0, content.getPostId());
                                                    MainActivity.userPostsList.add(0, content);
                                                }
                                                successCount++; // ?????? ????????? ?????????
                                                if (failCount + successCount == categoryNum) { // ?????? ???????????? ?????? ??? ?????????
                                                    failCount = 0;
                                                    successCount = 0;
                                                    MainActivity.userPostsList.sort(new Comparator<PostContent>() {
                                                        @RequiresApi(api = Build.VERSION_CODES.O)
                                                        @Override
                                                        public int compare(PostContent o1, PostContent o2) {
                                                            long o1Id = Long.parseUnsignedLong(o1.getPostId());
                                                            long o2Id = Long.parseUnsignedLong(o2.getPostId());

                                                            if (o1Id < o2Id) return 1;
                                                            else return -1;
                                                        }
                                                    });
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }
                            });
                        }

                        @Override
                        public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                        }

                        @Override
                        public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {
                        }

                        @Override
                        public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        }
                    });
                }
            }
        };

        return view;
        
    }
}