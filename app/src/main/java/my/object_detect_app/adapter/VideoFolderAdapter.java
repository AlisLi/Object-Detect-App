package my.object_detect_app.adapter;

import android.content.Context;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;

import my.object_detect_app.entity.Video;
import my.object_detect_app.entity.VideoFolder;

/**
 * User: Lizhiguo
 */
public class VideoFolderAdapter extends FolderAdapter {
    private ArrayList<VideoFolder> mFolders;

    public VideoFolderAdapter(Context context, ArrayList<VideoFolder> folders) {
        super(context);
        mFolders = folders;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final VideoFolder folder = mFolders.get(position);
        ArrayList<Video> videos = folder.getVideos();
        holder.tvFolderName.setText(folder.getName());
        holder.ivSelect.setVisibility(mSelectItem == position ? View.VISIBLE : View.GONE);
        if (videos != null && !videos.isEmpty()) {
            holder.tvFolderSize.setText(videos.size() + "张");
            Glide.with(mContext).load(new File(videos.get(0).getThumbnailPath()))
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(holder.ivImage);
        } else {
            holder.tvFolderSize.setText("0张");
            holder.ivImage.setImageBitmap(null);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectItem = holder.getAdapterPosition();
                notifyDataSetChanged();
                if (mListener != null) {
                    mListener.OnVideoFolderSelect(folder);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFolders == null ? 0 : mFolders.size();
    }

}
