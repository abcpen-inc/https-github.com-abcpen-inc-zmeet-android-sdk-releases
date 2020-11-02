package com.zmeetsdk;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.abcpen.meet.rtc.ZRtcEngine;
import com.abcpen.meet.view.ZMeetVideoCanvas;
import com.blankj.utilcode.util.LogUtils;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import circletextimage.viviant.com.circletextimagelib.view.CircleTextImage;

/**
 * 创建时间: 2020/10/13
 * coder: Alaske
 * description：
 */
public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

    private List<UserItem> uidList;
    private OnSelectListener selectListener;

    public interface OnSelectListener {
        void onItemSelect(int index);
    }

    public VideoAdapter(List<UserItem> uidList, OnSelectListener onSelectListener) {
        this.uidList = uidList;
        this.selectListener = onSelectListener;
    }


    public static class UserItem {
        private String uid;
        private String name;
        private boolean hasVideo;
        private boolean select = false;

        public UserItem(String uid, String name, boolean hasVideo) {
            this.uid = uid;
            this.name = name;
            this.hasVideo = hasVideo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UserItem)) return false;
            UserItem userItem = (UserItem) o;
            return Objects.equals(getUid(), userItem.getUid()) ;
        }

        @Override
        public int hashCode() {
            return Objects.hash(getUid());
        }

        public boolean isSelect() {
            return select;
        }

        public void setSelect(boolean select) {
            this.select = select;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isHasVideo() {
            return hasVideo;
        }

        public void setHasVideo(boolean hasVideo) {
            this.hasVideo = hasVideo;
        }

        public boolean isLocal() {
            return TextUtils.equals(uid,MeetDebugCons.UID);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(selectListener, LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserItem userItem = uidList.get(position);
        if (!userItem.hasVideo || userItem.select) {
            holder.zMeetVideoCanvas.setVisibility(View.GONE);
            holder.circleTextImage.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(userItem.name)) {
                holder.circleTextImage.setText4CircleImage(userItem.name);
            } else {
                holder.circleTextImage.setText4CircleImage("ZMeet");
            }
        } else {
            holder.zMeetVideoCanvas.setVisibility(View.VISIBLE);
            holder.circleTextImage.setVisibility(View.GONE);
            if (TextUtils.equals(MeetDebugCons.UID, userItem.uid)) {
                ZRtcEngine.get().setupLocalVideo(holder.zMeetVideoCanvas);
            } else {
                ZRtcEngine.get().setupRemoteVideo(holder.zMeetVideoCanvas, userItem.uid);
            }
        }
    }

    @Override
    public int getItemCount() {
        return uidList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ZMeetVideoCanvas zMeetVideoCanvas;
        private CircleTextImage circleTextImage;

        public ViewHolder(OnSelectListener selectListener, @NonNull View itemView) {
            super(itemView);
            zMeetVideoCanvas = itemView.findViewById(R.id.zmeet_video);
            circleTextImage = itemView.findViewById(R.id.iv_name);

            zMeetVideoCanvas.setOnClickListener(v -> {
                if (selectListener != null) {
                    selectListener.onItemSelect(getLayoutPosition());
                }
            });
        }

    }
}
