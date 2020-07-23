package com.neo.firebaseuserandemailverification.utility;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.neo.firebaseuserandemailverification.R;
import com.neo.firebaseuserandemailverification.models.ChatMessage;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by User on 9/18/2017.
 */

public class ChatMessageListAdapter extends ArrayAdapter<ChatMessage> {

    private static final String TAG = "ChatMessageListAdapter";

    private int mLayoutResource;
    private Context mContext;

    public ChatMessageListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<ChatMessage> objects) {
        super(context, resource, objects);
        mContext = context;
        mLayoutResource = resource;
    }

    public static class ViewHolder{
        TextView name, message;
        ImageView mProfileImage;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mLayoutResource, parent, false);
            holder = new ViewHolder();

            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.message = (TextView) convertView.findViewById(R.id.message);
            holder.mProfileImage = (ImageView) convertView.findViewById(R.id.profile_image);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
            holder.name.setText("");
            holder.message.setText("");
        }

        try{
            //set the message
            holder.message.setText(getItem(position).getMessage());
            if(!getItem(position).getProfile_image().equals("")){
                Picasso.get().load(getItem(position).getProfile_image()).into(holder.mProfileImage);
            }
            holder.name.setText(getItem(position).getName());


        }catch (NullPointerException e){
            Log.e(TAG, "getView: NullPointerException: ", e.getCause() );
        }

        return convertView;
    }

}

















