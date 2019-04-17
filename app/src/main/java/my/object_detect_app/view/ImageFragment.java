package my.object_detect_app.view;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import my.object_detect_app.R;

import static my.object_detect_app.utils.imageSelect.ImageUtils.getLocalBitmap;

/**
 * User: Lizhiguo
 */
public class ImageFragment extends Fragment {
    private ImageView localImageView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.image_fragment, container, false);
        Bundle bundle = getArguments();
        String imagePath = bundle.getString("imagePath");

        Bitmap bitmap = getLocalBitmap(imagePath);

        localImageView = view.findViewById(R.id.img_local);
        localImageView.setImageBitmap(bitmap);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

}
