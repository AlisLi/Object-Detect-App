package my.object_detect_app.adapter;

import android.content.Context;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;

import my.object_detect_app.entity.Image;
import my.object_detect_app.entity.ImageFolder;

/**
 * User: Lizhiguo
 */
public class ImageFolderAdapter extends FolderAdapter {
    private ArrayList<ImageFolder> mFolders;

    public ImageFolderAdapter(Context context) {
        super(context);
    }

    public ImageFolderAdapter(Context context, ArrayList<ImageFolder> folders) {
        super(context);
        mFolders = folders;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final ImageFolder folder = mFolders.get(position);
        ArrayList<Image> images = folder.getImages();
        holder.tvFolderName.setText(folder.getName());
        holder.ivSelect.setVisibility(mSelectItem == position ? View.VISIBLE : View.GONE);
        if (images != null && !images.isEmpty()) {
            holder.tvFolderSize.setText(images.size() + "张");
            Glide.with(mContext).load(new File(images.get(0).getPath()))
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
                    mListener.OnImageFolderSelect(folder);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFolders == null ? 0 : mFolders.size();
    }

}
