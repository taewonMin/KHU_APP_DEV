package com.studylink.khu_app;

import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimelineActivity extends Fragment {

    private ImageView timelinePost;
    private String write_title;
    private RecyclerView recycler_studyname;
    private RecyclerView recycler_timelineBoard;
    private ArrayList<RoomDTO> nameofroom = new ArrayList<>();
    private ArrayList<String> getroomname = new ArrayList<>();
    private ArrayList<ArrayList<Uri>> groupUri = new ArrayList<>();
    private StudynameRecyclerViewAdapter StudynameAdapter;
    private TimelineBoardViewAdapter timelineBoardViewAdapter;
    private PictureRecyclerViewAdapter pictureRecyclerViewAdapter;
    private FirebaseDatabase mdatabase;
    private DatabaseReference dataref;
    private StorageReference storageReference;
    private FirebaseAuth mauth;
    private FirebaseStorage storage;
    private String currentUserId;
    private ArrayList<RoomUploadDTO> uploadDTOArrayList = new ArrayList<>();
    private ArrayList<Uri> imageUri = new ArrayList<>();
    private int i = 0;
    private int k = 0;
    private String currentRoomuid;
    private String roomkey;
    private int selectedPosition = 0;
    private ViewGroup view;
    private AccountDTO acc;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = (ViewGroup) inflater.inflate(R.layout.timeline_main, container, false);
        mdatabase = FirebaseDatabase.getInstance();
        dataref = mdatabase.getReference();
        mauth = FirebaseAuth.getInstance();
        currentUserId = mauth.getCurrentUser().getUid();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReferenceFromUrl("gs://studylink-ec173.appspot.com").child("images/");

        nameofroom.clear();
        getroomname.clear();
        uploadDTOArrayList.clear();
        groupUri.clear();

        getbundle();

        // 글 작성버튼
        timelinePost = view.findViewById(R.id.timelinePost);
        timelinePost.setClickable(true);
        timelinePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TimelinePost.class);
                if(nameofroom != null & selectedPosition != nameofroom.size()){             // "참여한 스터디가 없거나, 나가기 버튼을 누른 경우" 제외
                    intent.putExtra("currentRoom", nameofroom.get(selectedPosition));
                    intent.putExtra("selectedPosition", selectedPosition);
                    intent.putExtra("currentUser", acc);

                    startActivity(intent);

                    Fragement_navi navi = new Fragement_navi();
                    navi.finish();
                }
                else{
                    Toast.makeText(getActivity(), "여기서는 할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        recycler_studyname = view.findViewById(R.id.recycler_studyname);

        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(getContext());
        horizontalLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//        horizontalLayoutManager.setReverseLayout(true);
//        horizontalLayoutManager.setStackFromEnd(true);
        recycler_studyname.setLayoutManager(horizontalLayoutManager);

        StudynameAdapter = new StudynameRecyclerViewAdapter(nameofroom);
        recycler_studyname.setAdapter(StudynameAdapter);

        setData();

        StudynameAdapter.notifyDataSetChanged();

        // 초기 선택된 방 - 가장 최근에 참여한 스터디
        dataref.child("users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                AccountDTO acc = dataSnapshot.getValue(AccountDTO.class);
                if(acc.getRoomId()!=null){
                    currentRoomuid = acc.getRoomId().get(selectedPosition);
                    setTimelineData();

                    timelineBoardViewAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        recycler_timelineBoard = view.findViewById(R.id.recycler_timelineBoard);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recycler_timelineBoard.setLayoutManager(layoutManager);

        timelineBoardViewAdapter = new TimelineBoardViewAdapter(uploadDTOArrayList);
        recycler_timelineBoard.setAdapter(timelineBoardViewAdapter);

        timelineBoardViewAdapter.notifyDataSetChanged();

        //initialize();

        return view;
    }

    private void getbundle(){
        int myRoomNum = ((Fragement_navi)getActivity()).myRoomNum;

        if(getArguments() != null){
            Bundle bundle = getArguments();
            selectedPosition = bundle.getInt("myRoomNum");
//            imageUri = bundle.getParcelableArrayList("ImageData");
//            roomkey = bundle.getString("roomkey");
//            write_title = bundle.getString("puttitle");
//            timelineBoardViewAdapter.notifyDataSetChanged();
        }
        else if(myRoomNum!=0){
            selectedPosition = myRoomNum;
        }
    }

//    private void initialize(){
//        mdatabase.getReference().child("users").child(mauth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                AccountDTO accountDTO = dataSnapshot.getValue(AccountDTO.class);
//                currentRoomuid = accountDTO.getRoomId().get(0);
//
//                timelineBoardViewAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }

    private void setTimelineData() {
            uploadDTOArrayList.clear();
            dataref.child("RoomUpload").child(currentRoomuid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    RoomUploadDTO uploadDTO;
                    if (dataSnapshot != null){
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            uploadDTO = data.getValue(RoomUploadDTO.class);
                            uploadDTOArrayList.add(uploadDTO);
                        }
                        timelineBoardViewAdapter.notifyDataSetChanged();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }




    private void setData(){
        dataref.child("users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                acc = dataSnapshot.getValue(AccountDTO.class);
                if(acc.getRoomId()!=null){
                    for(int i = 0; i < acc.getRoomId().size(); i++) {
                        getroomname.add(acc.getRoomId().get(i));
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        setData1();
    }

    private void setData1(){
        dataref.child("room").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int j = 0;
                RoomDTO roomm;
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    roomm = data.getValue(RoomDTO.class);
                    if (getroomname != null){
                        if (j < getroomname.size()) {
                            if (getroomname.get(j).equals(roomm.getId())) {
                                nameofroom.add(roomm);
                                StudynameAdapter.notifyDataSetChanged();
                                j++;
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    class StudynameRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {      //어느 스터디인지를 선택하는 부분의 recycler

        private ArrayList<RoomDTO> Room;
        private final int TYPE_ITEM = 0;
        private final int TYPE_FOOTER = 1;
        private boolean exitBtn = false;

        public StudynameRecyclerViewAdapter(ArrayList<RoomDTO> items){
            Room = items;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == Room.size())
                return TYPE_FOOTER;
            else
                return TYPE_ITEM;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

            RecyclerView.ViewHolder holder;
            View view;

            if(viewType==TYPE_FOOTER){
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.exit_room, viewGroup, false);
                holder = new FooterViewHolder(view);
            }
            else{
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.timeline_cardview, viewGroup, false);
                holder = new StudynameViewHolder(view);
            }

            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
            if(viewHolder instanceof FooterViewHolder){
                if(selectedPosition == position){
                    ((FooterViewHolder)viewHolder).exitRoom.setBackground(getResources().getDrawable(R.drawable.timeline_studyname_cardview));
                    ((FooterViewHolder)viewHolder).exitRoomImg.setImageResource(R.mipmap.icon_48);
                }
                else{
                    ((FooterViewHolder)viewHolder).exitRoom.setBackground(getResources().getDrawable(R.drawable.timeline_studyname_default));
                    ((FooterViewHolder)viewHolder).exitRoomImg.setImageResource(R.mipmap.icon_2);
                }

                ((FooterViewHolder)viewHolder).exitRoomImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedPosition = position;
                        // 타임라인 비우기
                        ((FooterViewHolder)viewHolder).timeline_studyName.setText("");
                        ((FooterViewHolder)viewHolder).toStudyDetail.setText("");
                        uploadDTOArrayList.clear();
                        timelineBoardViewAdapter.notifyDataSetChanged();
                        // 나가기 이미지 활성화
                        exitBtn = true;
                        // 새로고침
                        notifyDataSetChanged();
                    }
                });
            }
            else{
                if(exitBtn){
                    ((StudynameViewHolder)viewHolder).exitImg.setVisibility(View.VISIBLE);
                }
                else{
                    ((StudynameViewHolder)viewHolder).exitImg.setVisibility(View.INVISIBLE);
                }

                ((StudynameViewHolder)viewHolder).studyname_title.setText(Room.get(position).getSpinner1());

                if(selectedPosition == position){
                    ((StudynameViewHolder)viewHolder).timeline_studyName.setText(Room.get(position).getSpinner1()+" 스터디");
                    ((StudynameViewHolder) viewHolder).studyname_title.setBackground(getResources().getDrawable(R.drawable.timeline_studyname_cardview));
                    ((StudynameViewHolder)viewHolder).studyname_title.setTextColor(getResources().getColor(R.color.localBluecolor));
                } else{
                    ((StudynameViewHolder) viewHolder).studyname_title.setBackground(getResources().getDrawable(R.drawable.timeline_studyname_default));
                    ((StudynameViewHolder)viewHolder).studyname_title.setTextColor(getResources().getColor(R.color.colorWhite));
                }

                ((StudynameViewHolder)viewHolder).toStudyDetail.setText("스터디 상세정보 >");
                ((StudynameViewHolder)viewHolder).toStudyDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), StudydetailActivity.class);
                        intent.putExtra("roomDetail", Room.get(selectedPosition));
                        startActivity(intent);
                    }
                });

                ((StudynameViewHolder) viewHolder).studyname_title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentRoomuid = Room.get(position).getId();
                        selectedPosition = position;
                        exitBtn = false;
                        setTimelineData();
                        notifyDataSetChanged();
                    }
                });

                ((StudynameViewHolder) viewHolder).exitImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(getActivity());
                        alert_confirm.setMessage("방을 탈퇴하시겠습니까?").setCancelable(false).setPositiveButton("확인",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // 'YES'
                                        RoomDTO exitRoom = Room.get(position);
                                        // 인원수 갱신
                                        exitRoom.setMember(exitRoom.getMember()-1);
                                        // DB수정
                                        dataref.child("room").child(exitRoom.getId()).setValue(exitRoom);
                                        dataref.child("users").child(currentUserId).child("roomId").child(Integer.toString(position)).removeValue();

                                        selectedPosition = 0;

                                        Toast.makeText(getActivity(), "탈퇴완료", Toast.LENGTH_SHORT).show();
                                        notifyDataSetChanged();

                                        Intent intent = new Intent(getActivity(), Fragement_navi.class);
                                        intent.putExtra("frag_num", 0);
                                        startActivity(intent);
                                    }
                                }).setNegativeButton("취소",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // 'No'
                                        return;
                                    }
                                });
                        AlertDialog alert = alert_confirm.create();
                        alert.show();
                    }
                });
            }

        }

        @Override
        public int getItemCount() {
            return Room.size()+1;
        }

        private class StudynameViewHolder extends RecyclerView.ViewHolder{

            ImageView exitImg;
            TextView studyname_title, timeline_studyName, toStudyDetail;

            public StudynameViewHolder(View itemView) {
                super(itemView);
                exitImg = itemView.findViewById(R.id.exitImg);
                studyname_title = itemView.findViewById(R.id.studyname_title);
                timeline_studyName = view.findViewById(R.id.timeline_studyName);
                toStudyDetail = view.findViewById(R.id.toStudyDetail);
            }
        }

        private class FooterViewHolder extends RecyclerView.ViewHolder {

            ConstraintLayout exitRoom;
            ImageView exitRoomImg;
            TextView timeline_studyName, toStudyDetail;

            FooterViewHolder(View footerView) {
                super(footerView);

                timeline_studyName = view.findViewById(R.id.timeline_studyName);
                toStudyDetail = view.findViewById(R.id.toStudyDetail);
                exitRoom = footerView.findViewById(R.id.exitRoom);
                exitRoomImg = footerView.findViewById(R.id.exitRoomImg);
            }
        }
    }



    class TimelineBoardViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{           //Timeline에 올라가는 컨텐츠의 recycler

//        private static final int VIEW_TYPE_FILE = 0;
//        private static final int VIEW_TYPE_VOTE = 1;

        private ArrayList<ArrayList<Uri>> uriArrayList;
        private ArrayList<RoomUploadDTO> dtoArrayList;

        public TimelineBoardViewAdapter (ArrayList<RoomUploadDTO> uploadDTOArrayList){
            dtoArrayList = uploadDTOArrayList;
        }

//        @Override
//        public int getItemViewType(int position){
//            return position % 2 == 0 ? VIEW_TYPE_FILE : VIEW_TYPE_VOTE;
//        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

 //           if (viewType == VIEW_TYPE_FILE) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.timeline_cardview_file, viewGroup, false);
            return new FileViewHolder(view);
 //           } else {
 //               View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.timeline_cardview_vote, viewGroup, false);
 //               return new VoteViewHolder(view);
 //                    }
        }
        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int position) {

            RoomUploadDTO roomUploadDTO = dtoArrayList.get(position);
            // 글쓴이 프로필 이미지 설정
            if(roomUploadDTO.getUploaderImg() != null){
                storageReference.child("profiles/" + roomUploadDTO.getUploaderId() + "/" + roomUploadDTO.getUploaderId() + ".jpeg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        ((FileViewHolder)viewHolder).file_imageview.setImageURI(uri);
                        Glide.with(getActivity()).load(uri.toString()).apply(RequestOptions.circleCropTransform())
                                .override(43,43).into(((FileViewHolder)viewHolder).file_imageview);
                    }
                });
            }
            else{
                Glide.with(getActivity()).load(R.mipmap.icon_24).apply(RequestOptions.circleCropTransform())
                        .override(43,43).into(((FileViewHolder)viewHolder).file_imageview);
            }

            ((FileViewHolder)viewHolder).file_username.setText(roomUploadDTO.getUploadername());
            ((FileViewHolder)viewHolder).file_contentText.setText(roomUploadDTO.getWriting_content());

            SimpleDateFormat dateFormat = new SimpleDateFormat("M월 d일");
            Date nowDate = new Date();          // 현재 시간
            Date uploadDate = roomUploadDTO.getTime();   // 글 생성 날짜

            long calDate = nowDate.getTime() - uploadDate.getTime();
            long day = calDate/(24*60*60*1000);
            long hour = calDate/(60*60*1000);
            long minute = calDate/(60*1000);
            day = Math.abs(day);
            hour = Math.abs(hour);
            minute = Math.abs(minute);
            if (day == 0){
                if(hour == 0){
                    if(minute < 1){
                        ((FileViewHolder)viewHolder).file_time.setText("방금");
                    }
                    else if(minute < 60){
                        ((FileViewHolder)viewHolder).file_time.setText(minute + "분 전");
                    }
                    else{
                        ((FileViewHolder)viewHolder).file_time.setText("안보임");
                    }
                }
                else{
                    ((FileViewHolder)viewHolder).file_time.setText(hour + "시간 전");
                }
            }
            else{
                ((FileViewHolder)viewHolder).file_time.setText(dateFormat.format(uploadDate));
            }


            ((FileViewHolder)viewHolder).file_scrap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            RecyclerView.LayoutManager horizontalLayoutManager = new LinearLayoutManager(getContext());
            ((LinearLayoutManager)horizontalLayoutManager).setOrientation(LinearLayoutManager.HORIZONTAL);
            ((FileViewHolder)viewHolder).recyclerView.setLayoutManager(horizontalLayoutManager);
            pictureRecyclerViewAdapter = new PictureRecyclerViewAdapter(roomUploadDTO);
            ((FileViewHolder)viewHolder).recyclerView.setAdapter(pictureRecyclerViewAdapter);
            pictureRecyclerViewAdapter.notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return dtoArrayList.size();
        }

        private void scrap(int position){

        }

        class TimelineBoardViewHolder extends RecyclerView.ViewHolder{

            public TimelineBoardViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

        private class FileViewHolder extends RecyclerView.ViewHolder{

            TextView file_username, file_textview, file_time, file_contentText;
            RecyclerView recyclerView;
            ImageView file_scrap, file_imageview;

            public FileViewHolder(@NonNull View itemView) {
                super(itemView);
                file_imageview = itemView.findViewById(R.id.file_imageview);
                file_username = itemView.findViewById(R.id.file_username);
                file_textview = itemView.findViewById(R.id.file_textview);
                file_time = itemView.findViewById(R.id.file_time);
                file_contentText = itemView.findViewById(R.id.file_contentText);
                recyclerView = itemView.findViewById(R.id.image_recycler);
                file_scrap = itemView.findViewById(R.id.file_scrap);
            }
        }

        class VoteViewHolder extends RecyclerView.ViewHolder{

            public VoteViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }




    class PictureRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{           //연결부분

        private RoomUploadDTO uploadDTOS;

        public PictureRecyclerViewAdapter(RoomUploadDTO array){
            uploadDTOS = array;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.timeline_writing_recycler_picture, viewGroup, false);

            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
            if(uploadDTOS.getFiletitle() != null) {
                for (int i = 0; i < uploadDTOS.getFiletitle().size(); i++) {
                    storageReference.child(currentRoomuid + "/" + uploadDTOS.getWriting_content() + "/" + uploadDTOS.getFiletitle().get(position))
                            .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            ((CustomViewHolder) viewHolder).upload_imageview.setImageURI(uri);
                            Glide.with(((CustomViewHolder) viewHolder).root).load(uri.toString()).into(((CustomViewHolder) viewHolder).upload_imageview);
//                            ((CustomViewHolder)viewHolder).upload_imageview.setImageMatrix();
                        }
                    });
                }
            }
        }

        @Override
        public int getItemCount() {
            int size;
            if(uploadDTOS.getFiletitle() != null){
                size = uploadDTOS.getFiletitle().size();
            } else {
                size = 0;
            }

            return size;
        }



        private class CustomViewHolder extends RecyclerView.ViewHolder {

            ImageView upload_imageview;
            public View root;



            public CustomViewHolder(View view) {
                super(view);
                root = view;
                upload_imageview = view.findViewById(R.id.upload_imageview);
            }
        }
    }


}

