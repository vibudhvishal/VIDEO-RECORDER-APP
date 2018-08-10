package vibudhvishal.videorecorder;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class FileListAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<Video> videos;
    private ViewHolder holder;

    public FileListAdapter(Context context,ArrayList<Video> videos){
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.videos = videos;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return getCount();
    }

    @Override
    public int getCount() {
        return videos.size();
    }

    @Override
    public Object getItem(int i) {
        return videos.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view==null){
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.file_list_item_layout,null);
            holder.txtName = view.findViewById(R.id.txtName);
            holder.txtAttr = view.findViewById(R.id.txtAttr);

            holder.txtName.setText(videos.get(i).getFileName());
            holder.txtAttr.setText(String.format("Created on: %s", android.text.format.DateFormat.format("dd/MM/yyyy hh:mm a", videos.get(i).getCreateTime())));

            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }

        return view;
    }

    class ViewHolder{
        TextView txtName,txtAttr;
    }
}
