package my.object_detect_app.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;

import my.object_detect_app.entity.Video;

import static my.object_detect_app.Config.LOGGING_TAG;

/**
 * User: Lizhiguo
 */
public class VideoAdapter extends SelectAdapter {
    private ArrayList<Video> mVideos;

    //保存选中的视频
    private ArrayList<Video> mSelectVideos = new ArrayList<>();

    private OnVideoSelectListener mSelectListener;
    private OnItemClickListener mItemClickListener;

    /**
     * @param context
     * @param maxCount    视频的最大选择数量，小于等于0时，不限数量，isSingle为false时才有用。
     * @param isSingle    是否单选
     * @param isViewImage 是否点击放大视频查看
     */
    public VideoAdapter(Context context, int maxCount, boolean isSingle, boolean isViewImage) {
        super(context, maxCount, isSingle, isViewImage);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_IMAGE) {
            final Video video = getVideo(position);
            Glide.with(mContext).load(new File(video.getThumbnailPath()))
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                    .into(holder.ivImage);
            Log.i(LOGGING_TAG,"thumbnailPath is :" + video.getThumbnailPath());

            setItemSelect(holder, mSelectVideos.contains(video));

            //点击选中/取消选中视频
            holder.ivSelectIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkedVideo(holder, video);
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isViewImage) {
                        if (mItemClickListener != null) {
                            int p = holder.getAdapterPosition();
                            mItemClickListener.OnItemClick(video, useCamera ? p - 1 : p);
                        }
                    } else {
                        checkedVideo(holder, video);
                    }
                }
            });
        } else if (getItemViewType(position) == TYPE_CAMERA) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.OnCameraClick();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return useCamera ? getImageCount() + 1 : getImageCount();
    }

    private int getImageCount() {
        return mVideos == null ? 0 : mVideos.size();
    }

    private Video getVideo(int position) {
        return mVideos.get(useCamera ? position - 1 : position);
    }

    private void checkedVideo(ViewHolder holder, Video video) {
        if (mSelectVideos.contains(video)) {
            //如果视频已经选中，就取消选中
            unSelectVideo(video);
            setItemSelect(holder, false);
        } else if (isSingle) {
            //如果是单选，就先清空已经选中的视频，再选中当前视频
            clearVideoSelect();
            selectVideo(video);
            setItemSelect(holder, true);
        } else if (mMaxCount <= 0 || mSelectVideos.size() < mMaxCount) {
            //如果不限制视频的选中数量，或者视频的选中数量
            // 还没有达到最大限制，就直接选中当前视频
            selectVideo(video);
            setItemSelect(holder, true);
        }
    }

    /**
     * 取消选中视频
     *
     * @param video
     */
    private void unSelectVideo( Video video) {
        mSelectVideos.remove(video);
        if (mSelectListener != null) {
            mSelectListener.OnVideoSelect(video, false, mSelectVideos.size());
        }
    }

    private void clearVideoSelect() {
        if (mVideos != null && mSelectVideos.size() == 1) {
            int index = mVideos.indexOf(mSelectVideos.get(0));
            mSelectVideos.clear();
            if (index != -1) {
                notifyItemChanged(useCamera ? index + 1 : index);
            }
        }
    }

    /**
     * 选中视频
     *
     * @param video
     */
    private void selectVideo(Video video) {
        mSelectVideos.add(video);
        if (mSelectListener != null) {
            mSelectListener.OnVideoSelect(video, true, mSelectVideos.size());
        }
    }

    public void setOnVideoSelectListener(OnVideoSelectListener listener) {
        this.mSelectListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public interface OnVideoSelectListener {
        void OnVideoSelect(Video video, boolean isSelect, int selectCount);
    }

    public interface OnItemClickListener {
        void OnItemClick(Video video, int position);

        void OnCameraClick();
    }

    public Video getFirstVisibleVideo(int firstVisibleItem) {
        if (mVideos != null && !mVideos.isEmpty()) {
            if (useCamera) {
                return mVideos.get(firstVisibleItem == 0 ? 0 : firstVisibleItem - 1);
            } else {
                return mVideos.get(firstVisibleItem);
            }
        }
        return null;
    }

    public void refresh(ArrayList<Video> data, boolean useCamera) {
        mVideos = data;
        this.useCamera = useCamera;
        notifyDataSetChanged();
    }

    public ArrayList<Video> getSelectVideos() {
        return mSelectVideos;
    }


}
