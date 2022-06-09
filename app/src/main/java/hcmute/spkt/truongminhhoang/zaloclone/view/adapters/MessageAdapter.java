package hcmute.spkt.truongminhhoang.zaloclone.view.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import hcmute.spkt.truongminhhoang.zaloclone.R;
import hcmute.spkt.truongminhhoang.zaloclone.services.model.Chats;
import hcmute.spkt.truongminhhoang.zaloclone.utils.ImageConvert;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageHolder> {

    private static final int MSG_TYPE_LEFT_RECEIVED = 0;
    private static final int MSG_TYPE_RIGHT_RECEIVED = 1;
    private ArrayList<Chats> chatArrayList;
    private Context context;
    private String currentUser_sender;

    public MessageAdapter(ArrayList<Chats> chatArrayList, Context context, String currentUser_sender) {
        this.chatArrayList = chatArrayList;
        this.context = context;
        this.currentUser_sender = currentUser_sender;
    }


    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT_RECEIVED) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right_sent, parent, false);
            return new MessageHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left_received, parent, false);
            return new MessageHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MessageHolder holder, int position) {

        Chats chats = chatArrayList.get(position);
        String message = chats.getMessage();
        String type=chats.getType();
        String timeStamp = chats.getTimestamp();
        boolean isSeen = chats.getSeen();
        long intTimeStamp = Long.parseLong(timeStamp);
        String time_msg_received = timeStampConversionToTime(intTimeStamp);
        holder.tv_time.setText(time_msg_received);
        if(type.equals("text")){
            holder.tv_msg.setPadding(14,14,14,14);
            holder.tv_msg.setText(message);
        }
        else if(type.equals("image")){
            holder.iv_chat_image_received.getLayoutParams().height=400;
            holder.iv_chat_image_received.setImageBitmap(ImageConvert.convertBase64ToBitmap(message));
        }

        if (position == chatArrayList.size() - 1) {
            if (isSeen) {
                    holder.tv_seen.setVisibility(View.VISIBLE);
                String seen = "Seen";
                holder.tv_seen.setText(seen);
            } else {
                holder.tv_seen.setVisibility(View.VISIBLE);
                String delivered = "Delivered";
                holder.tv_seen.setText(delivered);
            }
        } else {
            holder.tv_seen.setVisibility(View.GONE);
        }
    }

    public String timeStampConversionToTime(long timeStamp) {

        Date date = new Date(timeStamp);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat jdf = new SimpleDateFormat("hh:mm a");
        jdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        return jdf.format(date);

    }

    @Override
    public int getItemCount() {
        return chatArrayList.size();
    }

    public class MessageHolder extends RecyclerView.ViewHolder {
        TextView tv_msg;
        TextView tv_time;
        TextView tv_seen;
        ImageView iv_chat_image_received;
        public MessageHolder(@NonNull View itemView) {
            super(itemView);
            tv_msg = itemView.findViewById(R.id.tv_chat_received);
            tv_time = itemView.findViewById(R.id.tv_chat_time_received);
            tv_seen = itemView.findViewById(R.id.tv_seen);
            iv_chat_image_received=itemView.findViewById(R.id.iv_chat_image_received);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (chatArrayList.get(position).getReceiverId().equals(currentUser_sender)) {
            return MSG_TYPE_LEFT_RECEIVED;
        } else return MSG_TYPE_RIGHT_RECEIVED;
    }
}
